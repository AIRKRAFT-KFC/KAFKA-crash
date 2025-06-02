package com.aircraftcontrol.utils;

import com.aircraftcontrol.model.AircraftData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AircraftDataDeserializer implements DeserializationSchema<AircraftData> {
    private static final Logger LOG = LoggerFactory.getLogger(AircraftDataDeserializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AircraftData deserialize(byte[] message) throws IOException {
        try {
            String jsonStr = new String(message, StandardCharsets.UTF_8);
            
            // Check if the message starts with '<' (HTML/XML) or other invalid characters
            if (jsonStr.trim().startsWith("<")) {
                LOG.warn("Received non-JSON data: {}", jsonStr.substring(0, Math.min(100, jsonStr.length())));
                return null;
            }
            
            return objectMapper.readValue(jsonStr, AircraftData.class);
        } catch (Exception e) {
            LOG.error("Failed to deserialize message: {}", new String(message, StandardCharsets.UTF_8), e);
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(AircraftData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<AircraftData> getProducedType() {
        return TypeInformation.of(AircraftData.class);
    }

    @Override
    public void deserialize(byte[] message, Collector<AircraftData> out) throws IOException {
        AircraftData data = deserialize(message);
        if (data != null) {
            out.collect(data);
        }
    }
} 