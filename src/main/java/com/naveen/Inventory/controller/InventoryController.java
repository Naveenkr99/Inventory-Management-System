package com.naveen.Inventory.controller;

import com.naveen.Inventory.dto.InventoryItemRequest;
import com.naveen.Inventory.model.InventoryItem;
import com.naveen.Inventory.model.Product;
import com.naveen.Inventory.model.Location;
import com.naveen.Inventory.service.InventoryService;
import com.naveen.Inventory.service.ProductService;
import com.naveen.Inventory.service.LocationService;
import com.naveen.Inventory.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LocationService locationService;

    //get all inventory
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAllInventoryItems() {
        List<InventoryItem> items = inventoryService.getAllInventoryItems();
        return ResponseEntity.ok(new ApiResponse<>(200, "Inventory items retrieved successfully", items));
    }

    //search by loc id
    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<?>> getInventoryByLocation(@PathVariable Long locationId) {
        Optional<Location> location = locationService.getLocationById(locationId);
        if (location.isPresent()) {
            List<InventoryItem> items = inventoryService.getInventoryByLocation(location.get());
            return ResponseEntity.ok(new ApiResponse<>(200, "Inventory items for location retrieved successfully", items));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Location not found"));
        }
    }

    //search by product id
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<?>> getInventoryByProduct(@PathVariable Long productId) {
        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            List<InventoryItem> items = inventoryService.getInventoryByProduct(product.get());
            return ResponseEntity.ok(new ApiResponse<>(200, "Inventory items for product retrieved successfully", items));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Product not found"));
        }
    }

    //search inv by loc id and product id
    @GetMapping("/{productId}/{locationId}")
    public ResponseEntity<ApiResponse<?>> getInventoryByProductAndLocation(@PathVariable Long productId, @PathVariable Long locationId) {
        Optional<Product> product = productService.getProductById(productId);
        Optional<Location> location = locationService.getLocationById(locationId);
        if (product.isPresent() && location.isPresent()) {
            Optional<InventoryItem> item = inventoryService.getInventoryItemByProductAndLocation(product.get(), location.get());
            if (item.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(200, "Inventory item retrieved successfully", item.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Inventory item not found for given product and location"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Product or Location not found"));
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStockItems() {
        List<InventoryItem> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(new ApiResponse<>(200, "Low stock items retrieved successfully", items));
    }

    //adding inventory item
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createInventoryItem(@RequestBody InventoryItemRequest request) {
        try {
            InventoryItem savedItem = inventoryService.saveInventoryItem(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "Inventory item created successfully", savedItem));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Product not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Product not found"));
            } else if (e.getMessage().contains("Location not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Location not found"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Invalid request: " + e.getMessage()));
            }
        }
    }

    //updating inventory item by id
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateInventoryItem(@PathVariable Long id, @RequestBody InventoryItem itemDetails) {
        try {
            InventoryItem updatedItem = inventoryService.updateInventoryItem(id, itemDetails);
            return ResponseEntity.ok(new ApiResponse<>(200, "Inventory item updated successfully", updatedItem));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Failed to update: " + e.getMessage()));
            }
        }
    }

    //increase inv by id (atomic database update - safe for concurrent access)
    @PutMapping("/inc/{id}")
    public ResponseEntity<ApiResponse<?>> increaseInventoryItem(@PathVariable Long id, @RequestParam int quantityChange) {
        try {
            // Atomic database update prevents race conditions entirely
            InventoryItem updatedItem = inventoryService.updateStockAtomic(id, quantityChange);
            return ResponseEntity.ok(new ApiResponse<>(200, "Inventory item stock increased successfully", updatedItem));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, e.getMessage()));
            } else if (e.getMessage().contains("negative")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, "Failed to update stock: " + e.getMessage()));
            }
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Inventory item deleted successfully"));
    }

    //    @PutMapping("/update-stock")
//    public ResponseEntity<ApiResponse<?>> updateStock(@RequestParam Long productId, @RequestParam Long locationId, @RequestParam int quantityChange) {
//        Optional<Product> product = productService.getProductById(productId);
//        Optional<Location> location = locationService.getLocationById(locationId);
//        if (product.isPresent() && location.isPresent()) {
//            InventoryItem updatedItem = inventoryService.updateStock(product.get(), location.get(), quantityChange);
//            return ResponseEntity.ok(new ApiResponse<>(200, "Stock updated successfully", updatedItem));
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse<>(404, "Product or Location not found"));
//        }
//    }
}
