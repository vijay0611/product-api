package com.publicis_sapient.products_api;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import com.publicis_sapient.products_api.service.ProductService;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class ProductsApiApplication implements CommandLineRunner {

	@Autowired
	ProductService productService;

	public static void main(String[] args) {
		SpringApplication.run(ProductsApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		CompletableFuture.runAsync(() -> {
			try {
				productService.loadProducts();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
