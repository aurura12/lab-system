package com.lab.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IncompatibilityCheckResult {
    private boolean hasConflict;
    private List<ConflictItem> directConflicts = new ArrayList<>();
    private List<ConflictItem> indirectConflicts = new ArrayList<>();

    @Data
    public static class ConflictItem {
        private String categoryAName;
        private String categoryBName;
        private String hazardType;
        private String severity;
        private String description;
        private String actionRequired;
        private String type; // "direct" or "indirect"
        private String chain; // for indirect: e.g., "A → B → C"
    }
}
