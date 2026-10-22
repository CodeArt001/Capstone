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
            Category sneakers = Category.builder()
                    .name("Sneakers")
                    .active(true)
                    .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff")
                    .build();

            Category casual = Category.builder()
                    .name("Casual")
                    .active(true)
                    .imageUrl("https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77")
                    .build();

            Category formal = Category.builder()
                    .name("Formal")
                    .active(true)
                    .imageUrl("https://images.unsplash.com/photo-1614252235316-8c857d38b5f4")
                    .build();

            Category limitedEdition = Category.builder()
                    .name("Limited Edition")
                    .active(true)
                    .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a")
                    .build();

            Category leather = Category.builder()
                    .name("Leather")
                    .active(true)
                    .imageUrl("https://images.unsplash.com/photo-1533867617858-e7b97e060509")
                    .build();

            categoryRepository.saveAll(List.of(sneakers, casual, formal, limitedEdition, leather));
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
                        .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff")
                        .build(),

                Product.builder()
                        .name("Shoe 2")
                        .description("Sleek lightweight design suitable for all conditions.")
                        .price(new BigDecimal("180.00"))
                        .stockQuantity(15)
                        .active(true)
                        .category(sneakers)
                        .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a")
                        .build()
            );

            productRepository.saveAll(defaultProducts);
            System.out.println("Default products seeded into PostgreSQL database!");
        }
    }
}