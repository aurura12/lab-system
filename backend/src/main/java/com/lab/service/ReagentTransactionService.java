package com.lab.service;

import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentTransactionDTO;
import com.lab.repository.ReagentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReagentTransactionService {

    private final ReagentTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReagentTransactionDTO> getTransactions(UUID inventoryId, String type,
                                                                LocalDate startDate, LocalDate endDate,
                                                                int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.lab.entity.ReagentTransaction> transactionPage;

        if (inventoryId != null) {
            transactionPage = transactionRepository.findByInventoryId(inventoryId, pageable);
        } else if (type != null && !type.isEmpty()) {
            transactionPage = transactionRepository.findByType(type, pageable);
        } else if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            List<com.lab.entity.ReagentTransaction> list = transactionRepository.findByTimeRange(start, end);
            return new PageResponse<>(
                    list.stream().map(ReagentTransactionDTO::fromEntity).toList(),
                    list.size(), 1, page, size
            );
        } else {
            transactionPage = transactionRepository.findAll(pageable);
        }

        return new PageResponse<>(
                transactionPage.getContent().stream().map(ReagentTransactionDTO::fromEntity).toList(),
                transactionPage.getTotalElements(), transactionPage.getTotalPages(), page, size
        );
    }

    @Transactional(readOnly = true)
    public List<ReagentTransactionDTO> traceByBarcode(String barcode) {
        return transactionRepository.findByBarcode(barcode).stream()
                .map(ReagentTransactionDTO::fromEntity)
                .toList();
    }
}
