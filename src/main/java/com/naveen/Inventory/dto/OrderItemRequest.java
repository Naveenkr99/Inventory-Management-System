package com.naveen.Inventory.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private Long locationId;
    private Integer quantity;
}

