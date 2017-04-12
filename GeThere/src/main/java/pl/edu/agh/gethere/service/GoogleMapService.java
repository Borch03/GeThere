package pl.edu.agh.gethere.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by Dominik on 08.04.2017.
 */
public class GoogleMapService extends Service {

    private final IBinder binder = new LocalBinder();
    private GoogleMapServiceCallbacks serviceCallbacks;
    boolean isDestinationAchieved;
    boolean isLoaded;

    public class LocalBinder extends Binder {
        public GoogleMapService getService() {
            return GoogleMapService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(GoogleMapServiceCallbacks serviceCallbacks) {
        this.serviceCallbacks = serviceCallbacks;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            public void run() {
                while(!isDestinationAchieved) {
                    try {
                        if (serviceCallbacks != null) {
                            if (!isLoaded) {
                                serviceCallbacks.stopLoading();
                            }
                            isLoaded = true;
                            serviceCallbacks.updateMap();
                            Thread.sleep(3000);
                        } else {
                            Thread.sleep(3000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return START_NOT_STICKY;
    }
}
