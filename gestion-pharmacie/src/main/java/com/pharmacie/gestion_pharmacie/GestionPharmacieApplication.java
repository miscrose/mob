package com.pharmacie.gestion_pharmacie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionPharmacieApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionPharmacieApplication.class, args);
	}

}
