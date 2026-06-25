package com.lab.dto.request;

import lombok.Data;

@Data
public class StorageLocationRequest {
    private String parentId;
    private String roomId;
    private String level;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
}
