package pl.edu.agh.gethere.controller;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.vr.sdk.base.*;
import jmini3d.JMini3d;
import jmini3d.android.Renderer3d;
import jmini3d.android.ResourceLoader;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.connection.LocationProvider;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.service.GoogleMapService;
import pl.edu.agh.gethere.service.GoogleMapServiceCallbacks;
import pl.edu.agh.gethere.service.NavigationService;
import pl.edu.agh.gethere.service.NavigationServiceCallbacks;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import javax.microedition.khronos.egl.EGLConfig;
import java.util.ArrayList;
import java.util.Arrays;

public class NavigationActivity extends GvrActivity implements GvrView.StereoRenderer, OnMapReadyCallback,
        DirectionCallback, NavigationServiceCallbacks, GoogleMapServiceCallbacks {

    private static final String TAG = "AndroidCameraApi";
    private static final String API_KEY = "AIzaSyDrUH6-lmzWNG5fWhcM9uNosu8BmZYH6bw";

    // Camera
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

    // Navigation
    private LocationProvider locationProvider = new LocationProvider();
    private Coordinates destination;
    private Coordinates origin;
    private NavigationService navigationService;
    private boolean navigationBound = false;

    //GoogleMap
    private GoogleMapService googleMapService;
    private boolean mapBound = false;
    private GoogleMap googleMap;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent = getIntent();
//        destination = (Coordinates) intent.getSerializableExtra("destination");
        destination = new Coordinates(50.0325625,19.939043);
        origin = getOrigin();
        initializeVrStuff();
        initMap();
        startLoading();
    }

    //Camera

    @Override
    protected void onStart() {
        super.onStart();
        Intent googleMapIntent = new Intent(this, GoogleMapService.class);
        Intent navigationIntent = new Intent(this, NavigationService.class);
        bindService(googleMapIntent, mapServiceConnection, Context.BIND_AUTO_CREATE);
        bindService(navigationIntent, navigationServiceConnection, Context.BIND_AUTO_CREATE);
        startService(googleMapIntent);
        startService(navigationIntent);
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

        leftTextureView = (TextureView) findViewById(R.id.leftTexture);
        rightTextureView = (TextureView) findViewById(R.id.rightTexture);
        leftTextureView.setRotation(270.0f);
        rightTextureView.setRotation(270.0f);
        leftTextureView.setSurfaceTextureListener(textureListener);
        rightTextureView.setSurfaceTextureListener(textureListener);
    }

    private void initMap() {
        GoogleMapOptions googleMapOptions = new GoogleMapOptions();
        googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .mapToolbarEnabled(false)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = MapFragment.newInstance(googleMapOptions);
        mapFragment.getMapAsync(this);
        fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
    }

    private void startLoading() {
        progress = ProgressDialog.show(this, "Loading", "Loading...", true);
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

    private ServiceConnection mapServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            GoogleMapService.LocalBinder binder = (GoogleMapService.LocalBinder) service;
            googleMapService = binder.getService();
            mapBound = true;
            googleMapService.setCallbacks(NavigationActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mapBound = false;
        }
    };

    private ServiceConnection navigationServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NavigationService.LocalBinder binder = (NavigationService.LocalBinder) service;
            navigationService = binder.getService();
            navigationBound = true;
            navigationService.setCallbacks(NavigationActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            navigationBound = false;
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
        if (navigationBound) {
            navigationService.setCallbacks(null);
            unbindService(navigationServiceConnection);
            navigationBound = false;
        }
        if (mapBound) {
            googleMapService.setCallbacks(null);
            unbindService(mapServiceConnection);
            mapBound = false;
        }
    }

    // Navigation

    @Override
    public Coordinates getOrigin() {
        origin = locationProvider.getLocation(this);
        return origin;
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

    @Override
    public void activeFinish(final String maneuverDistance) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageButton finishL = (ImageButton) findViewById(R.id.finishL);
                final ImageButton finishR = (ImageButton) findViewById(R.id.finishR);
                final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
                final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);
                deactiveArrows();
                finishL.setVisibility(View.VISIBLE);
                finishR.setVisibility(View.VISIBLE);
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
        final ImageButton finishL = (ImageButton) findViewById(R.id.finishL);
        final ImageButton finishR = (ImageButton) findViewById(R.id.finishR);
        final TextView maneuverDistanceL = (TextView) findViewById(R.id.maneuverDistanceL);
        final TextView maneuverDistanceR = (TextView) findViewById(R.id.maneuverDistanceR);

        leftArrowL.setVisibility(View.INVISIBLE);
        leftArrowR.setVisibility(View.INVISIBLE);
        upArrowL.setVisibility(View.INVISIBLE);
        upArrowR.setVisibility(View.INVISIBLE);
        rightArrowL.setVisibility(View.INVISIBLE);
        rightArrowR.setVisibility(View.INVISIBLE);
        finishL.setVisibility(View.INVISIBLE);
        finishR.setVisibility(View.INVISIBLE);
        maneuverDistanceL.setText("0m");
        maneuverDistanceR.setText("0m");
    }

    //GoogleMap

    @Override
    public void updateMap() {
        GoogleDirection.withServerKey(API_KEY)
                .from(convertCoordinatesToLatLng(origin))
                .to(convertCoordinatesToLatLng(destination))
                .transportMode(TransportMode.WALKING)
                .execute(this);
    }

    @Override
    public void stopLoading() {
        setMapInvisible();
        progress.dismiss();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(convertCoordinatesToLatLng(origin), 17));
    }

    private LatLng convertCoordinatesToLatLng(Coordinates coordinates) {
        return new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            googleMap.clear();
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(convertCoordinatesToLatLng(origin))
                    .bearing(locationProvider.getBearing(this)).tilt(0f).zoom(17f).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            googleMap.addMarker(new MarkerOptions().position(convertCoordinatesToLatLng(origin)));
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 3, 0xFF0080FF));

            GoogleMap.SnapshotReadyCallback snapshotCallback = new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap snapshot) {
                    setMapImage(snapshot);
                }
            };
            googleMap.snapshot(snapshotCallback);
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        String successTitle = "Failure";
        String successMessage = "There is a problem with Google Map. Please try again later.";
        new SingleAlertDialog(successTitle, successMessage).displayAlertMessage(this);
    }

    private void setMapImage(Bitmap mapImage) {
        final ImageView mapImageL = (ImageView) findViewById(R.id.mapImageL);
        final ImageView mapImageR = (ImageView) findViewById(R.id.mapImageR);
        Bitmap cropped = Bitmap.createBitmap(mapImage, (mapImage.getWidth()-mapImage.getHeight())/2, 0,
                mapImage.getHeight(), mapImage.getHeight());
        mapImageL.setImageBitmap(cropped);
        mapImageR.setImageBitmap(cropped);
        mapImageL.setAlpha(0.6f);
        mapImageR.setAlpha(0.6f);
    }

    private void setMapInvisible() {
        final View map = findViewById(R.id.map);
        if (map.getVisibility() == View.VISIBLE) {
            map.setVisibility(View.INVISIBLE);
        }
    }
}
