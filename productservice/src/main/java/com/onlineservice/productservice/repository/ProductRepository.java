package com.onlineservice.productservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.onlineservice.productservice.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

}
