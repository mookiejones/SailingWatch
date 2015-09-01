package com.solutions.nerd.sailing.sailingwatchface;


class ConverterUtil
{
    public static final String CELSIUS_STRING    = "°C";
    public static final String FAHRENHEIT_STRING = "°F";
    public static final int    FAHRENHEIT        = 0;
    private static final int   TIME_UNIT_12      = 0;
    public static final int    TIME_UNIT_24      = 1;

    /** The number of half winds for boxing the compass. */
    private static final int NUMBER_OF_HALF_WINDS = 16;

    /** The Earth's radius, in kilometers. */
    private static final double EARTH_RADIUS_KM = 6371.0;
    /**
     * Calculates {@code a mod b} in a way that respects negative values (for example,
     * {@code mod(-1, 5) == 4}, rather than {@code -1}).
     *
     * @param a the dividend
     * @param b the divisor
     * @return {@code a mod b}
     */
    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    /**
     * Calculates {@code a mod b} in a way that respects negative values (for example,
     * {@code mod(-1, 5) == 4}, rather than {@code -1}).
     *
     * @param a the dividend
     * @param b the divisor
     * @return {@code a mod b}
     */
    public static float mod(float a, float b) {
        return (a % b + b) % b;
    }

    /**
     * Gets the relative bearing from one geographical coordinate to another.
     *
     * @param latitude1 the latitude of the source point
     * @param longitude1 the longitude of the source point
     * @param latitude2 the latitude of the destination point
     * @param longitude2 the longitude of the destination point
     * @return the relative bearing from point 1 to point 2, in degrees. The result is guaranteed
     *         to fall in the range 0-360
     */
    public static float getBearing(double latitude1, double longitude1, double latitude2,
                                   double longitude2) {
        latitude1 = Math.toRadians(latitude1);
        longitude1 = Math.toRadians(longitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude2 = Math.toRadians(longitude2);

        double dLon = longitude2 - longitude1;

        double y = Math.sin(dLon) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1)
                * Math.cos(latitude2) * Math.cos(dLon);

        double bearing = Math.atan2(y, x);
        return mod((float) Math.toDegrees(bearing), 360.0f);
    }


    /**
     * Gets the great circle distance in kilometers between two geographical points, using
     * the <a href="http://en.wikipedia.org/wiki/Haversine_formula">haversine formula</a>.
     *
     * @param latitude1 the latitude of the first point
     * @param longitude1 the longitude of the first point
     * @param latitude2 the latitude of the second point
     * @param longitude2 the longitude of the second point
     * @return the distance, in kilometers, between the two points
     */
    public static float getDistance(double latitude1, double longitude1, double latitude2,
                                    double longitude2) {
        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLon = Math.toRadians(longitude2 - longitude1);
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double sqrtHaversineLat = Math.sin(dLat / 2);
        double sqrtHaversineLon = Math.sin(dLon / 2);
        double a = sqrtHaversineLat * sqrtHaversineLat + sqrtHaversineLon * sqrtHaversineLon
                * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (EARTH_RADIUS_KM * c);
    }


    // converts km to knots
    public static double convertKMToKnots(double speed){
        return speed * 1943.844492441;
    }

    // converts km to mph
    public static double convertKMToMPH(double speed){
        return speed;
    }

    // converts to celsius
    public static int convertFahrenheitToCelsius( int fahrenheit )
    {
        return ( ( fahrenheit - 32 ) * 5 / 9 );
    }

    // converts to fahrenheit
    public static int convertCelsiusToFahrenheit( int celsius )
    {
        return ( ( celsius * 9 ) / 5 ) + 32;
    }

    public static String convertToMonth( int month )
    {
        switch ( month )
        {
            case 0:
                return "January ";
            case 1:
                return "February ";
            case 2:
                return "March ";
            case 3:
                return "April ";
            case 4:
                return "May ";
            case 5:
                return "June ";
            case 6:
                return "July ";
            case 7:
                return "August ";
            case 8:
                return "September ";
            case 9:
                return "October ";
            case 10:
                return "November ";
            default:
                return "December";
        }
    }

    public static String getDaySuffix( int monthDay )
    {
        switch ( monthDay )
        {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static int convertHour( int hour, int timeUnit )
    {
        if ( timeUnit == TIME_UNIT_12 )
        {
            int result = hour % 12;
            return ( result == 0 ) ? 12 : result;
        }
        else
        {
            return hour;
        }
    }
}
