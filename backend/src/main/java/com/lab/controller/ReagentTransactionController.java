package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.ReagentTransactionDTO;
import com.lab.service.ReagentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reagent-transactions")
@RequiredArgsConstructor
public class ReagentTransactionController {

    private final ReagentTransactionService transactionService;

    @GetMapping
    public ApiResponse<PageResponse<ReagentTransactionDTO>> getTransactions(
            @RequestParam(required = false) UUID inventoryId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
                transactionService.getTransactions(inventoryId, type, startDate, endDate, page, size));
    }

    @GetMapping("/trace")
    public ApiResponse<List<ReagentTransactionDTO>> traceByBarcode(@RequestParam String barcode) {
        return ApiResponse.success(transactionService.traceByBarcode(barcode));
    }
}
