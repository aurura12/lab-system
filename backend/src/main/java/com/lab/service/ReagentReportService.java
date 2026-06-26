package com.lab.service;

import com.lab.dto.response.ReagentReportDTO;
import com.lab.entity.ReagentTransaction;
import com.lab.repository.ReagentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReagentReportService {

    private final ReagentTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public ReagentReportDTO getReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(12);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<ReagentTransaction> transactions = transactionRepository.findByTimeRange(start, end);

        ReagentReportDTO report = new ReagentReportDTO();
        report.setMonthly(buildMonthlyReport(transactions));
        report.setByCategory(buildCategoryReport(transactions));
        report.setByProject(buildProjectReport(transactions));
        return report;
    }

    private List<ReagentReportDTO.MonthlyItem> buildMonthlyReport(List<ReagentTransaction> transactions) {
        Map<String, ReagentReportDTO.MonthlyItem> map = new LinkedHashMap<>();

        for (ReagentTransaction t : transactions) {
            String key = t.getCreatedAt().getYear() + "-" + String.format("%02d", t.getCreatedAt().getMonthValue());
            ReagentReportDTO.MonthlyItem item = map.computeIfAbsent(key, k -> {
                ReagentReportDTO.MonthlyItem m = new ReagentReportDTO.MonthlyItem();
                m.setYear(t.getCreatedAt().getYear());
                m.setMonth(t.getCreatedAt().getMonthValue());
                m.setInboundTotal(BigDecimal.ZERO);
                m.setOutboundTotal(BigDecimal.ZERO);
                return m;
            });

            if ("inbound".equals(t.getType())) {
                item.setInboundCount(item.getInboundCount() + 1);
                item.setInboundTotal(item.getInboundTotal().add(t.getQuantity()));
            } else {
                item.setOutboundCount(item.getOutboundCount() + 1);
                item.setOutboundTotal(item.getOutboundTotal().add(t.getQuantity()));
            }
        }

        return new ArrayList<>(map.values());
    }

    private List<ReagentReportDTO.CategoryItem> buildCategoryReport(List<ReagentTransaction> transactions) {
        Map<String, ReagentReportDTO.CategoryItem> map = new LinkedHashMap<>();

        for (ReagentTransaction t : transactions) {
            if (!"outbound".equals(t.getType())) continue;
            if (t.getInventory() == null || t.getInventory().getCategory() == null) continue;

            String name = t.getInventory().getCategory().getName();
            ReagentReportDTO.CategoryItem item = map.computeIfAbsent(name, k -> {
                ReagentReportDTO.CategoryItem c = new ReagentReportDTO.CategoryItem();
                c.setCategoryName(name);
                c.setUnit(t.getInventory().getUnit());
                c.setOutboundTotal(BigDecimal.ZERO);
                return c;
            });

            item.setOutboundCount(item.getOutboundCount() + 1);
            item.setOutboundTotal(item.getOutboundTotal().add(t.getQuantity()));
        }

        return new ArrayList<>(map.values());
    }

    private List<ReagentReportDTO.ProjectItem> buildProjectReport(List<ReagentTransaction> transactions) {
        Map<String, ReagentReportDTO.ProjectItem> map = new LinkedHashMap<>();

        for (ReagentTransaction t : transactions) {
            if (!"outbound".equals(t.getType())) continue;
            if (t.getProject() == null) continue;

            String name = t.getProject().getName();
            ReagentReportDTO.ProjectItem item = map.computeIfAbsent(name, k -> {
                ReagentReportDTO.ProjectItem p = new ReagentReportDTO.ProjectItem();
                p.setProjectName(name);
                p.setOutboundTotal(BigDecimal.ZERO);
                return p;
            });

            item.setOutboundCount(item.getOutboundCount() + 1);
            item.setOutboundTotal(item.getOutboundTotal().add(t.getQuantity()));
        }

        return new ArrayList<>(map.values());
    }
}
