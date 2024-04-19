package com.onlineservice.productservice;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineservice.productservice.dto.ProductRequest;
import com.onlineservice.productservice.dto.ProductResponse;
import com.onlineservice.productservice.repository.ProductRepository;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ProductServiceApplicationTests {
	
	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.7");
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private ProductRepository productRepository;
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}
	

	@Test
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
					.contentType(MediaType.APPLICATION_JSON)
					.content(productRequestString))
				.andExpect(status().isCreated());
		Assertions.assertEquals(1,productRepository.findAll().size());
				
	}


	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("iPhone 13")
				.description("iPhone 13")
				.price(BigDecimal.valueOf(1200))
				.build();
	}
 
//	@Test
//	void shouldGetProduct() throws Exception {
//		MvcResult mvcResult = mockMvc.perform(get("/api/product/"))
//	            .andExpect(jsonPath("$.name").value("iPhone 13"))
//	            .andExpect(jsonPath("$.description").value("iPhone 13"))
//	            .andExpect(jsonPath("$.price").value(1200))
//	            .andReturn();
//		
//		String responseBody = mvcResult.getResponse().getContentAsString();
//		System.out.println(responseBody);
//	    ProductResponse productResponse = objectMapper.readValue(responseBody, ProductResponse.class);
//
//	    // You can perform further assertions on the productResponse if needed
//	    Assertions.assertEquals("iPhone 13", productResponse.getName());
//	    Assertions.assertEquals("iPhone 13", productResponse.getDescription());
//	    Assertions.assertEquals(BigDecimal.valueOf(1200), productResponse.getPrice());
//	}
}
