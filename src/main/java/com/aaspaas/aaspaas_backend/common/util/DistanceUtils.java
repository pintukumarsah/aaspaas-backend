package com.aaspaas.aaspaas_backend.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceUtils() {
    }

    public static BigDecimal distanceInKm(
            BigDecimal latitude1,
            BigDecimal longitude1,
            BigDecimal latitude2,
            BigDecimal longitude2) {

        if (latitude1 == null
                || longitude1 == null
                || latitude2 == null
                || longitude2 == null) {

            return BigDecimal.valueOf(-1);
        }

        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lon1 = Math.toRadians(longitude1.doubleValue());

        double lat2 = Math.toRadians(latitude2.doubleValue());
        double lon2 = Math.toRadians(longitude2.doubleValue());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a =
                Math.sin(deltaLat / 2)
                        * Math.sin(deltaLat / 2)
                        + Math.cos(lat1)
                        * Math.cos(lat2)
                        * Math.sin(deltaLon / 2)
                        * Math.sin(deltaLon / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        double distance =
                EARTH_RADIUS_KM * c;

        return BigDecimal
                .valueOf(distance)
                .setScale(2, RoundingMode.HALF_UP);
    }
}