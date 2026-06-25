package com.lab.service;

import com.lab.dto.request.ReagentCategoryRequest;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentCategoryDTO;
import com.lab.entity.ReagentCategory;
import com.lab.exception.BadRequestException;
import com.lab.exception.ResourceNotFoundException;
import com.lab.repository.ReagentCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReagentCategoryService {

    private final ReagentCategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReagentCategoryDTO> getCategories(String keyword, String hazardClass,
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ReagentCategory> categoryPage;

        if (keyword != null && !keyword.isEmpty()) {
            categoryPage = categoryRepository.findByKeyword(keyword, pageable);
        } else if (hazardClass != null && !hazardClass.isEmpty()) {
            categoryPage = categoryRepository.findByHazardClass(hazardClass, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        return new PageResponse<>(
                categoryPage.getContent().stream().map(ReagentCategoryDTO::fromEntity).toList(),
                categoryPage.getTotalElements(), categoryPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public ReagentCategoryDTO getById(UUID id) {
        return ReagentCategoryDTO.fromEntity(findCategory(id));
    }

    @Transactional
    public ReagentCategoryDTO create(ReagentCategoryRequest request) {
        if (request.getCasNo() != null && !request.getCasNo().isEmpty()
                && categoryRepository.existsByCasNo(request.getCasNo())) {
            throw new BadRequestException("CAS number already exists: " + request.getCasNo());
        }

        ReagentCategory category = new ReagentCategory();
        category.setName(request.getName());
        category.setCasNo(request.getCasNo());
        category.setMolecularFormula(request.getMolecularFormula());
        category.setSpecification(request.getSpecification());
        category.setHazardClass(request.getHazardClass() != null ? request.getHazardClass() : "none");
        category.setUnit(request.getUnit() != null ? request.getUnit() : "瓶");
        category.setDefaultShelfLifeDays(request.getDefaultShelfLifeDays());
        category.setMinStockThreshold(request.getMinStockThreshold());
        category.setStorageConditions(request.getStorageConditions());

        return ReagentCategoryDTO.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public ReagentCategoryDTO update(UUID id, ReagentCategoryRequest request) {
        ReagentCategory category = findCategory(id);

        if (request.getName() != null) category.setName(request.getName());
        if (request.getCasNo() != null && !request.getCasNo().equals(category.getCasNo())) {
            if (categoryRepository.existsByCasNo(request.getCasNo())) {
                throw new BadRequestException("CAS number already exists: " + request.getCasNo());
            }
            category.setCasNo(request.getCasNo());
        }
        if (request.getMolecularFormula() != null) category.setMolecularFormula(request.getMolecularFormula());
        if (request.getSpecification() != null) category.setSpecification(request.getSpecification());
        if (request.getHazardClass() != null) category.setHazardClass(request.getHazardClass());
        if (request.getUnit() != null) category.setUnit(request.getUnit());
        if (request.getDefaultShelfLifeDays() != null) category.setDefaultShelfLifeDays(request.getDefaultShelfLifeDays());
        if (request.getMinStockThreshold() != null) category.setMinStockThreshold(request.getMinStockThreshold());
        if (request.getStorageConditions() != null) category.setStorageConditions(request.getStorageConditions());

        return ReagentCategoryDTO.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        categoryRepository.deleteById(id);
    }

    private ReagentCategory findCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReagentCategory", "id", id));
    }
}
