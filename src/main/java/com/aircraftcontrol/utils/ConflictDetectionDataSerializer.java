package com.aircraftcontrol.utils;

import com.aircraftcontrol.model.ConflictDetectionData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class ConflictDetectionDataSerializer implements SerializationSchema<ConflictDetectionData> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(ConflictDetectionData element) {
        try {
            return objectMapper.writeValueAsBytes(element);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing ConflictDetectionData", e);
        }
    }
} 