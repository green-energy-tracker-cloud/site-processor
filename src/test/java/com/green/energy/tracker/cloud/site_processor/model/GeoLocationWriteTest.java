package com.green.energy.tracker.cloud.site_processor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoLocationWriteTest {

    @Test
    void builder_shouldCreateGeoLocationWithCoordinates() {
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(40.7128)
            .longitude(-74.0060)
            .build();

        assertNotNull(geoLocation);
        assertEquals(40.7128, geoLocation.getLatitude());
        assertEquals(-74.0060, geoLocation.getLongitude());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyGeoLocation() {
        GeoLocationWrite geoLocation = new GeoLocationWrite();

        assertNotNull(geoLocation);
        assertEquals(0.0, geoLocation.getLatitude());
        assertEquals(0.0, geoLocation.getLongitude());
    }

    @Test
    void allArgsConstructor_shouldCreateGeoLocationWithCoordinates() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(51.5074, -0.1278);

        assertNotNull(geoLocation);
        assertEquals(51.5074, geoLocation.getLatitude());
        assertEquals(-0.1278, geoLocation.getLongitude());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        GeoLocationWrite geoLocation = new GeoLocationWrite();

        geoLocation.setLatitude(35.6762);
        geoLocation.setLongitude(139.6503);

        assertEquals(35.6762, geoLocation.getLatitude());
        assertEquals(139.6503, geoLocation.getLongitude());
    }

    @Test
    void builder_shouldHandleNegativeCoordinates() {
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(-33.8688)
            .longitude(-151.2093)
            .build();

        assertEquals(-33.8688, geoLocation.getLatitude());
        assertEquals(-151.2093, geoLocation.getLongitude());
    }

    @Test
    void builder_shouldHandleZeroCoordinates() {
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(0.0)
            .longitude(0.0)
            .build();

        assertEquals(0.0, geoLocation.getLatitude());
        assertEquals(0.0, geoLocation.getLongitude());
    }

    @Test
    void builder_shouldHandleExtremeCoordinates() {
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(90.0)
            .longitude(180.0)
            .build();

        assertEquals(90.0, geoLocation.getLatitude());
        assertEquals(180.0, geoLocation.getLongitude());
    }

    @Test
    void equals_shouldReturnTrueForSameCoordinates() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(40.7128, -74.0060);

        assertEquals(geo1, geo2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCoordinates() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(51.5074, -0.1278);

        assertNotEquals(geo1, geo2);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(40.7128, -74.0060);

        assertEquals(geo1.hashCode(), geo2.hashCode());
    }


    @Test
    void builder_shouldHandleVeryPreciseCoordinates() {
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(40.71280123456789)
            .longitude(-74.00600987654321)
            .build();

        assertEquals(40.71280123456789, geoLocation.getLatitude());
        assertEquals(-74.00600987654321, geoLocation.getLongitude());
    }

    @Test
    void setLatitude_shouldUpdateValue() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(0.0, 0.0);

        geoLocation.setLatitude(45.5);

        assertEquals(45.5, geoLocation.getLatitude());
    }

    @Test
    void setLongitude_shouldUpdateValue() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(0.0, 0.0);

        geoLocation.setLongitude(-122.5);

        assertEquals(-122.5, geoLocation.getLongitude());
    }

    @Test
    void toString_shouldContainLatitudeAndLongitude() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        String result = geoLocation.toString();

        assertNotNull(result);
        assertTrue(result.contains("GeoLocationWrite"));
        assertTrue(result.contains("latitude=40.7128"));
        assertTrue(result.contains("longitude=-74.006"));
    }

    @Test
    void toString_shouldNotReturnNull() {
        GeoLocationWrite geoLocation = new GeoLocationWrite();

        assertNotNull(geoLocation.toString());
    }

    @Test
    void equals_shouldReturnTrueForSameObject() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        assertEquals(geoLocation, geoLocation);
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        assertNotEquals(null, geoLocation);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);
        String differentType = "Not a GeoLocationWrite";

        assertNotEquals(differentType, geoLocation);
    }

    @Test
    void equals_shouldBeSymmetric() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(40.7128, -74.0060);

        assertTrue(geo1.equals(geo2) && geo2.equals(geo1));
    }

    @Test
    void equals_shouldBeTransitive() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo3 = new GeoLocationWrite(40.7128, -74.0060);

        assertEquals(geo1, geo2);
        assertEquals(geo2, geo3);
        assertEquals(geo1, geo3);
    }

    @Test
    void hashCode_shouldBeDifferentForDifferentObjects() {
        GeoLocationWrite geo1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite geo2 = new GeoLocationWrite(51.5074, -0.1278);

        assertNotEquals(geo1.hashCode(), geo2.hashCode());
    }

    @Test
    void hashCode_shouldBeConsistentOnMultipleCalls() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        int firstHashCode = geoLocation.hashCode();
        int secondHashCode = geoLocation.hashCode();

        assertEquals(firstHashCode, secondHashCode);
    }

    @Test
    void hashCode_shouldChangeWhenObjectIsModified() {
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);
        int originalHashCode = geoLocation.hashCode();

        geoLocation.setLatitude(51.5074);

        int newHashCode = geoLocation.hashCode();
        assertNotEquals(originalHashCode, newHashCode);
    }
}
