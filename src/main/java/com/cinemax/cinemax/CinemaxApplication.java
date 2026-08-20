package com.cinemax.cinemax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Enable Spring Boot configuration and discover components in this package
@SpringBootApplication
public class CinemaxApplication {

	public static void main(String[] args) {
		// Start CineMax and its embedded web server
		SpringApplication.run(CinemaxApplication.class, args);
	}

}
