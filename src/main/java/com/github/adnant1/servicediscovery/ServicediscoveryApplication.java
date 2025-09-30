package com.github.adnant1.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ServiceDiscoveryApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceDiscoveryApplication.class, args);
	}
}
