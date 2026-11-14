package com.commercial.project.dto;

import lombok.Data;

import java.util.Map;

@Data
public class OrderCreateRequest {
    private Map<Long, Integer> items;
}
