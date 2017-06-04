package pl.edu.agh.gethere.connection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.utils.SingleAlertDialog;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Dominik on 03.04.2017.
 */
public class LocationProvider {

    private LocationManager locationManager;

    public Coordinates getLocation(Context context) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String title = "GPS error";
            String message = "Cannot get GPS coordinates.";
            new SingleAlertDialog(title, message).displayAlertMessage(context);
            return null;
        }
        Location location = getLastKnownLocation(context);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return new Coordinates(latitude, longitude);
    }

    public Float getBearing(Context context) {
        return getLastKnownLocation(context).getBearing();
    }

    private Location getLastKnownLocation(Context context) {
        locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
