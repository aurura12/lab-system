package com.lab.service;

import com.lab.dto.request.IncompatibilityRuleRequest;
import com.lab.dto.response.IncompatibilityCheckResult;
import com.lab.dto.response.IncompatibilityRuleDTO;
import com.lab.entity.IncompatibilityRule;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.IncompatibilityRuleRepository;
import com.lab.repository.ReagentCategoryRepository;
import com.lab.repository.ReagentInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class IncompatibilityRuleService {

    private final IncompatibilityRuleRepository ruleRepository;
    private final ReagentCategoryRepository categoryRepository;
    private final ReagentInventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public Page<IncompatibilityRuleDTO> getRules(String scenario, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (scenario != null && !scenario.isEmpty()) {
            List<IncompatibilityRule> list = ruleRepository.findByScenario(scenario);
            return new org.springframework.data.domain.PageImpl<>(
                    list.stream().map(this::toDTO).toList(), pageable, list.size());
        } else {
            return ruleRepository.findAll(pageable).map(this::toDTO);
        }
    }

    @Transactional(readOnly = true)
    public IncompatibilityRuleDTO getById(UUID id) {
        return toDTO(findRule(id));
    }

    @Transactional
    public IncompatibilityRuleDTO create(IncompatibilityRuleRequest request) {
        UUID categoryAId = UUID.fromString(request.getCategoryAId());
        UUID categoryBId = UUID.fromString(request.getCategoryBId());

        var existing = ruleRepository.findBetween(categoryAId, categoryBId);
        if (existing.isPresent()) {
            throw new BadRequestException("该禁忌规则已存在");
        }

        IncompatibilityRule rule = new IncompatibilityRule();
        rule.setCategoryA(categoryRepository.findById(categoryAId)
                .orElseThrow(() -> new ResourceNotFoundException("ReagentCategory", "id", categoryAId)));
        rule.setCategoryB(categoryRepository.findById(categoryBId)
                .orElseThrow(() -> new ResourceNotFoundException("ReagentCategory", "id", categoryBId)));
        rule.setHazardType(request.getHazardType());
        rule.setSeverity(request.getSeverity());
        rule.setDescription(request.getDescription());
        rule.setActionRequired(request.getActionRequired());
        rule.setScenario(request.getScenario() != null ? request.getScenario() : "storage");

        return toDTO(ruleRepository.save(rule));
    }

    @Transactional
    public IncompatibilityRuleDTO update(UUID id, IncompatibilityRuleRequest request) {
        IncompatibilityRule rule = findRule(id);
        if (request.getHazardType() != null) rule.setHazardType(request.getHazardType());
        if (request.getSeverity() != null) rule.setSeverity(request.getSeverity());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getActionRequired() != null) rule.setActionRequired(request.getActionRequired());
        if (request.getScenario() != null) rule.setScenario(request.getScenario());
        return toDTO(ruleRepository.save(rule));
    }

    @Transactional
    public void delete(UUID id) {
        ruleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public IncompatibilityCheckResult check(List<String> categoryIdStrs, String scenario) {
        if (categoryIdStrs == null || categoryIdStrs.size() < 2) {
            IncompatibilityCheckResult empty = new IncompatibilityCheckResult();
            empty.setHasConflict(false);
            return empty;
        }

        List<UUID> ids = categoryIdStrs.stream().map(UUID::fromString).toList();
        IncompatibilityCheckResult result = new IncompatibilityCheckResult();
        String targetScenario = scenario != null ? scenario : "all";

        // 一次性加载所有相关规则 + 构建内存邻接表，避免 BFS 中 N+1 查询
        List<IncompatibilityRule> allRules = ruleRepository.findWithinSet(ids);
        Map<UUID, List<IncompatibilityRule>> adjacency = new HashMap<>();
        Set<String> foundPairs = new HashSet<>();

        for (IncompatibilityRule r : allRules) {
            if (!matchesScenario(r.getScenario(), targetScenario)) continue;
            adjacency.computeIfAbsent(r.getCategoryAId(), k -> new ArrayList<>()).add(r);
            adjacency.computeIfAbsent(r.getCategoryBId(), k -> new ArrayList<>()).add(r);

            // 直接冲突加入结果
            IncompatibilityCheckResult.ConflictItem item = buildConflictItem(r, "direct");
            result.getDirectConflicts().add(item);
            foundPairs.add(pairKey(r.getCategoryAId(), r.getCategoryBId()));
        }

        // 加载品类名称
        Map<UUID, String> idToName = new HashMap<>();
        for (UUID id : ids) {
            categoryRepository.findById(id).ifPresent(c -> idToName.put(id, c.getName()));
        }

        // 间接冲突检测（BFS 在内存邻接表上运行）
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                UUID id1 = ids.get(i);
                UUID id2 = ids.get(j);
                if (foundPairs.contains(pairKey(id1, id2))) continue;

                List<UUID> path = findIndirectPathInMemory(id1, id2, ids, adjacency);
                if (path != null) {
                    IncompatibilityCheckResult.ConflictItem item = new IncompatibilityCheckResult.ConflictItem();
                    item.setCategoryAName(idToName.getOrDefault(id1, "?"));
                    item.setCategoryBName(idToName.getOrDefault(id2, "?"));
                    item.setType("indirect");
                    item.setSeverity("warning");
                    item.setHazardType("other");
                    item.setDescription("没有直接禁忌规则，但通过中介品类存在间接关联，建议确认");
                    item.setActionRequired("查阅化学品安全技术说明书(MSDS)确认兼容性");
                    item.setChain(buildChain(path, idToName));
                    result.getIndirectConflicts().add(item);
                }
            }
        }

        result.setHasConflict(!result.getDirectConflicts().isEmpty() || !result.getIndirectConflicts().isEmpty());
        return result;
    }

    @Transactional(readOnly = true)
    public IncompatibilityCheckResult checkLocation(UUID locationId, UUID newCategoryId, String scenario) {
        IncompatibilityCheckResult result = new IncompatibilityCheckResult();
        result.setDirectConflicts(new ArrayList<>());
        result.setIndirectConflicts(new ArrayList<>());

        // 查询该位置下所有活跃库存的品类 ID
        List<UUID> existingCategoryIds = inventoryRepository.findActiveCategoryIdsByLocationId(locationId);
        if (existingCategoryIds.isEmpty()) {
            result.setHasConflict(false);
            return result;
        }

        // 查询新品类与现有品类之间的禁忌规则
        String targetScenario = scenario != null ? scenario : "storage";
        List<IncompatibilityRule> rules = ruleRepository.findBetweenTargetAndSet(newCategoryId, existingCategoryIds);

        for (IncompatibilityRule r : rules) {
            if (!matchesScenario(r.getScenario(), targetScenario)) continue;
            result.getDirectConflicts().add(buildConflictItem(r, "direct"));
        }

        result.setHasConflict(!result.getDirectConflicts().isEmpty());
        return result;
    }

    private List<UUID> findIndirectPathInMemory(UUID from, UUID to, List<UUID> validIds,
                                                 Map<UUID, List<IncompatibilityRule>> adjacency) {
        Queue<List<UUID>> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();
        queue.add(List.of(from));
        visited.add(from);

        while (!queue.isEmpty()) {
            List<UUID> path = queue.poll();
            UUID current = path.get(path.size() - 1);

            List<IncompatibilityRule> rules = adjacency.get(current);
            if (rules == null) continue;

            for (IncompatibilityRule r : rules) {
                UUID neighbor = r.getCategoryAId().equals(current) ? r.getCategoryBId() : r.getCategoryAId();
                if (!validIds.contains(neighbor)) continue;
                if (visited.contains(neighbor)) continue;
                if (neighbor.equals(to)) {
                    List<UUID> fullPath = new ArrayList<>(path);
                    fullPath.add(neighbor);
                    return fullPath;
                }
                visited.add(neighbor);
                List<UUID> newPath = new ArrayList<>(path);
                newPath.add(neighbor);
                queue.add(newPath);
            }
        }
        return null;
    }

    private boolean matchesScenario(String ruleScenario, String targetScenario) {
        if ("all".equals(ruleScenario)) return true;
        if ("all".equals(targetScenario)) return true;
        return ruleScenario.equals(targetScenario);
    }

    private String pairKey(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a.toString() + "-" + b.toString() : b.toString() + "-" + a.toString();
    }

    private String buildChain(List<UUID> path, Map<UUID, String> idToName) {
        return path.stream()
                .map(id -> idToName.getOrDefault(id, "?"))
                .reduce((a, b) -> a + " → " + b)
                .orElse("");
    }

    private IncompatibilityCheckResult.ConflictItem buildConflictItem(IncompatibilityRule r, String type) {
        IncompatibilityCheckResult.ConflictItem item = new IncompatibilityCheckResult.ConflictItem();
        item.setCategoryAName(r.getCategoryA() != null ? r.getCategoryA().getName() : "?");
        item.setCategoryBName(r.getCategoryB() != null ? r.getCategoryB().getName() : "?");
        item.setHazardType(r.getHazardType());
        item.setSeverity(r.getSeverity());
        item.setDescription(r.getDescription());
        item.setActionRequired(r.getActionRequired());
        item.setType(type);
        return item;
    }

    private IncompatibilityRuleDTO toDTO(IncompatibilityRule rule) {
        IncompatibilityRuleDTO dto = new IncompatibilityRuleDTO();
        dto.setId(rule.getId());
        dto.setCategoryAId(rule.getCategoryAId());
        if (rule.getCategoryA() != null) dto.setCategoryAName(rule.getCategoryA().getName());
        dto.setCategoryBId(rule.getCategoryBId());
        if (rule.getCategoryB() != null) dto.setCategoryBName(rule.getCategoryB().getName());
        dto.setHazardType(rule.getHazardType());
        dto.setSeverity(rule.getSeverity());
        dto.setDescription(rule.getDescription());
        dto.setActionRequired(rule.getActionRequired());
        dto.setScenario(rule.getScenario());
        dto.setCreatedAt(rule.getCreatedAt());
        return dto;
    }

    private IncompatibilityRule findRule(UUID id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncompatibilityRule", "id", id));
    }
}
