package com.commercial.project.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStats{
    private long totalOrders;
    private long pendingOrders;
    private long lowStockProductsCounts;
    private BigDecimal totalRevenue;
}
