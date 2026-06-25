package com.lab.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class BatchUseRequest {
    private List<BatchUseItem> items;
    private String projectId;
    private String purpose;
}
