package com.mahmoud.firstChallenge.controller;

import com.mahmoud.firstChallenge.model.Product;
import com.mahmoud.firstChallenge.service.ProductService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products/ean")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{ean}")
    public Flux<Product> getProductsByEan(@PathVariable String ean) {
        return productService.getProductsByEan(ean);
    }

    @GetMapping("/{ean}/store/{storeId}")
    public Mono<Product> getProductByEanAndStore(@PathVariable String ean, @PathVariable String storeId) {
        return productService.getProductByEanAndStore(ean, storeId);
    }

    @PostMapping
    public Mono<Product> addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }
}
