package com.publicis_sapient.products_api;

import com.publicis_sapient.products_api.entity.Product;
import com.publicis_sapient.products_api.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.publicis_sapient.products_api.controller.ProductController;

class ProductControllerTest {

	@Mock
	private ProductService productService;

	@InjectMocks
	private ProductController productController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@SuppressWarnings("null")
	@Test
	void testGetProducts() {
		Product product = new Product();
		product.setId(1L);
		product.setTitle("Test Product");

		List<Product> products = Arrays.asList(product);

		when(productService.findAll()).thenReturn(products);

		ResponseEntity<List<Product>> response = productController.getProducts(null, null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
		assertEquals("Test Product", response.getBody().get(0).getTitle());
	}

	@SuppressWarnings("null")
	@Test
	void testGetProductsByCategory() {
		String category = "Electronics";
		Product product = new Product();
		product.setId(1L);
		product.setCategory(category);
		product.setTitle("Test Product");

		List<Product> products = Arrays.asList(product);

		when(productService.findByCategory(category)).thenReturn(products);

		ResponseEntity<List<Product>> response = productController.getProducts(category, null, null);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
		assertEquals(category, response.getBody().get(0).getCategory());
	}

	@SuppressWarnings("null")
	@Test
	void testGetProductById() {
		Long id = 1L;
		Product product = new Product();
		product.setId(id);
		product.setTitle("Test Product");

		when(productService.findById(id)).thenReturn(Optional.of(product));

		ResponseEntity<Product> response = productController.getById(id);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Test Product", response.getBody().getTitle());
	}

	@Test
	void testGetProductById_NotFound() {
		Long id = 999L;

		when(productService.findById(id)).thenReturn(Optional.empty());

		ResponseEntity<Product> response = productController.getById(id);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@SuppressWarnings("null")
	@Test
	void testGetProductBySku() {
		String sku = "SKU123";
		Product product = new Product();
		product.setSku(sku);
		product.setTitle("Test Product");

		when(productService.findBySku(sku)).thenReturn(Optional.of(product));

		ResponseEntity<Product> response = productController.getBySku(sku);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(sku, response.getBody().getSku());
	}

	@Test
	void testGetProductBySku_NotFound() {
		String sku = "SKU999";

		when(productService.findBySku(sku)).thenReturn(Optional.empty());

		ResponseEntity<Product> response = productController.getBySku(sku);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@SuppressWarnings("null")
	@Test
	void testGetSortedByPrice() {
		Product product1 = new Product();
		product1.setId(1L);
		product1.setTitle("Product 1");
		product1.setPrice(20.0);

		Product product2 = new Product();
		product2.setId(2L);
		product2.setTitle("Product 2");
		product2.setPrice(30.0);

		List<Product> products = Arrays.asList(product1, product2);

		when(productService.sortByPrice("asc")).thenReturn(products);

		ResponseEntity<List<Product>> response = productController.getSortedByPrice("asc");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(2, response.getBody().size());
		assertEquals("Product 1", response.getBody().get(0).getTitle());
	}

	@Test
	void testGetSortedByPrice_InvalidDirection() {
		ResponseEntity<List<Product>> response = productController.getSortedByPrice("invalid");

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}
}
