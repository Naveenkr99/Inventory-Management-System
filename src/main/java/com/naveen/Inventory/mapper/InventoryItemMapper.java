package com.naveen.Inventory.mapper;

import com.naveen.Inventory.dto.InventoryItemRequest;

public class InventoryItemMapper {

    public InventoryItemRequest toRequest(Long productId, Long locationId, Integer quantity, Integer minStockLevel, Integer maxStockLevel) {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setProductId(productId);
        request.setLocationId(locationId);
        request.setQuantity(quantity);
        request.setMinStockLevel(minStockLevel);
        request.setMaxStockLevel(maxStockLevel);
        return request;
    }
}
