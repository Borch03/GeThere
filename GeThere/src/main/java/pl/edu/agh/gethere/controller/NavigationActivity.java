package pl.edu.agh.gethere.controller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.vr.sdk.base.*;
import jmini3d.JMini3d;
import jmini3d.android.Renderer3d;
import jmini3d.android.ResourceLoader;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.connection.LocationProvider;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.service.NavigationService;
import pl.edu.agh.gethere.service.NavigationServiceCallbacks;

import javax.microedition.khronos.egl.EGLConfig;
import java.util.Arrays;

public class NavigationActivity extends GvrActivity implements GvrView.StereoRenderer, NavigationServiceCallbacks {

    private static final String TAG = "AndroidCameraApi";

    private TextureView leftTextureView;
    private TextureView rightTextureView;
    private Renderer3d renderer;

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private LocationProvider locationProvider = new LocationProvider();
    private Coordinates destination;
    private NavigationService navigationService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent = getIntent();
//        destination = (Coordinates) intent.getSerializableExtra("destination");
        destination = new Coordinates(50.0325524,19.9368543);
        initializeVrStuff();
        leftTextureView = (TextureView) findViewById(R.id.leftTexture);
        rightTextureView = (TextureView) findViewById(R.id.rightTexture);
        leftTextureView.setRotation(270.0f);
        rightTextureView.setRotation(270.0f);
        leftTextureView.setSurfaceTextureListener(textureListener);
        rightTextureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NavigationService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    private void initializeVrStuff() {
        setContentView(R.layout.activity_navigation);
        JMini3d.useOpenglAxisSystem();
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(this);
//        gvrView.setDistortionCorrectionEnabled(true);
        gvrView.setAsyncReprojectionEnabled(true);
        setGvrView(gvrView);
        renderer = new Renderer3d(new ResourceLoader(this));
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {

    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        renderer.setViewPort(width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        renderer.reset();
    }

    @Override
    public void onRendererShutdown() {

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NavigationService.LocalBinder binder = (NavigationService.LocalBinder) service;
            navigationService = binder.getService();
            bound = true;
            navigationService.setCallbacks(NavigationActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture leftTexture = leftTextureView.getSurfaceTexture();
            SurfaceTexture rightTexture = rightTextureView.getSurfaceTexture();
            leftTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            rightTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface leftSurface = new Surface(leftTexture);
            Surface rightSurface = new Surface(rightTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(leftSurface);
            captureRequestBuilder.addTarget(rightSurface);
            cameraDevice.createCaptureSession(Arrays.asList(leftSurface, rightSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(NavigationActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NavigationActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(NavigationActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (leftTextureView.isAvailable() && rightTextureView.isAvailable()) {
            openCamera();
        } else {
            leftTextureView.setSurfaceTextureListener(textureListener);
            rightTextureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            navigationService.setCallbacks(null);
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public Coordinates getOrigin() {
        return locationProvider.getLocation(this);
    }

    @Override
    public Coordinates getDestination() {
        return destination;
    }

    @Override
    public void setTotalDistance(final String totalDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView totalDistanceL = (TextView) findViewById(R.id.totalDistanceL);
                final TextView totalDistanceR = (TextView) findViewById(R.id.totalDistanceR);
                totalDistanceL.setText(totalDistance);
                totalDistanceR.setText(totalDistance);
            }
        });
    }

    @Override
    public void activeLeftArrow(final String maneuverDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageButton leftArrowL = (ImageButton) findViewById(R.id.leftArrowL);
                final ImageButton leftArrowR = (ImageButton) findViewById(R.id.leftArrowR);
                final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
                final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
                deactiveArrows();
                leftArrowL.setVisibility(View.VISIBLE);
                leftArrowR.setVisibility(View.VISIBLE);
                maneuverDistanceL.setText(maneuverDistance);
                maneuverDistanceR.setText(maneuverDistance);
            }
        });
    }

    @Override
    public void activeUpArrow(final String maneuverDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageButton upArrowL = (ImageButton) findViewById(R.id.upArrowL);
                final ImageButton upArrowR = (ImageButton) findViewById(R.id.upArrowR);
                final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
                final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
                deactiveArrows();
                upArrowL.setVisibility(View.VISIBLE);
                upArrowR.setVisibility(View.VISIBLE);
                maneuverDistanceL.setText(maneuverDistance);
                maneuverDistanceR.setText(maneuverDistance);
            }
        });
    }

    @Override
    public void activeRightArrow(final String maneuverDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageButton rightArrowL = (ImageButton) findViewById(R.id.rightArrowL);
                final ImageButton rightArrowR = (ImageButton) findViewById(R.id.rightArrowR);
                final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
                final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
                deactiveArrows();
                rightArrowL.setVisibility(View.VISIBLE);
                rightArrowR.setVisibility(View.VISIBLE);
                maneuverDistanceL.setText(maneuverDistance);
                maneuverDistanceR.setText(maneuverDistance);
            }
        });
    }

    @Override
    public void activeNullArrow(final String maneuverDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
                final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
                deactiveArrows();
                maneuverDistanceL.setText(maneuverDistance);
                maneuverDistanceR.setText(maneuverDistance);
            }
        });
    }

    private void deactiveArrows() {
        final ImageButton leftArrowL = (ImageButton) findViewById(R.id.leftArrowL);
        final ImageButton leftArrowR = (ImageButton) findViewById(R.id.leftArrowR);
        final ImageButton upArrowL = (ImageButton) findViewById(R.id.upArrowL);
        final ImageButton upArrowR = (ImageButton) findViewById(R.id.upArrowR);
        final ImageButton rightArrowL = (ImageButton) findViewById(R.id.rightArrowL);
        final ImageButton rightArrowR = (ImageButton) findViewById(R.id.rightArrowR);
        final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
        final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
        final TextView totalDistanceL = (TextView) findViewById(R.id.totalDistanceL);
        final TextView totalDistanceR = (TextView) findViewById(R.id.totalDistanceR);

        leftArrowL.setVisibility(View.INVISIBLE);
        leftArrowR.setVisibility(View.INVISIBLE);
        upArrowL.setVisibility(View.INVISIBLE);
        upArrowR.setVisibility(View.INVISIBLE);
        rightArrowL.setVisibility(View.INVISIBLE);
        rightArrowR.setVisibility(View.INVISIBLE);
        maneuverDistanceL.setText("0m");
        maneuverDistanceR.setText("0m");
    }
}
