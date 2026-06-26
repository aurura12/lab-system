package com.lab.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class IncompatibilityCheckRequest {
    private List<String> categoryIds;
    private String scenario;
}
