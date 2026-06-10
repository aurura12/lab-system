package com.lab.service;

import com.lab.dto.DashboardDTO;
import com.lab.entity.Equipment;
import com.lab.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EquipmentRepository equipmentRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final DataRecordRepository dataRecordRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public DashboardDTO.Overview getOverview() {
        DashboardDTO.Overview overview = new DashboardDTO.Overview();
        overview.setTotalEquipment(equipmentRepository.count());
        overview.setActiveEquipment(equipmentRepository.countByStatus(Equipment.Status.in_use));
        overview.setTotalProjects(projectRepository.count());
        overview.setTotalDataRecords(dataRecordRepository.count());

        // Calculate total usage hours this month
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long totalSeconds = 0;
        List<Equipment> allEquipment = equipmentRepository.findAll();
        for (Equipment eq : allEquipment) {
            totalSeconds += usageRecordRepository.sumUsageSecondsByEquipmentAndTimeRange(eq.getId(), monthStart);
        }
        overview.setTotalUsageHoursThisMonth(totalSeconds / 3600);

        return overview;
    }

    @Transactional(readOnly = true)
    public List<DashboardDTO.EquipmentUtilization> getEquipmentUtilization() {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Equipment> allEquipment = equipmentRepository.findAll();

        return allEquipment.stream().map(eq -> {
            DashboardDTO.EquipmentUtilization utilization = new DashboardDTO.EquipmentUtilization();
            utilization.setEquipmentName(eq.getName());
            long seconds = usageRecordRepository.sumUsageSecondsByEquipmentAndTimeRange(eq.getId(), monthStart);
            utilization.setUsageHours(seconds / 3600);
            return utilization;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardDTO.UsageTrend> getUsageTrend(int days) {
        LocalDateTime startTime = LocalDate.now().minusDays(days).atStartOfDay();
        List<Object[]> rawTrend = usageRecordRepository.findDailyUsageTrend(startTime);

        return rawTrend.stream().map(row -> {
            DashboardDTO.UsageTrend trend = new DashboardDTO.UsageTrend();
            trend.setDate(row[0].toString());
            trend.setTotalHours(((Number) row[1]).doubleValue() / 3600.0);
            return trend;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardDTO.ProjectDataDistribution> getProjectDataDistribution() {
        List<Object[]> rawDistribution = dataRecordRepository.countByProject();

        return rawDistribution.stream().map(row -> {
            DashboardDTO.ProjectDataDistribution dist = new DashboardDTO.ProjectDataDistribution();
            UUID projectId = (UUID) row[0];
            dist.setDataCount(((Number) row[1]).longValue());
            // We need project name - fetch from projectRepository
            projectRepository.findById(projectId).ifPresent(p -> dist.setProjectName(p.getName()));
            return dist;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardDTO.RecentActivity> getRecentActivity() {
        List<DashboardDTO.RecentActivity> activities = new ArrayList<>();

        // Get recent usage records
        List<com.lab.entity.UsageRecord> usageRecords = usageRecordRepository.findAll();
        usageRecords.stream()
                .sorted(Comparator.comparing(com.lab.entity.UsageRecord::getCreatedAt).reversed())
                .limit(5)
                .forEach(record -> {
                    DashboardDTO.RecentActivity activity = new DashboardDTO.RecentActivity();
                    activity.setType("usage");
                    activity.setUserName(record.getUser().getRealName());
                    activity.setDescription("Used " + record.getEquipment().getName());
                    activity.setTimestamp(record.getCreatedAt().toString());
                    activities.add(activity);
                });

        // Get recent data records
        List<com.lab.entity.DataRecord> dataRecords = dataRecordRepository.findAll();
        dataRecords.stream()
                .sorted(Comparator.comparing(com.lab.entity.DataRecord::getCreatedAt).reversed())
                .limit(5)
                .forEach(record -> {
                    DashboardDTO.RecentActivity activity = new DashboardDTO.RecentActivity();
                    activity.setType("data");
                    activity.setUserName(record.getUser().getRealName());
                    activity.setDescription("Uploaded " + record.getFileName());
                    activity.setTimestamp(record.getCreatedAt().toString());
                    activities.add(activity);
                });

        // Sort by timestamp and take top 10
        return activities.stream()
                .sorted(Comparator.comparing(DashboardDTO.RecentActivity::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
}
