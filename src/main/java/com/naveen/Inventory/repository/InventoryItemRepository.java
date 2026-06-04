package com.naveen.Inventory.repository;

import com.naveen.Inventory.model.InventoryItem;
import com.naveen.Inventory.model.Product;
import com.naveen.Inventory.model.Location;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

//    @Query("SELECT i FROM InventoryItem i WHERE i.product.id = :productId AND i.location.id = :locationId")
    Optional<InventoryItem> findByProductAndLocation(Product product, Location location);

    List<InventoryItem> findByLocation(Location location);

    List<InventoryItem> findByProduct(Product product);

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity < i.minStockLevel")
    List<InventoryItem> findLowStockItems();

    // Pessimistic locking - locks the row for the duration of the transaction
    // Prevents race conditions when checking and updating inventory
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findByIdWithLock(@Param("id") Long id);

    //Atomic DB update - only updates if result won't be negative
    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity + :quantityChange WHERE i.id = :id AND (i.quantity + :quantityChange) >= 0")
    int updateQuantityAtomicIfPositive(@Param("id") Long id, @Param("quantityChange") int quantityChange);

    //Legacy atomic DB update (allows negative values - use with caution)
    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity + :quantityChange WHERE i.id = :id")
    int updateQuantityAtomic(@Param("id") Long id, @Param("quantityChange") int quantityChange);
}
