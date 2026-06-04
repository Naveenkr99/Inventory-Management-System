package com.naveen.Inventory.service;

import com.naveen.Inventory.dto.OrderItemRequest;
import com.naveen.Inventory.dto.OrderRequest;
import com.naveen.Inventory.model.InventoryItem;
import com.naveen.Inventory.model.OrderEntity;
import com.naveen.Inventory.model.OrderItem;
import com.naveen.Inventory.model.Product;
import com.naveen.Inventory.model.Location;
import com.naveen.Inventory.repository.OrderRepository;
import com.naveen.Inventory.repository.OrderItemRepository;
import com.naveen.Inventory.repository.UserRepository;
import com.naveen.Inventory.model.User;
import com.naveen.Inventory.service.ProductService;
import com.naveen.Inventory.service.LocationService;
import com.naveen.Inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserRepository userRepository;

    public List<OrderEntity> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderEntity getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // Place order with multiple items - atomic all-or-nothing
    @Transactional
    public OrderEntity placeOrder(OrderRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("Order must be associated with a userId");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        // Validate all items first before modifying inventory
        List<OrderItem> validatedOrderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new RuntimeException("All quantities must be greater than zero");
            }

            Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemRequest.getProductId()));
            
            Location location = locationService.getLocationById(itemRequest.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found with ID: " + itemRequest.getLocationId()));

            InventoryItem inventoryItem = inventoryService.getInventoryItemByProductAndLocation(product, location)
                    .orElseThrow(() -> new RuntimeException("Inventory item not found for product " + itemRequest.getProductId() + " at location " + itemRequest.getLocationId()));

            if (inventoryItem.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product " + product.getId() + " at location " + location.getId() + ". Available: " + inventoryItem.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setLocation(location);
            orderItem.setInventoryItemId(inventoryItem.getId());
            orderItem.setQuantity(itemRequest.getQuantity());

            validatedOrderItems.add(orderItem);
        }

        // All validations passed - create order with items
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderEntity.Status.PLACED);
        order.setOrderedAt(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());
        order.setUser(user);

        // Save order first
        OrderEntity savedOrder = orderRepository.save(order);

        // Save all order items and decrement inventory
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (OrderItem orderItem : validatedOrderItems) {
            orderItem.setOrder(savedOrder);
            OrderItem savedItem = orderItemRepository.save(orderItem);
            savedOrderItems.add(savedItem);
            
            // Decrement inventory atomically
            inventoryService.updateStockAtomic(orderItem.getInventoryItemId(), -orderItem.getQuantity());
        }

        // Set the saved items back to the order
        savedOrder.setOrderItems(savedOrderItems);
        
        // Return order with items populated
        return savedOrder;
    }

    // Cancel order - restocks all items atomically
    @Transactional
    public OrderEntity cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderEntity.Status.PLACED) {
            throw new RuntimeException("Only placed orders can be cancelled. Current status: " + order.getStatus());
        }

        // Restock all items in the order
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getInventoryItemId() != null) {
                    inventoryService.updateStockAtomic(item.getInventoryItemId(), item.getQuantity());
                }
            }
        }

        order.setStatus(OrderEntity.Status.CANCELLED);
        return orderRepository.save(order);
    }
}


