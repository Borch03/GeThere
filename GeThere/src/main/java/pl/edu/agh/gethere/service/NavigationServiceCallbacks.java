package pl.edu.agh.gethere.service;

import pl.edu.agh.gethere.model.Coordinates;

/**
 * Created by Dominik on 05.03.2017.
 */
public interface NavigationServiceCallbacks {

    Coordinates getOrigin();
    Coordinates getDestination();
    void activeLeftArrow(final String maneuverDistance);
    void activeUpArrow(final String maneuverDistance);
    void activeRightArrow(final String maneuverDistance);
    void activeFinish(final String maneuverDistance);
    void activeNullArrow(final String maneuverDistance);
    void setTotalDistance(final String totalDistance);
}
