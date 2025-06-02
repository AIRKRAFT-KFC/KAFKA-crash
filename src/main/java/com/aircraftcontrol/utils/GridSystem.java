package com.aircraftcontrol.utils;

import com.aircraftcontrol.model.AircraftData;
import java.io.Serializable;
import java.util.*;

public class GridSystem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final double GRID_SIZE = 10.0; // 10km x 10km 격자
    private final Map<String, Set<AircraftData>> gridMap;

    public GridSystem() {
        this.gridMap = new HashMap<>();
    }

    // 위도, 경도를 격자 좌표로 변환
    public String getGridKey(double latitude, double longitude) {
        int latGrid = (int) (latitude / GRID_SIZE);
        int lonGrid = (int) (longitude / GRID_SIZE);
        return latGrid + "," + lonGrid;
    }

    // 항공기 데이터를 격자에 추가
    public void addAircraft(AircraftData aircraft) {
        if (aircraft == null || aircraft.getTrack() == null) {
            return;  // null 데이터는 무시
        }
        
        String gridKey = getGridKey(
            aircraft.getTrack().getLatitude(),
            aircraft.getTrack().getLongitude()
        );
        gridMap.computeIfAbsent(gridKey, k -> new HashSet<>()).add(aircraft);
    }

    // 격자 내의 모든 항공기 가져오기
    public Set<AircraftData> getAircraftInGrid(String gridKey) {
        return gridMap.getOrDefault(gridKey, new HashSet<>());
    }

    // 인접 격자의 키 목록 가져오기
    public List<String> getAdjacentGridKeys(String gridKey) {
        String[] parts = gridKey.split(",");
        int latGrid = Integer.parseInt(parts[0]);
        int lonGrid = Integer.parseInt(parts[1]);

        List<String> adjacentKeys = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // 현재 격자는 제외
                adjacentKeys.add((latGrid + i) + "," + (lonGrid + j));
            }
        }
        return adjacentKeys;
    }

    // 특정 항공기와 가까운 모든 항공기 찾기
    public List<AircraftData> findNearbyAircraft(AircraftData aircraft) {
        if (aircraft == null || aircraft.getTrack() == null) {
            return new ArrayList<>();  // null 데이터는 빈 리스트 반환
        }
        
        String gridKey = getGridKey(
            aircraft.getTrack().getLatitude(),
            aircraft.getTrack().getLongitude()
        );
        
        List<AircraftData> nearbyAircraft = new ArrayList<>();
        
        // 현재 격자의 항공기들
        nearbyAircraft.addAll(getAircraftInGrid(gridKey));
        
        // 인접 격자의 항공기들
        for (String adjacentKey : getAdjacentGridKeys(gridKey)) {
            nearbyAircraft.addAll(getAircraftInGrid(adjacentKey));
        }
        
        // 자기 자신 제외
        nearbyAircraft.remove(aircraft);
        
        return nearbyAircraft;
    }

    // 격자 시스템 초기화
    public void clear() {
        gridMap.clear();
    }
} 