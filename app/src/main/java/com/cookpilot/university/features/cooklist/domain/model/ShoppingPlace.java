package com.cookpilot.university.features.cooklist.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class ShoppingPlace {
    private static final double EARTH_METERS = 6371000;
    public final int profile;
    public final double latitude;
    public final double longitude;
    public final double distanceMeters;

    private ShoppingPlace(int profile, double latitude, double longitude, double distance) {
        this.profile = profile;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceMeters = distance;
    }

    public static List<ShoppingPlace> generate(double latitude, double longitude, Random random) {
        List<ShoppingPlace> places = new ArrayList<>();
        double rotation = random.nextDouble() * Math.PI * 2;
        double lat = Math.toRadians(latitude);
        double lng = Math.toRadians(longitude);
        for (int i = 0; i < 5; i++) {
            double angle = rotation + i * Math.PI * 2 / 5 + (random.nextDouble() - .5) * .3;
            double distance = 300 + random.nextDouble() * 1200;
            double arc = distance / EARTH_METERS;
            double targetLat = Math.asin(Math.sin(lat) * Math.cos(arc)
                    + Math.cos(lat) * Math.sin(arc) * Math.cos(angle));
            double targetLng = lng + Math.atan2(Math.sin(angle) * Math.sin(arc) * Math.cos(lat),
                    Math.cos(arc) - Math.sin(lat) * Math.sin(targetLat));
            places.add(new ShoppingPlace(i, Math.toDegrees(targetLat),
                    (Math.toDegrees(targetLng) + 540) % 360 - 180, distance));
        }
        places.sort(Comparator.comparingDouble(place -> place.distanceMeters));
        return places;
    }
}
