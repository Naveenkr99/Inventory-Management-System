package com.naveen.Inventory.controller;

import com.naveen.Inventory.dto.ApiResponse;
import com.naveen.Inventory.dto.OrderRequest;
import com.naveen.Inventory.model.OrderEntity;
import com.naveen.Inventory.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderEntity>>> getAllOrders() {
        List<OrderEntity> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse<>(200, "Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@PathVariable Long id) {
        try {
            OrderEntity order = orderService.getOrderById(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Order retrieved successfully", order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, e.getMessage()));
        }
    }

    // Place order with multiple items
    @PostMapping
    public ResponseEntity<ApiResponse<?>> placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderEntity order = orderService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Order placed successfully", order));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, msg));
            }
            if (msg.contains("Insufficient") || msg.contains("must contain") || msg.contains("must be greater")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, msg));
        }
    }

    // Cancel order - restocks all items
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(@PathVariable Long id) {
        try {
            OrderEntity order = orderService.cancelOrder(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Order cancelled successfully", order));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, msg));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, msg));
        }
    }
}

