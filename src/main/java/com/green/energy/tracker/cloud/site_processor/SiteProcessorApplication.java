package com.green.energy.tracker.cloud.site_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SiteProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteProcessorApplication.class, args);
	}

}
