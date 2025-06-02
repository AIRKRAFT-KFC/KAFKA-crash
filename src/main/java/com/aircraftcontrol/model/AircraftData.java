package com.aircraftcontrol.model;

import java.io.Serializable;

public class AircraftData implements Serializable {
    private static final long serialVersionUID = 1L;
    private Enhanced enhanced;
    private String messageType;
    private FlightPlan flightPlan;
    private Record record;
    private String source;
    private Track track;
    private long timestamp;

    // Enhanced 클래스
    public static class Enhanced implements Serializable {
        private static final long serialVersionUID = 1L;
        private String departureAirport;
        private String destinationAirport;

        public String getDepartureAirport() { return departureAirport; }
        public void setDepartureAirport(String departureAirport) { this.departureAirport = departureAirport; }
        public String getDestinationAirport() { return destinationAirport; }
        public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }
    }

    // FlightPlan 클래스
    public static class FlightPlan implements Serializable {
        private static final long serialVersionUID = 1L;
        private String aircraftType;
        private int assignedAltitude;
        private String entryFix;
        private int requestedAltitude;
        private String flightRules;
        private String callSign;
        private String airport;
        private String exitFix;

        public String getAircraftType() { return aircraftType; }
        public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
        public int getAssignedAltitude() { return assignedAltitude; }
        public void setAssignedAltitude(int assignedAltitude) { this.assignedAltitude = assignedAltitude; }
        public String getEntryFix() { return entryFix; }
        public void setEntryFix(String entryFix) { this.entryFix = entryFix; }
        public int getRequestedAltitude() { return requestedAltitude; }
        public void setRequestedAltitude(int requestedAltitude) { this.requestedAltitude = requestedAltitude; }
        public String getFlightRules() { return flightRules; }
        public void setFlightRules(String flightRules) { this.flightRules = flightRules; }
        public String getCallSign() { return callSign; }
        public void setCallSign(String callSign) { this.callSign = callSign; }
        public String getAirport() { return airport; }
        public void setAirport(String airport) { this.airport = airport; }
        public String getExitFix() { return exitFix; }
        public void setExitFix(String exitFix) { this.exitFix = exitFix; }
    }

    // Record 클래스
    public static class Record implements Serializable {
        private static final long serialVersionUID = 1L;
        private String seqNum;
        private String source;
        private String type;

        public String getSeqNum() { return seqNum; }
        public void setSeqNum(String seqNum) { this.seqNum = seqNum; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Track 클래스
    public static class Track implements Serializable {
        private static final long serialVersionUID = 1L;
        private int altitude;
        private String beaconCode;
        private String trackNum;
        private int verticalVelocity;
        private double latitude;
        private int velocityX;
        private String time;
        private int velocityY;
        private String status;
        private double longitude;

        public int getAltitude() { return altitude; }
        public void setAltitude(int altitude) { this.altitude = altitude; }
        public String getBeaconCode() { return beaconCode; }
        public void setBeaconCode(String beaconCode) { this.beaconCode = beaconCode; }
        public String getTrackNum() { return trackNum; }
        public void setTrackNum(String trackNum) { this.trackNum = trackNum; }
        public int getVerticalVelocity() { return verticalVelocity; }
        public void setVerticalVelocity(int verticalVelocity) { this.verticalVelocity = verticalVelocity; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public int getVelocityX() { return velocityX; }
        public void setVelocityX(int velocityX) { this.velocityX = velocityX; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public int getVelocityY() { return velocityY; }
        public void setVelocityY(int velocityY) { this.velocityY = velocityY; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }

    // Getters and Setters
    public Enhanced getEnhanced() { return enhanced; }
    public void setEnhanced(Enhanced enhanced) { this.enhanced = enhanced; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public FlightPlan getFlightPlan() { return flightPlan; }
    public void setFlightPlan(FlightPlan flightPlan) { this.flightPlan = flightPlan; }
    public Record getRecord() { return record; }
    public void setRecord(Record record) { this.record = record; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
} 