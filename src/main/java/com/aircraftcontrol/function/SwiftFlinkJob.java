package com.aircraftcontrol.function;

import com.aircraftcontrol.model.AircraftData;
import com.aircraftcontrol.model.ConflictDetectionData;
import com.aircraftcontrol.utils.AircraftDataDeserializer;
import com.aircraftcontrol.utils.AircraftDataSerializer;
import com.aircraftcontrol.utils.ConflictDetectionDataSerializer;
import com.aircraftcontrol.utils.GridSystem;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public class SwiftFlinkJob implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bootstrapServers;
    private final String inputTopic;
    private final String outputTopic;
    
    // 충돌 감지 기준값
    private static final double TA_DISTANCE = 750.0; // 1km
    private static final double RA_DISTANCE = 500.0;  // 500m
    private static final double RA_ALTITUDE = 100.0;  // 100ft
    private static final double MIN_ALTITUDE = 100.0; // 100ft

    public SwiftFlinkJob(String bootstrapServers, String inputTopic, String outputTopic) {
        this.bootstrapServers = bootstrapServers;
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
    }

    public void run() throws Exception {
        // Flink 설정
        Configuration configuration = new Configuration();
        configuration.setString("rest.address", "localhost");
        configuration.setInteger("rest.port", 8081);
        
        // 상태 백엔드 설정
        configuration.setString("state.backend", "hashmap");
        configuration.setString("state.checkpoints.dir", "file:///tmp/flink-checkpoints");
        configuration.setBoolean("state.backend.incremental", true);
        
        // 체크포인트 설정
        configuration.setLong("execution.checkpointing.interval", 10000);
        configuration.setString("execution.checkpointing.mode", "EXACTLY_ONCE");
        
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        
        // Kafka 소스 설정
        KafkaSource<AircraftData> source = KafkaSource.<AircraftData>builder()
            .setBootstrapServers(bootstrapServers)
            .setTopics(inputTopic)
            .setStartingOffsets(OffsetsInitializer.latest())
            .setValueOnlyDeserializer(new AircraftDataDeserializer())
            .setProperties(createKafkaProperties())
            .build();

        // 스트림 처리
        DataStream<AircraftData> inputStream = env.fromSource(source, 
            WatermarkStrategy.<AircraftData>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((event, timestamp) -> event.getTimestamp()),
            "Kafka Source")
            .filter(data -> data != null && data.getTrack() != null)
            .keyBy(data -> data.getFlightPlan().getCallSign())
            .process(new ProcessFunction<AircraftData, AircraftData>() {
                private Map<String, Long> lastProcessedTime = new HashMap<>();
                private static final long PROCESS_INTERVAL = 1000; // 1초

                @Override
                public void processElement(AircraftData value, Context ctx, Collector<AircraftData> out) throws Exception {
                    String callSign = value.getFlightPlan().getCallSign();
                    long currentTime = System.currentTimeMillis();
                    
                    if (!lastProcessedTime.containsKey(callSign) || 
                        (currentTime - lastProcessedTime.get(callSign)) >= PROCESS_INTERVAL) {
                        lastProcessedTime.put(callSign, currentTime);
                        out.collect(value);
                    }
                }
            });

        // 충돌 감지 처리
        DataStream<ConflictDetectionData> alerts = inputStream
            .process(new ProcessFunction<AircraftData, ConflictDetectionData>() {
                private final GridSystem gridSystem = new GridSystem();
                private int collisionCount = 0;

                @Override
                public void processElement(AircraftData aircraft, Context ctx, Collector<ConflictDetectionData> out) throws Exception {
                    if (aircraft.getTrack().getAltitude() < MIN_ALTITUDE) {
                        return;
                    }
                    
                    gridSystem.addAircraft(aircraft);
                    List<AircraftData> nearbyAircraft = gridSystem.findNearbyAircraft(aircraft);

                    for (AircraftData other : nearbyAircraft) {
                        if (other.getTrack().getAltitude() < MIN_ALTITUDE) {
                            continue;
                        }
                        
                        if (isPotentialCollision(aircraft, other)) {
                            collisionCount++;
                            
                            // Calculate distances
                            double horizontalDistance = calculateDistance(
                                aircraft.getTrack().getLatitude(), aircraft.getTrack().getLongitude(),
                                other.getTrack().getLatitude(), other.getTrack().getLongitude()
                            );
                            double altitudeDiff = Math.abs(aircraft.getTrack().getAltitude() - other.getTrack().getAltitude()) * 0.3048;
                            double distance3D = Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(altitudeDiff, 2));
                            
                            System.out.println("\n=== Collision Detection Status ===");
                            System.out.println("Total Detections: " + collisionCount);
                            System.out.println("\nAircraft 1: " + aircraft.getFlightPlan().getCallSign());
                            System.out.println("  Position: " + String.format("%.4f°N, %.4f°E", 
                                aircraft.getTrack().getLatitude(), aircraft.getTrack().getLongitude()));
                            System.out.println("  Altitude: " + aircraft.getTrack().getAltitude() + "ft");
                            
                            System.out.println("\nAircraft 2: " + other.getFlightPlan().getCallSign());
                            System.out.println("  Position: " + String.format("%.4f°N, %.4f°E", 
                                other.getTrack().getLatitude(), other.getTrack().getLongitude()));
                            System.out.println("  Altitude: " + other.getTrack().getAltitude() + "ft");
                            
                            System.out.println("\nDistance Information:");
                            System.out.println("  Horizontal Distance: " + String.format("%.2f m", horizontalDistance));
                            System.out.println("  Vertical Distance: " + String.format("%.2f m", altitudeDiff));
                            System.out.println("  3D Distance: " + String.format("%.2f m", distance3D));
                            System.out.println("================================\n");

                            ConflictDetectionData conflictData = new ConflictDetectionData();
                            ConflictDetectionData.ConflictDetection conflict = new ConflictDetectionData.ConflictDetection();
                            
                            conflict.setAircraft1(aircraft);
                            conflict.setAircraft2(other);
                            conflict.setSource(aircraft.getSource());
                            conflict.setTimestamp(System.currentTimeMillis());
                            conflict.setRecord(aircraft.getRecord());
                            
                            conflictData.setConflictDetection(conflict);
                            out.collect(conflictData);
                            break;
                        }
                    }
                }

                private boolean isPotentialCollision(AircraftData data1, AircraftData data2) {
                    if (data1 == null || data2 == null || 
                        data1.getTrack() == null || data2.getTrack() == null) {
                        return false;
                    }
                    
                    // Compare callSigns to avoid duplicate detection and self-detection
                    String callSign1 = data1.getFlightPlan().getCallSign();
                    String callSign2 = data2.getFlightPlan().getCallSign();
                    
                    // 같은 항공기는 충돌 감지에서 제외
                    if (callSign1.equals(callSign2)) {
                        return false;
                    }
                    
                    if (callSign1.compareTo(callSign2) > 0) {
                        // Swap data1 and data2 to ensure consistent ordering
                        AircraftData temp = data1;
                        data1 = data2;
                        data2 = temp;
                    }
                    
                    AircraftData.Track track1 = data1.getTrack();
                    AircraftData.Track track2 = data2.getTrack();
                    
                    // Calculate 3D distance
                    double horizontalDistance = calculateDistance(
                        track1.getLatitude(), track1.getLongitude(),
                        track2.getLatitude(), track2.getLongitude()
                    );
                    
                    // Convert altitude difference to meters (1 ft = 0.3048 m)
                    double altitudeDiff = Math.abs(track1.getAltitude() - track2.getAltitude()) * 0.3048;
                    
                    // Calculate 3D distance using Pythagorean theorem
                    double distance3D = Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(altitudeDiff, 2));
                    
                    double speed1 = Math.sqrt(
                        Math.pow(track1.getVelocityX(), 2) + 
                        Math.pow(track1.getVelocityY(), 2)
                    );
                    double speed2 = Math.sqrt(
                        Math.pow(track2.getVelocityX(), 2) + 
                        Math.pow(track2.getVelocityY(), 2)
                    );
                    double speedDifference = Math.abs(speed1 - speed2);
                    
                    if (speedDifference > 100) {
                        return false;
                    }
                    
                    return distance3D < TA_DISTANCE;
                }

                private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
                    final int R = 6371;
                    double dLat = Math.toRadians(lat2 - lat1);
                    double dLon = Math.toRadians(lon2 - lon1);
                    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
                    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                    return R * c * 1000; // Convert to meters
                }
            });

        // 결과를 Kafka로 전송
        alerts.sinkTo(createKafkaSink());
        env.execute("AIR-CRASH");
    }

    private Properties createKafkaProperties() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootstrapServers);
        props.setProperty("group.id", "aircraft-collision-detection");
        props.setProperty("auto.offset.reset", "latest");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("client.id", "swift-flink-client");
        
        props.setProperty("acks", "1");
        props.setProperty("retries", "3");
        props.setProperty("batch.size", "32768");
        props.setProperty("linger.ms", "5");
        props.setProperty("buffer.memory", "67108864");
        
        props.setProperty("request.timeout.ms", "30000");
        props.setProperty("session.timeout.ms", "30000");
        
        return props;
    }

    private KafkaSink<ConflictDetectionData> createKafkaSink() {
        return KafkaSink.<ConflictDetectionData>builder()
            .setBootstrapServers(bootstrapServers)
            .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                .setTopic(outputTopic)
                .setValueSerializationSchema(new ConflictDetectionDataSerializer())
                .build())
            .setKafkaProducerConfig(createKafkaProperties())
            .build();
    }

    public static void main(String[] args) throws Exception {
        String bootstrapServers = "13.209.157.53:9092,15.164.111.153:9092,3.34.32.69:9092";
        String inputTopic = "SWIFT";
        String outputTopic = "Error-Crash";

        SwiftFlinkJob job = new SwiftFlinkJob(bootstrapServers, inputTopic, outputTopic);
        job.run();
    }
} 
