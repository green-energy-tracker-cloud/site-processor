package com.green.energy.tracker.cloud.site_processor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperConfigTest {

    private ObjectMapperConfig objectMapperConfig;

    @BeforeEach
    void setUp() {
        objectMapperConfig = new ObjectMapperConfig();
    }

    @Test
    void objectMapper_shouldReturnConfiguredObjectMapper() {
        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        assertThat(objectMapper).isNotNull();
    }

    @Test
    void objectMapper_shouldHaveProtobufModuleRegistered() {
        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        assertThat(objectMapper.getRegisteredModuleIds())
                .as("ObjectMapper should have ProtobufModule registered")
                .contains(new ProtobufModule().getTypeId());
    }

    @Test
    void objectMapper_shouldCreateNewInstanceEachTime() {
        ObjectMapper firstCall = objectMapperConfig.objectMapper();
        ObjectMapper secondCall = objectMapperConfig.objectMapper();
        assertThat(firstCall)
                .as("Each call should create a new ObjectMapper instance")
                .isNotSameAs(secondCall);
    }
}
