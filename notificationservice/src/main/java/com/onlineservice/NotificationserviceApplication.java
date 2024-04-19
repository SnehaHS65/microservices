package com.onlineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class NotificationserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationserviceApplication.class, args);
  }

  @KafkaListener(topics="notificationTopic")
  public void handleNotification(OrderPlacedEvent orderPlacedEvent) {
	  //send out an email notification
	  log.info("Received notification for Order - {}", orderPlacedEvent.getOrderNumber());
  }
}
