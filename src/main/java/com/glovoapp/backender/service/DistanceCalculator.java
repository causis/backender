package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Location;
import org.springframework.stereotype.Component;

import static java.lang.Math.*;

@Component
public class DistanceCalculator {

    private static final int EARTH_RADIUS = 6371;

    /**
     * Returns distance between two locations in kilometers
     * Shamelessly copied from https://github.com/jasonwinn/haversine
     */
    public double calculateDistance(Location start, Location end) {
        double deltaLat = toRadians((end.getLat() - start.getLat()));
        double deltaLong = toRadians((end.getLon() - start.getLon()));

        double startLat = toRadians(start.getLat());
        double endLat = toRadians(end.getLat());

        double a = haversin(deltaLat) + cos(startLat) * cos(endLat) * haversin(deltaLong);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
