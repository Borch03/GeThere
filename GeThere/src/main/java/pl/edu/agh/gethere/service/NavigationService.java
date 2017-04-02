package pl.edu.agh.gethere.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.edu.agh.gethere.connection.HttpResponseReceiver;
import pl.edu.agh.gethere.model.Coordinates;
import pl.edu.agh.gethere.utils.ArrowDirection;

import java.util.HashMap;

/**
 * Created by Dominik on 05.03.2017.
 */
public class NavigationService extends Service {

    private static final String DIRECTION_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String ORIGIN = "origin=";
    private static final String DESTINATION = "&destination=";
    private static final String MODE = "&mode=walking";
    private static final String KEY = "&key=AIzaSyDrUH6-lmzWNG5fWhcM9uNosu8BmZYH6bw";

    private static final double DESTINATION_ACHIEVED_RADIUS = 15;

    private final IBinder binder = new LocalBinder();
    private NavigationServiceCallbacks serviceCallbacks;
    private HttpResponseReceiver httpResponseReceiver;
    private Coordinates origin;
    private Coordinates destination;
    private boolean isDestinationAchieved;

    public class LocalBinder extends Binder {
        public NavigationService getService() {
            return NavigationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(NavigationServiceCallbacks serviceCallbacks) {
        this.serviceCallbacks = serviceCallbacks;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            public void run() {
                while(!isDestinationAchieved) {
                    try {
                        if (serviceCallbacks != null) {
                            origin = serviceCallbacks.getOrigin();
                            destination = serviceCallbacks.getDestination();
                            String googleDirectionUrl = buildGoogleDirectionUrl();
                            JSONObject directionInfo = getDirectionInfo(googleDirectionUrl);

                            String totalDistance = getTotalDistanceText(directionInfo);
                            String maneuverDistance = getManuverDistance(directionInfo);
                            ArrowDirection arrowDirection = getArrowDirection(getManeuverDescription(directionInfo));

                            serviceCallbacks.setTotalDistance(totalDistance);
                            switch (arrowDirection) {
                                case LEFT:
                                    serviceCallbacks.activeLeftArrow(maneuverDistance);
                                    Thread.sleep(1000);
                                    break;
                                case STRAIGHT:
                                    serviceCallbacks.activeUpArrow(maneuverDistance);
                                    Thread.sleep(1000);
                                    break;
                                case RIGHT:
                                    serviceCallbacks.activeRightArrow(maneuverDistance);
                                    Thread.sleep(1000);
                                    break;
                                case NULL:
                                    serviceCallbacks.activeNullArrow(maneuverDistance);
                                    Thread.sleep(1000);
                                    break;
                            }
                            if (getTotalDistanceValue(directionInfo) < DESTINATION_ACHIEVED_RADIUS) {
                                isDestinationAchieved = true;
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        return START_NOT_STICKY;
    }

    private String buildGoogleDirectionUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(DIRECTION_API_URL);
        sb.append(ORIGIN);
        sb.append(origin.toString());
        sb.append(DESTINATION);
        sb.append(destination.toString());
        sb.append(MODE);
        sb.append(KEY);

        return sb.toString();
    }

    private JSONObject getDirectionInfo(String url) {
        HashMap<String,String> directionInfo = new HashMap<>();
        try {
            httpResponseReceiver = new HttpResponseReceiver(url);
            String response = httpResponseReceiver.execute().get();
            return new JSONObject(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getTotalDistanceText(JSONObject directionInfo) throws JSONException {
        return directionInfo.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                .getJSONObject(0).getJSONObject("distance").getString("text");
    }

    private int getTotalDistanceValue(JSONObject directionInfo) throws JSONException {
        return (int)directionInfo.getJSONArray("routes").getJSONObject(0)
                .getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");
    }

    private String getManuverDistance(JSONObject directionInfo) throws JSONException {
        JSONArray steps = directionInfo.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                .getJSONObject(0).getJSONArray("steps");
        return steps.getJSONObject(0).getJSONObject("distance").getString("text");
    }

    private String getManeuverDescription(JSONObject directionInfo) throws JSONException {
        JSONObject firstStep = directionInfo.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                .getJSONObject(0).getJSONArray("steps").getJSONObject(0);
        if (firstStep.has("maneuver")) {
            return firstStep.getString("maneuver");
        } else {
            return "null";
        }
    }

    private ArrowDirection getArrowDirection(@Nullable String maneuverDescription) {
        if (maneuverDescription.contains("left")) {
            return ArrowDirection.LEFT;
        } else if (maneuverDescription.contains("straight")) {
            return ArrowDirection.STRAIGHT;
        } else if (maneuverDescription.contains("right")) {
            return ArrowDirection.RIGHT;
        } else {
            return ArrowDirection.NULL;
        }
    }
}
