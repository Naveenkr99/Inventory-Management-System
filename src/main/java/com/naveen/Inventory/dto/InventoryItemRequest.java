package com.naveen.Inventory.dto;

import lombok.Data;

@Data
public class InventoryItemRequest {
    private Long productId;
    private Long locationId;
    private Integer quantity;
    private Integer minStockLevel;
    private Integer maxStockLevel;
}
