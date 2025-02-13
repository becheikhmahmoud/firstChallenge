package com.mahmoud.firstChallenge;

import com.mahmoud.firstChallenge.controller.ProductController;
import com.mahmoud.firstChallenge.model.Product;
import com.mahmoud.firstChallenge.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProductControllerTest {
    private final ProductService productService = Mockito.mock(ProductService.class);
    private final WebTestClient webTestClient = WebTestClient.bindToController(new ProductController(productService)).build();

    @Test
    public void testGetProductsByEan() {
        Product product = new Product();
        product.setEan("123456789");
        product.setStoreId("1");
        product.setName("Produit A");
        product.setPrice(10.99);

        Mockito.when(productService.getProductsByEan("123456789")).thenReturn(Flux.just(product));

        webTestClient.get().uri("/products/ean/123456789")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Product.class).hasSize(1);
    }

    @Test
    public void testGetProductByEanAndStore() {
        Product product = new Product();
        product.setEan("123456789");
        product.setStoreId("1");
        product.setName("Produit A");
        product.setPrice(10.99);

        Mockito.when(productService.getProductByEanAndStore("123456789", "1")).thenReturn(Mono.just(product));

        webTestClient.get().uri("/products/ean/123456789/store/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Product.class);
    }
}