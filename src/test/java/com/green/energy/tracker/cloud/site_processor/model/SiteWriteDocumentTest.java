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

    // Test aggiuntivi per toString() generato da @Data
    @Test
    void toString_shouldContainClassName() {
        SiteWriteDocument document = new SiteWriteDocument();

        String result = document.toString();

        assertNotNull(result);
        assertTrue(result.contains("SiteWriteDocument"));
    }

    @Test
    void toString_shouldNotReturnNull() {
        SiteWriteDocument document = new SiteWriteDocument();

        assertNotNull(document.toString());
    }

    @Test
    void toString_shouldHandleNullFields() {
        SiteWriteDocument document = SiteWriteDocument.builder().build();

        String result = document.toString();

        assertNotNull(result);
        assertTrue(result.contains("SiteWriteDocument"));
    }

    // Test aggiuntivi per equals() - casi edge
    @Test
    void equals_shouldReturnTrueForSameObject() {
        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .build();

        assertEquals(document, document);
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .build();

        assertNotEquals(null, document);
    }

    @Test
    void equals_shouldReturnFalseForDifferentType() {
        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .build();
        String differentType = "Not a SiteWriteDocument";

        assertNotEquals(document, differentType);
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-2")
            .name("Test Site")
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentName() {
        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site A")
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site B")
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentUserId() {
        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .userId("user-1")
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .userId("user-2")
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentAddress() {
        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .address("Address 1")
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .address("Address 2")
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentLocation() {
        GeoLocationWrite loc1 = new GeoLocationWrite(40.7128, -74.0060);
        GeoLocationWrite loc2 = new GeoLocationWrite(51.5074, -0.1278);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .location(loc1)
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .location(loc2)
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentCreatedAt() {
        Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
        Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .createdAt(time1)
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .createdAt(time2)
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldReturnFalseForDifferentUpdatedAt() {
        Timestamp time1 = Timestamp.ofTimeSecondsAndNanos(1000, 0);
        Timestamp time2 = Timestamp.ofTimeSecondsAndNanos(2000, 0);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .updatedAt(time1)
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .updatedAt(time2)
            .build();

        assertNotEquals(doc1, doc2);
    }

    @Test
    void equals_shouldBeSymmetric() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertTrue(doc1.equals(doc2) && doc2.equals(doc1));
    }

    @Test
    void equals_shouldBeTransitive() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        SiteWriteDocument doc3 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertTrue(doc1.equals(doc2));
        assertTrue(doc2.equals(doc3));
        assertTrue(doc1.equals(doc3));
    }

    // Test aggiuntivi per hashCode()
    @Test
    void hashCode_shouldBeDifferentForDifferentObjects() {
        SiteWriteDocument doc1 = SiteWriteDocument.builder()
            .id("site-1")
            .name("Site A")
            .build();
        SiteWriteDocument doc2 = SiteWriteDocument.builder()
            .id("site-2")
            .name("Site B")
            .build();

        assertNotEquals(doc1.hashCode(), doc2.hashCode());
    }

    @Test
    void hashCode_shouldBeConsistentOnMultipleCalls() {
        Timestamp now = Timestamp.now();
        GeoLocationWrite geoLocation = new GeoLocationWrite(40.7128, -74.0060);

        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-1")
            .name("Test Site")
            .userId("user-1")
            .address("Address 1")
            .location(geoLocation)
            .createdAt(now)
            .updatedAt(now)
            .build();

        int firstHashCode = document.hashCode();
        int secondHashCode = document.hashCode();

        assertEquals(firstHashCode, secondHashCode);
    }

    @Test
    void hashCode_shouldChangeWhenObjectIsModified() {
        SiteWriteDocument document = SiteWriteDocument.builder()
            .id("site-1")
            .name("Original Name")
            .build();

        int originalHashCode = document.hashCode();

        document.setName("Modified Name");

        int newHashCode = document.hashCode();
        assertNotEquals(originalHashCode, newHashCode);
    }

    @Test
    void hashCode_shouldHandleNullFields() {
        SiteWriteDocument document = new SiteWriteDocument();

        assertDoesNotThrow(() -> document.hashCode());
    }

    // Test aggiuntivi per i setter individuali
    @Test
    void setId_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();

        document.setId("new-id");

        assertEquals("new-id", document.getId());
    }

    @Test
    void setName_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();

        document.setName("new-name");

        assertEquals("new-name", document.getName());
    }

    @Test
    void setUserId_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();

        document.setUserId("new-user-id");

        assertEquals("new-user-id", document.getUserId());
    }

    @Test
    void setAddress_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();

        document.setAddress("new-address");

        assertEquals("new-address", document.getAddress());
    }

    @Test
    void setLocation_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();
        GeoLocationWrite newLocation = new GeoLocationWrite(45.0, 9.0);

        document.setLocation(newLocation);

        assertEquals(newLocation, document.getLocation());
    }

    @Test
    void setCreatedAt_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();
        Timestamp newTime = Timestamp.now();

        document.setCreatedAt(newTime);

        assertEquals(newTime, document.getCreatedAt());
    }

    @Test
    void setUpdatedAt_shouldUpdateValue() {
        SiteWriteDocument document = new SiteWriteDocument();
        Timestamp newTime = Timestamp.now();

        document.setUpdatedAt(newTime);

        assertEquals(newTime, document.getUpdatedAt());
    }
}
