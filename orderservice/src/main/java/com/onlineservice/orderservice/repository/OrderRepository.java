package com.onlineservice.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlineservice.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
