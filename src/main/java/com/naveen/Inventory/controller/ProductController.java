package com.naveen.Inventory.controller;

import com.naveen.Inventory.model.Product;
import com.naveen.Inventory.service.ProductService;
import com.naveen.Inventory.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // show all product
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>(200, "Products retrieved successfully", products));
    }

    //find product by id
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Product retrieved successfully", product.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Product not found"));
        }
    }

    //find product by sku
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<Product>> getProductBySku(@PathVariable String sku) {
        Optional<Product> product = productService.getProductBySku(sku);
        if (product.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Product retrieved successfully", product.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Product not found"));
        }
    }

    //adding product
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Product created successfully", savedProduct));
    }

    //updating product
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setSku(productDetails.getSku());
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setCategory(productDetails.getCategory());
            product.setPrice(productDetails.getPrice());
            Product updatedProduct = productService.saveProduct(product);
            return ResponseEntity.ok(new ApiResponse<>(200, "Product updated successfully", updatedProduct));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Product not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);
        if(productOpt.isPresent()){
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Product deleted successfully"));
        }else{
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Invalid product ID"));
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse<>(404, "Product not found"));
        }

    }
}
