package com.lab.dto;

import lombok.Data;

import java.util.List;

@Data
public class DashboardDTO {

    @Data
    public static class Overview {
        private long totalEquipment;
        private long activeEquipment;
        private long totalProjects;
        private long totalDataRecords;
        private long totalUsageHoursThisMonth;
    }

    @Data
    public static class EquipmentUtilization {
        private String equipmentName;
        private long usageHours;
    }

    @Data
    public static class UsageTrend {
        private String date;
        private double totalHours;
    }

    @Data
    public static class ProjectDataDistribution {
        private String projectName;
        private long dataCount;
    }

    @Data
    public static class RecentActivity {
        private String type; // "usage" or "data"
        private String description;
        private String userName;
        private String timestamp;
    }
}
