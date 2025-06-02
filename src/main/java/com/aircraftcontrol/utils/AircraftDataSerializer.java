package com.aircraftcontrol.utils;

import com.aircraftcontrol.model.AircraftData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AircraftDataSerializer implements SerializationSchema<AircraftData> {
    private static final Logger LOG = LoggerFactory.getLogger(AircraftDataSerializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(AircraftData element) {
        try {
            return objectMapper.writeValueAsBytes(element);
        } catch (Exception e) {
            LOG.error("Error serializing message: {}", element, e);
            throw new RuntimeException("Error serializing AircraftData", e);
        }
    }
} 