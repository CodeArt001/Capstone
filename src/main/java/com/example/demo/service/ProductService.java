package com.example.demo.service;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ProductRequestDTO;
import com.example.demo.dto.ProductResponseDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

public class ProductService {
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private CloudinaryService cloudinaryService;

    // FR-PROD-01: Create Product (Admin Only)
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        if (!category.getActive()) {
            throw new RuntimeException("Cannot assign product to an inactive category");
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(category)
                .active(true)
                .imageUrl(dto.getImageUrl())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    // FR-PROD-02: Upload Image to Cloudinary and attach to product
    @Transactional
    public ProductResponseDTO uploadProductImage(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        String imageUrl = cloudinaryService.uploadImage(file);
        product.setImageUrl(imageUrl);
        Product updatedProduct = productRepository.save(product);

        return mapToDTO(updatedProduct);
    }

    // FR-PROD-03, FR-PROD-04, FR-PROD-05: List, search, filter & paginate active products
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getActiveProducts(Long categoryId, String search, Pageable pageable) {
        Page<Product> products;

        if (categoryId != null) {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else if (search != null && !search.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(search, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        return products.map(this::mapToDTO);
    }

    // FR-PROD-06: Fetch single active product
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Product not found or inactive with id: " + id));
        return mapToDTO(product);
    }

    // FR-PROD-07: Soft delete / deactivate product (Admin Only)
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    private ProductResponseDTO mapToDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .active(product.getActive())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }
}
