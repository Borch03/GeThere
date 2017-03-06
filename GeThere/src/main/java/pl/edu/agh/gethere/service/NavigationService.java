package pl.edu.agh.gethere.service;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

/**
 * Created by Dominik on 05.03.2017.
 */
public class NavigationService extends Service {

    private final IBinder binder = new LocalBinder();
    private NavigationServiceCallbacks serviceCallbacks;

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
                while(true) {
                    try {
                        if (serviceCallbacks != null) {
                            Random random = new Random();
                            int randomInt = random.nextInt(4) + 1;
                            switch (randomInt) {
                                case 1:
                                    serviceCallbacks.activeLeftArrow(randomInt);
                                    Thread.sleep(3000);
                                    break;
                                case 2:
                                    serviceCallbacks.activeUpArrow(randomInt);
                                    Thread.sleep(3000);
                                    break;
                                case 3:
                                    serviceCallbacks.activeDownArrow(randomInt);
                                    Thread.sleep(3000);
                                    break;
                                case 4:
                                    serviceCallbacks.activeRightArrow(randomInt);
                                    Thread.sleep(3000);
                                    break;
                            }
                        } else {
                            Thread.sleep(3000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        return START_NOT_STICKY;
    }
}
