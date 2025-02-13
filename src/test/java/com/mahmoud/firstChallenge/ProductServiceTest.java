package com.mahmoud.firstChallenge;

import com.mahmoud.firstChallenge.model.Product;
import com.mahmoud.firstChallenge.repository.ProductRepository;
import com.mahmoud.firstChallenge.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class ProductServiceTest {
    private final ProductRepository productRepository = Mockito.mock(ProductRepository.class);
    private final ProductService productService = new ProductService(productRepository);

    @Test
    public void testGetProductsByEan() {
        Product product1 = new Product();
        product1.setEan("123456789");
        product1.setStoreId("1");
        product1.setName("Produit A");
        product1.setPrice(10.99);

        Product product2 = new Product();
        product2.setEan("123456789");
        product2.setStoreId("2");
        product2.setName("Produit B");
        product2.setPrice(12.99);

        Mockito.when(productRepository.findByEan("123456789"))
                .thenReturn(Flux.just(product1, product2));

        Flux<Product> result = productService.getProductsByEan("123456789");

        StepVerifier.create(result)
                .expectNext(product1, product2)
                .verifyComplete();
    }

    @Test
    public void testGetProductByEanAndStore() {
        Product product = new Product();
        product.setEan("123456789");
        product.setStoreId("1");
        product.setName("Produit A");
        product.setPrice(10.99);

        Mockito.when(productRepository.findByEanAndStoreId("123456789", "1"))
                .thenReturn(Mono.just(product));

        Mono<Product> result = productService.getProductByEanAndStore("123456789", "1");

        StepVerifier.create(result)
                .expectNext(product)
                .verifyComplete();
    }
}
