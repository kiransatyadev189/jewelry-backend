package com.luxeglow.jewelrybackend.controller;

import com.luxeglow.jewelrybackend.entity.Product;
import com.luxeglow.jewelrybackend.service.CloudinaryService;
import com.luxeglow.jewelrybackend.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://fashion-jewelry-store-frontend.vercel.app"
})
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductService productService, CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.status(404).body("Product not found");
        }
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductWithImage(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile image
    ) {
        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest().body("No image selected");
            }

            String imageUrl = cloudinaryService.uploadImage(image);

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setCategory(category);
            product.setImageUrl(imageUrl);

            Product savedProduct = productService.saveProduct(product);

            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Image upload failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Server error: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Optional<Product> optionalProduct = productService.getProductById(id);

            if (optionalProduct.isEmpty()) {
                return ResponseEntity.status(404).body("Product not found");
            }

            Product existingProduct = optionalProduct.get();
            String oldImageUrl = existingProduct.getImageUrl();

            existingProduct.setName(name);
            existingProduct.setDescription(description);
            existingProduct.setPrice(price);
            existingProduct.setCategory(category);

            if (image != null && !image.isEmpty()) {
                String newImageUrl = cloudinaryService.uploadImage(image);
                existingProduct.setImageUrl(newImageUrl);

                if (oldImageUrl != null && !oldImageUrl.isBlank()) {
                    try {
                        cloudinaryService.deleteImage(oldImageUrl);
                    } catch (Exception deleteError) {
                        System.out.println("Old Cloudinary image delete failed: " + deleteError.getMessage());
                    }
                }
            }

            Product updatedProduct = productService.saveProduct(existingProduct);

            return ResponseEntity.ok(updatedProduct);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Image update failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Update failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product updatedProduct) {
        try {
            Product product = productService.updateProduct(id, updatedProduct);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Update failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Optional<Product> optionalProduct = productService.getProductById(id);

            if (optionalProduct.isEmpty()) {
                return ResponseEntity.status(404).body("Product not found");
            }

            Product product = optionalProduct.get();
            String imageUrl = product.getImageUrl();

            productService.deleteProduct(id);

            if (imageUrl != null && !imageUrl.isBlank()) {
                try {
                    cloudinaryService.deleteImage(imageUrl);
                } catch (Exception deleteError) {
                    System.out.println("Cloudinary delete failed: " + deleteError.getMessage());
                }
            }

            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
        }
    }
}