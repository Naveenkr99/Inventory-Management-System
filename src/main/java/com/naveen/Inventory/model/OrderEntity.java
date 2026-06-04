package com.naveen.Inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import com.naveen.Inventory.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference(value = "order-items")
    private List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonBackReference(value = "user-orders")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime orderedAt;

    public enum Status {
        PLACED, FULFILLED, CANCELLED
    }
}

