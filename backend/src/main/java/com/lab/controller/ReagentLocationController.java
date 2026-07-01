package com.lab.controller;

import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.ReagentInventoryDTO;
import com.lab.service.ReagentInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reagent-locations")
@RequiredArgsConstructor
public class ReagentLocationController {

    private final ReagentInventoryService inventoryService;

    @GetMapping("/search")
    public ApiResponse<List<ReagentInventoryDTO>> search(@RequestParam String keyword) {
        // Use inventory search which already does fuzzy matching on name, barcode, batchNo
        var page = inventoryService.getInventory(keyword, null, null, null, null, 0, 50);
        return ApiResponse.success(page.getContent());
    }
}
