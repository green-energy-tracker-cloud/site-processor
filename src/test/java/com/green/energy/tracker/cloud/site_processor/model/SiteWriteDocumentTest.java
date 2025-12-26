package com.green.energy.tracker.cloud.site_processor.model;

import com.google.cloud.Timestamp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SiteWriteDocumentTest {

    @Test
    void builder_shouldCreateDocumentWithAllFields() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = GeoLocationWrite.builder()
            .latitude(40.7128)
            .longitude(-74.0060)
            .build();

        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-123")
            .name("Test Site")
            .userId("user-456")
            .address("123 Main St")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertNotNull(document);
        assertEquals("site-123", document.getId());
        assertEquals("Test Site", document.getName());
        assertEquals("user-456", document.getUserId());
        assertEquals("123 Main St", document.getAddress());
        assertEquals(geoLocation, document.getLocation());
        assertEquals(now, document.getCreatedAt());
        assertEquals(now, document.getUpdatedAt());
    }

    @Test
    void noArgsConstructor_shouldCreateEmptyDocument() {
        SiteWriteDocument document = new SiteWriteDocument();

        assertNotNull(document);
        assertNull(document.getId());
        assertNull(document.getName());
        assertNull(document.getUserId());
        assertNull(document.getAddress());
        assertNull(document.getLocation());
        assertNull(document.getCreatedAt());
        assertNull(document.getUpdatedAt());
    }

    @Test
    void allArgsConstructor_shouldCreateDocumentWithAllFields() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument document = new SiteWriteDocument(
            "site-789",
            "Another Site",
            "user-999",
            "456 Oak Ave",
            geoLocation,
            now,
            now
        );

        assertNotNull(document);
        assertEquals("site-789", document.getId());
        assertEquals("Another Site", document.getName());
        assertEquals("user-999", document.getUserId());
        assertEquals("456 Oak Ave", document.getAddress());
        assertEquals(geoLocation, document.getLocation());
        assertEquals(now, document.getCreatedAt());
        assertEquals(now, document.getUpdatedAt());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        SiteWriteDocument document = new SiteWriteDocument();
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(51.5074, -0.1278);

        document.setId("new-id");
        document.setName("New Name");
        document.setUserId("new-user");
        document.setAddress("New Address");
        document.setLocation(geoLocation);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        assertEquals("new-id", document.getId());
        assertEquals("New Name", document.getName());
        assertEquals("new-user", document.getUserId());
        assertEquals("New Address", document.getAddress());
        assertEquals(geoLocation, document.getLocation());
        assertEquals(now, document.getCreatedAt());
        assertEquals(now, document.getUpdatedAt());
    }

    @Test
    void equals_shouldReturnTrueForSameContent() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site Name")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site Name")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentContent() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site Name 1")
            .build();

        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-2")
            .name("Site Name 2")
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site Name")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site Name")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertEquals(doc1.hashCode(), doc2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-123")
            .name("Test Site")
            .userId("user-456")
            .address("123 Main St")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        String result = document.toString();

        assertNotNull(result);
        assertTrue(result.contains("site-123"));
        assertTrue(result.contains("Test Site"));
        assertTrue(result.contains("user-456"));
        assertTrue(result.contains("123 Main St"));
    }

    @Test
    void builder_shouldHandleNullValues() {
        SiteWriteDocument document = SiteWriteDocument.builder()
            .id(null)
            .name(null)
            .userId(null)
            .address(null)
            .location(null)
            .createdAt(null)
            .updatedAt(null)
            .build();

        assertNotNull(document);
        assertNull(document.getId());
        assertNull(document.getName());
        assertNull(document.getUserId());
        assertNull(document.getAddress());
        assertNull(document.getLocation());
        assertNull(document.getCreatedAt());
        assertNull(document.getUpdatedAt());
    }
}
