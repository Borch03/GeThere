package pl.edu.agh.gethere.service;

/**
 * Created by Dominik on 05.03.2017.
 */
public interface NavigationServiceCallbacks {

    void activeLeftArrow(final int distance);
    void activeUpArrow(final int distance);
    void activeDownArrow(final int distance);
    void activeRightArrow(final int distance);
}
