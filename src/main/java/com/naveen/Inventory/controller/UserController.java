package com.naveen.Inventory.controller;

import com.naveen.Inventory.dto.ApiResponse;
import com.naveen.Inventory.model.User;
import com.naveen.Inventory.repository.UserRepository;
import com.naveen.Inventory.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "User created", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.<ApiResponse<?>>ok(new ApiResponse<>(200, "User retrieved", u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "User not found")));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<?>> getOrdersForUser(@PathVariable Long id) {
        // Verify user exists
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "User not found"));
        }
        java.util.List<com.naveen.Inventory.model.OrderEntity> orders = orderRepository.findByUserId(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Orders retrieved for user", orders));
    }
}



