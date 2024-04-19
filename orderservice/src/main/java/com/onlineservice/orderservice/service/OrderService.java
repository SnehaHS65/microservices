package com.onlineservice.orderservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.onlineservice.orderservice.dto.InventoryResponse;
import com.onlineservice.orderservice.dto.OrderLineItemsDto;
import com.onlineservice.orderservice.dto.OrderRequest;
import com.onlineservice.orderservice.event.OrderPlacedEvent;
import com.onlineservice.orderservice.model.Order;
import com.onlineservice.orderservice.model.OrderLineItems;
import com.onlineservice.orderservice.repository.OrderRepository;

import brave.Span;
import brave.Tracer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
	
	@Autowired
	private final OrderRepository orderRepository;
	
	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private Tracer tracer;
	
	@Autowired
	private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
	
	public String placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());
		
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();
		
		order.setOrderLineItemsList(orderLineItems);
		
		List<String> skuCodes = order.getOrderLineItemsList().stream()
				.map(OrderLineItems::getSkuCode)
				.toList();
		log.info("Calling inventory Service");
		
		Span inventoryServiceLookup = tracer.nextSpan().name("Inventory Service Lookup");
		
		try(Tracer.SpanInScope spanInScope =  tracer.withSpanInScope(inventoryServiceLookup.start())){
			//Call Inventory Service, and place order if product is in stock. inventory is running in 8082 port.
			InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
					.uri("http://inventoryservice/api/inventory",
							uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
					.retrieve()
					.bodyToMono(InventoryResponse[].class) // to convert return type to response
					.block();				   // to make synchronous comm
			
			//allMatch method checks if isInStock returns true for all the products in the order, even if One is false, it returns false
			boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
			
			if(allProductsInStock) {
				orderRepository.save(order);
				kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
				return "Order Placed Successfully";
			}
			else {
				throw new IllegalArgumentException("Product is not in stock, please try again later");
			}
		}
		finally {
			inventoryServiceLookup.abandon();
		}
		
		
		
	}
	
	private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setPrice(orderLineItemsDto.getPrice());
		orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
		orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
		
		return orderLineItems;
	}

}

