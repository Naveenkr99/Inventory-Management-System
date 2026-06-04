package com.naveen.Inventory.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;  // Multiple items in one order
    private Long userId; // ID of the user placing the order
}

