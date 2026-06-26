package com.lab.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReagentReportDTO {
    private java.util.List<MonthlyItem> monthly;
    private java.util.List<CategoryItem> byCategory;
    private java.util.List<ProjectItem> byProject;

    @Data
    public static class MonthlyItem {
        private int year;
        private int month;
        private long inboundCount;
        private BigDecimal inboundTotal;
        private long outboundCount;
        private BigDecimal outboundTotal;
    }

    @Data
    public static class CategoryItem {
        private String categoryName;
        private long outboundCount;
        private BigDecimal outboundTotal;
        private String unit;
    }

    @Data
    public static class ProjectItem {
        private String projectName;
        private long outboundCount;
        private BigDecimal outboundTotal;
    }
}
