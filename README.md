# WebFlux Product Microservice

This is a Spring Boot WebFlux microservice that retrieves products from a MongoDB database.

## Endpoints

- **GET** `/products/ean/{ean}` → Get all products with the specified EAN.
- **GET** `/products/ean/{ean}/store/{storeId}` → Get a product by EAN and store ID.

## How to Run

```sh
mvn spring-boot:run
