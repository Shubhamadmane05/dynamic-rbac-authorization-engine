package com.app.rbac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching 
public class DynamicRbacAuthorizationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(DynamicRbacAuthorizationEngineApplication.class, args);
	}

}
