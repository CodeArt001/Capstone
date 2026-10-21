package com.example.demo.config;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        
        if (categoryRepository.count() == 0) {
            Category sneakers = Category.builder().name("Sneakers").active(true).build();
            Category boots = Category.builder().name("Boots").active(true).build();
            Category sandals = Category.builder().name("Sandals").active(true).build();

            categoryRepository.saveAll(List.of(sneakers, boots, sandals));
            System.out.println("Categories seeded successfully!");
        }

        if (productRepository.count() == 0) {
            Category sneakers = categoryRepository.findByName("Sneakers")
                    .orElseGet(() -> categoryRepository.findAll().get(0));

            List<Product> defaultProducts = List.of(
                Product.builder()
                        .name("Shoe 1")
                        .description("Experience unmatched comfort and style. Built for performance and designed for everyday wear.")
                        .price(new BigDecimal("250.00"))
                        .stockQuantity(20)
                        .active(true)
                        .category(sneakers)
                        .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff") // Reliable sample shoe image
                        .build(),

                Product.builder()
                        .name("Shoe 2")
                        .description("Sleek lightweight design suitable for all conditions.")
                        .price(new BigDecimal("180.00"))
                        .stockQuantity(15)
                        .active(true)
                        .category(sneakers)
                        .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a") // Reliable sample shoe image
                        .build()
            );

            productRepository.saveAll(defaultProducts);
            System.out.println("Default products seeded into PostgreSQL database!");
        }
    }
}