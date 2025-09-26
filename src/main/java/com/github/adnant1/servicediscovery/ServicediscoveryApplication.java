package com.github.adnant1.servicediscovery;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ServiceDiscoveryApplication {

	public static void main(String[] args) {
		/*
		 * Run as a non-web application since this is a gRPC server
		 */
		new SpringApplicationBuilder(ServiceDiscoveryApplication.class)
			.web(WebApplicationType.NONE)
			.run(args);
	}

}
