package com.green.energy.tracker.cloud.site_processor.model;

import com.google.cloud.Timestamp;
import com.green.energy.tracker.cloud.sitebff.web.model.SiteResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SiteMapperTest {

    private SiteMapper siteMapper;

    @BeforeEach
    void setUp() {
        siteMapper = Mappers.getMapper(SiteMapper.class);
    }


    @Test
    void toDto_withNullDocument_shouldReturnNull() {
        SiteResponseDto result = siteMapper.toDto(null);
        assertNull(result);
    }

    @Test
    void timestampToOffsetDateTime_shouldConvertCorrectly() {
        long seconds = 1609459200L;
        int nanos = 123456789;
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(seconds, nanos);

        OffsetDateTime result = siteMapper.timestampToOffsetDateTime(timestamp);

        assertNotNull(result);
        assertEquals(seconds, result.toEpochSecond());
        assertEquals(nanos, result.getNano());
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    @Test
    void timestampToOffsetDateTime_withNullTimestamp_shouldReturnNull() {
        OffsetDateTime result = siteMapper.timestampToOffsetDateTime(null);
        assertNull(result);
    }

    @Test
    void timestampToOffsetDateTime_withZeroTimestamp_shouldConvertCorrectly() {
        Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(0, 0);

        OffsetDateTime result = siteMapper.timestampToOffsetDateTime(timestamp);

        assertNotNull(result);
        assertEquals(0, result.toEpochSecond());
        assertEquals(0, result.getNano());
    }

    @Test
    void offsetDateTimeToTimestamp_shouldConvertCorrectly() {
        Instant instant = Instant.ofEpochSecond(1609459200L, 123456789);
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);

        Timestamp result = siteMapper.offsetDateTimeToTimestamp(offsetDateTime);

        assertNotNull(result);
        assertEquals(1609459200L, result.getSeconds());
        assertEquals(123456789, result.getNanos());
    }

    @Test
    void offsetDateTimeToTimestamp_withNullOffsetDateTime_shouldReturnNull() {
        Timestamp result = siteMapper.offsetDateTimeToTimestamp(null);
        assertNull(result);
    }

    @Test
    void offsetDateTimeToTimestamp_withDifferentTimezone_shouldConvertToUTC() {
        Instant instant = Instant.ofEpochSecond(1609459200L);
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, ZoneOffset.ofHours(5));

        Timestamp result = siteMapper.offsetDateTimeToTimestamp(offsetDateTime);

        assertNotNull(result);
        assertEquals(1609459200L, result.getSeconds());
    }

    @Test
    void stringToUuid_shouldConvertValidUuidString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";

        UUID result = siteMapper.stringToUuid(uuidString);

        assertNotNull(result);
        assertEquals(uuidString, result.toString());
    }

    @Test
    void stringToUuid_withNullString_shouldReturnNull() {
        UUID result = siteMapper.stringToUuid(null);
        assertNull(result);
    }

    @Test
    void stringToUuid_withInvalidString_shouldThrowException() {
        String invalidUuid = "invalid-uuid";

        assertThrows(IllegalArgumentException.class, () -> {
            siteMapper.stringToUuid(invalidUuid);
        });
    }

    @Test
    void uuidToString_shouldConvertValidUuid() {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        String result = siteMapper.uuidToString(uuid);

        assertNotNull(result);
        assertEquals("123e4567-e89b-12d3-a456-426614174000", result);
    }

    @Test
    void uuidToString_withNullUuid_shouldReturnNull() {
        String result = siteMapper.uuidToString(null);
        assertNull(result);
    }

    @Test
    void uuidToString_withRandomUuid_shouldReturnValidString() {
        UUID uuid = UUID.randomUUID();

        String result = siteMapper.uuidToString(uuid);

        assertNotNull(result);
        assertEquals(uuid.toString(), result);
    }

    @Test
    void roundTripConversion_timestampToOffsetDateTimeAndBack_shouldPreserveValue() {
        Timestamp originalTimestamp = Timestamp.ofTimeSecondsAndNanos(1609459200L, 123456789);

        OffsetDateTime offsetDateTime = siteMapper.timestampToOffsetDateTime(originalTimestamp);
        Timestamp resultTimestamp = siteMapper.offsetDateTimeToTimestamp(offsetDateTime);

        assertNotNull(resultTimestamp);
        assertEquals(originalTimestamp.getSeconds(), resultTimestamp.getSeconds());
        assertEquals(originalTimestamp.getNanos(), resultTimestamp.getNanos());
    }

    @Test
    void roundTripConversion_uuidToStringAndBack_shouldPreserveValue() {
        UUID originalUuid = UUID.randomUUID();

        String uuidString = siteMapper.uuidToString(originalUuid);
        UUID resultUuid = siteMapper.stringToUuid(uuidString);

        assertNotNull(resultUuid);
        assertEquals(originalUuid, resultUuid);
    }
}
