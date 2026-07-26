package com.eyatrooz.transaction_monitoring.case_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class CaseManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaseManagementServiceApplication.class, args);
	}
}
