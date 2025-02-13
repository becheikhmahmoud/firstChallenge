package com.mahmoud.firstChallenge.service;

import com.mahmoud.firstChallenge.model.Product;
import com.mahmoud.firstChallenge.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Flux<Product> getProductsByEan(String ean) {
        return productRepository.findByEan(ean);
    }

    public Mono<Product> getProductByEanAndStore(String ean, String storeId) {
        return productRepository.findByEanAndStoreId(ean, storeId);
    }

    public Mono<Product> saveProduct(Product product) {
        return productRepository.save(product);
    }
}
