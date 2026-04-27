package com.naveen.Inventory.controller;

import com.naveen.Inventory.model.Location;
import com.naveen.Inventory.service.LocationService;
import com.naveen.Inventory.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Location>>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(new ApiResponse<>(200, "Locations retrieved successfully", locations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Location>> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationService.getLocationById(id);
        if (location.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Location retrieved successfully", location.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Location not found"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createLocation(@RequestBody Location location) {
        if (location.getId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "ID is required for creating a location"));
        }
        Location savedLocation = locationService.saveLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Location created successfully", savedLocation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateLocation(@PathVariable Long id, @RequestBody Location locationDetails) {
        Optional<Location> locationOpt = locationService.getLocationById(id);
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            location.setName(locationDetails.getName());
            location.setAddress(locationDetails.getAddress());
            location.setCity(locationDetails.getCity());
            location.setState(locationDetails.getState());
            location.setZipCode(locationDetails.getZipCode());
            location.setCountry(locationDetails.getCountry());
            location.setType(locationDetails.getType());
            Location updatedLocation = locationService.saveLocation(location);
            return ResponseEntity.ok(new ApiResponse<>(200, "Location updated successfully", updatedLocation));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Location not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Location deleted successfully"));
    }
}
