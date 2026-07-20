package com.eyatrooz.transaction_monitoring.rule_engine_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RuleEngineServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RuleEngineServiceApplication.class, args);
	}

}
