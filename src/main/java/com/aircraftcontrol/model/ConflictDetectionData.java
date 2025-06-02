package com.aircraftcontrol.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConflictDetectionData {
    
    @JsonProperty("messageType")
    private String messageType;
    
    @JsonProperty("conflictDetection")
    private ConflictDetection conflictDetection;

    public ConflictDetectionData() {
        this.messageType = "TAIS";
    }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    
    public ConflictDetection getConflictDetection() { return conflictDetection; }
    public void setConflictDetection(ConflictDetection conflictDetection) { this.conflictDetection = conflictDetection; }

    public static class ConflictDetection {
        @JsonProperty("timestamp")
        private long timestamp;
        
        @JsonProperty("source")
        private String source;
        
        @JsonProperty("aircraft1")
        private AircraftData aircraft1;
        
        @JsonProperty("aircraft2")
        private AircraftData aircraft2;
        
        @JsonProperty("record")
        private AircraftData.Record record;

        public ConflictDetection() {
            this.timestamp = System.currentTimeMillis();
        }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public AircraftData getAircraft1() { return aircraft1; }
        public void setAircraft1(AircraftData aircraft1) { this.aircraft1 = aircraft1; }
        
        public AircraftData getAircraft2() { return aircraft2; }
        public void setAircraft2(AircraftData aircraft2) { this.aircraft2 = aircraft2; }
        
        public AircraftData.Record getRecord() { return record; }
        public void setRecord(AircraftData.Record record) { this.record = record; }
    }
}