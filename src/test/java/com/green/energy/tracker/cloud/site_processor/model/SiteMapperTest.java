package com.green.energy.tracker.cloud.site_processor.model;

import com.green.energy.tracker.cloud.common.v1.GeoLocation;
import com.green.energy.tracker.cloud.site.v1.Site;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SiteMapperTest {

    private SiteMapper siteMapper;

    @BeforeEach
    void setUp() {
        siteMapper = Mappers.getMapper(SiteMapper.class);
    }

    @Test
    void updateDoc_shouldUpdateAllFieldsExceptIgnored() {
        Site site = Site.newBuilder()
                .setId("new-site-id")
                .setName("Updated Name")
                .setUserId("updated-user-123")
                .setAddress("Updated Address")
                .setLocation(GeoLocation.newBuilder()
                        .setLatitude(45.0)
                        .setLongitude(-75.0)
                        .build())
                .build();

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("original-id")
                .name("Original Name")
                .userId("original-user")
                .address("Original Address")
                .location(GeoLocationWrite.builder()
                        .latitude(40.0)
                        .longitude(-74.0)
                        .build())
                .createdAt(new Date(1000000))
                .updatedAt(new Date(2000000))
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertNotNull(result);
        assertEquals("original-id", result.getId()); // Should NOT be updated
        assertEquals("Updated Name", result.getName());
        assertEquals("updated-user-123", result.getUserId());
        assertEquals("Updated Address", result.getAddress());
        assertNotNull(result.getLocation());
        assertEquals(45.0, result.getLocation().getLatitude());
        assertEquals(-75.0, result.getLocation().getLongitude());
        assertEquals(new Date(1000000), result.getCreatedAt()); // Should NOT be updated
        assertEquals(new Date(2000000), result.getUpdatedAt()); // Should NOT be updated
    }

    @Test
    void updateDoc_shouldHandleLocationUpdate() {
        Site site = Site.newBuilder()
                .setId("site-123")
                .setName("Test Site")
                .setUserId("user-456")
                .setAddress("Test Address")
                .setLocation(GeoLocation.newBuilder()
                        .setLatitude(50.5)
                        .setLongitude(-80.5)
                        .build())
                .build();

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("site-123")
                .name("Old Name")
                .userId("old-user")
                .address("Old Address")
                .location(GeoLocationWrite.builder()
                        .latitude(30.0)
                        .longitude(-70.0)
                        .build())
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertNotNull(result);
        assertNotNull(result.getLocation());
        assertEquals(50.5, result.getLocation().getLatitude());
        assertEquals(-80.5, result.getLocation().getLongitude());
    }

    @Test
    void updateDoc_shouldCreateLocationIfNotExists() {
        Site site = Site.newBuilder()
                .setId("site-123")
                .setName("Test Site")
                .setUserId("user-456")
                .setAddress("Test Address")
                .setLocation(GeoLocation.newBuilder()
                        .setLatitude(35.0)
                        .setLongitude(-85.0)
                        .build())
                .build();

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("site-123")
                .name("Old Name")
                .userId("old-user")
                .address("Old Address")
                .location(null)
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertNotNull(result);
        assertNotNull(result.getLocation());
        assertEquals(35.0, result.getLocation().getLatitude());
        assertEquals(-85.0, result.getLocation().getLongitude());
    }

    @Test
    void updateDoc_shouldSetLocationToNullWhenSiteHasNoLocation() {
        Site site = Site.newBuilder()
                .setId("site-123")
                .setName("Test Site")
                .setUserId("user-456")
                .setAddress("Test Address")
                .build(); // No location set

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("site-123")
                .name("Old Name")
                .userId("old-user")
                .address("Old Address")
                .location(GeoLocationWrite.builder()
                        .latitude(30.0)
                        .longitude(-70.0)
                        .build())
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertNotNull(result);
        assertNull(result.getLocation());
    }

    @Test
    void updateDoc_withNullSite_shouldReturnDocument() {
        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("site-123")
                .name("Name")
                .userId("user-456")
                .address("Address")
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(null, existingDoc);

        assertNotNull(result);
        assertEquals(existingDoc, result);
    }

    @Test
    void updateDoc_shouldPreserveTimestampFields() {
        Date createdDate = new Date(System.currentTimeMillis() - 100000);
        Date updatedDate = new Date(System.currentTimeMillis() - 50000);

        Site site = Site.newBuilder()
                .setId("site-123")
                .setName("New Name")
                .setUserId("new-user")
                .setAddress("New Address")
                .build();

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("original-id")
                .name("Original Name")
                .userId("original-user")
                .address("Original Address")
                .createdAt(createdDate)
                .updatedAt(updatedDate)
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertNotNull(result);
        assertSame(createdDate, result.getCreatedAt());
        assertSame(updatedDate, result.getUpdatedAt());
    }

    @Test
    void updateDoc_shouldReturnSameDocumentInstance() {
        Site site = Site.newBuilder()
                .setId("site-123")
                .setName("Name")
                .setUserId("user-456")
                .setAddress("Address")
                .build();

        SiteWriteDocument existingDoc = SiteWriteDocument.builder()
                .id("site-123")
                .name("Old Name")
                .userId("old-user")
                .address("Old Address")
                .build();

        SiteWriteDocument result = siteMapper.updateDoc(site, existingDoc);

        assertSame(existingDoc, result); // @MappingTarget should return the same instance
    }
}
