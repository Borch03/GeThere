package pl.edu.agh.gethere.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import pl.edu.agh.gethere.R;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback {
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyDrUH6-lmzWNG5fWhcM9uNosu8BmZYH6bw";
    private LatLng camera = new LatLng(37.7849569, -122.4068855);
    private LatLng origin = new LatLng(37.7849569, -122.4068855);
    private LatLng destination = new LatLng(37.7814432, -122.4460177);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map1)).getMapAsync(this);
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .execute(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 14));

    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            googleMap.addMarker(new MarkerOptions().position(origin));
            googleMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 6, 0xFF0080FF));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        String successTitle = "Failure";
        String successMessage = "There is a problem with Google Map. Please try again later.";
        new SingleAlertDialog(successTitle, successMessage).displayAlertMessage(this);
    }
}
