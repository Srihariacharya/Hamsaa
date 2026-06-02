package com.contactpro.contactpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContactproApplication {

	public static void main(String[] args) {
		// Forcefully override any broken Render dashboard environment variables
		System.setProperty("spring.datasource.url", "jdbc:postgresql://dpg-d8fe0128qa3s738tl1cg-a.oregon-postgres.render.com:5432/hamsaa_db_new?sslmode=require");
		System.setProperty("spring.datasource.username", "hamsaa_db_new_user");
		System.setProperty("spring.datasource.password", "1ZNi9fEKlNSL2iGX1hVaRaTBa4wtsTiN");
		System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
		System.setProperty("spring.datasource.hikari.maximum-pool-size", "3");
		
		SpringApplication.run(ContactproApplication.class, args);
	}

}
