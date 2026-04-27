package com.naveen.Inventory.service;

import com.naveen.Inventory.dto.InventoryItemRequest;
import com.naveen.Inventory.model.InventoryItem;
import com.naveen.Inventory.model.Product;
import com.naveen.Inventory.model.Location;
import com.naveen.Inventory.repository.InventoryItemRepository;
import com.naveen.Inventory.repository.LocationRepository;
import com.naveen.Inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    public List<InventoryItem> getInventoryByLocation(Location location) {
        return inventoryItemRepository.findByLocation(location);
    }

    public List<InventoryItem> getInventoryByProduct(Product product) {
        return inventoryItemRepository.findByProduct(product);
    }

    public Optional<InventoryItem> getInventoryItemByProductAndLocation(Product product, Location location) {
        return inventoryItemRepository.findByProductAndLocation(product, location);
    }

    public InventoryItem saveInventoryItem(InventoryItemRequest request) {
        Product product=productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Location location=locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));
        InventoryItem inventoryItem=new InventoryItem();
        inventoryItem.setProduct(product);
        inventoryItem.setLocation(location);
        inventoryItem.setQuantity(request.getQuantity());
        inventoryItem.setMinStockLevel(request.getMinStockLevel());
        inventoryItem.setMaxStockLevel(request.getMaxStockLevel());
        return inventoryItemRepository.save(inventoryItem);
    }

    public void deleteInventoryItem(Long id) {
        inventoryItemRepository.deleteById(id);
    }

    public InventoryItem updateInventoryItem(Long id, InventoryItem itemDetails) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        item.setQuantity(itemDetails.getQuantity());
        item.setMinStockLevel(itemDetails.getMinStockLevel());
        item.setMaxStockLevel(itemDetails.getMaxStockLevel());

        return inventoryItemRepository.save(item);
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems();
    }

    public Optional<InventoryItem> getInventoryItemById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    //does not handle concurrency
    public InventoryItem updateStock(Long id, int quantityChange) {
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        int newQuantity = item.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Quantity cannot be negative. Current: " + item.getQuantity() + ", Change: " + quantityChange);
        }

        item.setQuantity(newQuantity);
        return inventoryItemRepository.save(item);
    }


    //handle concurrency
    @Transactional
    public InventoryItem updateStockAtomic(Long id, int quantityChange) {
        // Validate inventory exists
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found"));

        // Validate quantity won't go negative
        if (item.getQuantity() + quantityChange < 0) {
            throw new RuntimeException("Quantity cannot be negative. Current: " + item.getQuantity() + ", Change: " + quantityChange);
        }

        // Atomic database-level update - no race conditions
        int rowsUpdated = inventoryItemRepository.updateQuantityAtomic(id, quantityChange);

        if (rowsUpdated == 0) {
            throw new RuntimeException("Failed to update inventory item");
        }

        // Return updated item
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found after update"));
    }

    // Method to update stock
//    public InventoryItem updateStock(Product product, Location location, int quantityChange) {
//        Optional<InventoryItem> itemOpt = getInventoryItemByProductAndLocation(product, location);
//        if (itemOpt.isPresent()) {
//            InventoryItem item = itemOpt.get();
//            item.setQuantity(item.getQuantity() + quantityChange);
//            return saveInventoryItem(item);
//        } else {
//            // Create new if not exists
//            InventoryItem newItem = new InventoryItem();
//            newItem.setProduct(product);
//            newItem.setLocation(location);
//            newItem.setQuantity(quantityChange);
//            return saveInventoryItem(newItem);
//        }
//    }
}
