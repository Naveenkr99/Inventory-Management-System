package com.naveen.Inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    @Enumerated(EnumType.STRING)
    private LocationType type; // e.g., WAREHOUSE, STORE

    public enum LocationType {
        WAREHOUSE, STORE, DISTRIBUTION_CENTER
    }
}
