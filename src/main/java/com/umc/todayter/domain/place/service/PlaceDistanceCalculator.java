package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.entity.Place;
import org.springframework.stereotype.Component;

@Component
public class PlaceDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public Double distanceKm(Double latitude, Double longitude, Place place) {
        if (latitude == null || longitude == null) {
            return null;
        }

        double latitudeDistance = Math.toRadians(place.getLatitude() - latitude);
        double longitudeDistance = Math.toRadians(place.getLongitude() - longitude);
        double requestLatitude = Math.toRadians(latitude);
        double placeLatitude = Math.toRadians(place.getLatitude());

        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(requestLatitude) * Math.cos(placeLatitude)
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_KM * c * 10.0) / 10.0;
    }
}
