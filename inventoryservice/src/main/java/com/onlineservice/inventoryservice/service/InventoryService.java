package com.onlineservice.inventoryservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onlineservice.inventoryservice.dto.InventoryResponse;
import com.onlineservice.inventoryservice.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
	
	@Autowired
	private InventoryRepository inventoryRepository;
	
	@Transactional
	@SneakyThrows
	public List<InventoryResponse> isInStock(List<String> skuCode) {
		log.info("Checking Inventory");
		return inventoryRepository.findBySkuCodeIn(skuCode).stream()
				.map(inventory -> 
					InventoryResponse.builder()
					.skuCode(inventory.getSkuCode())
					.isInStock(inventory.getQuantity() > 0)
					.build()
				).toList();
	}

}
