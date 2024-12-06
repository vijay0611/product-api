package com.publicis_sapient.products_api.controller;

import com.publicis_sapient.products_api.entity.Product;
import com.publicis_sapient.products_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Slf4j
@Tag(name = "Products", description = "Operations related to Products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * Loads products from an external API into the database.
	 */
	/*
	 * @Operation(summary = "Load products", description =
	 * "Load products into the database from external API.")
	 * 
	 * @GetMapping("/load") public ResponseEntity<String> loadProducts() { try {
	 * log.info("Loading products from external API...");
	 * productService.loadProducts(); log.info("Products loaded successfully.");
	 * return ResponseEntity.ok("Products loaded successfully!"); } catch (Exception
	 * e) { log.error("Error loading products: {}", e.getMessage(), e); return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Failed to load products. Please try again later."); } }
	 */

	/**
	 * Retrieves all products or filters them by category.
	 */
	@Operation(summary = "Get products", description = "Retrieve a list of products, optionally filtered by category, name, SKU, and sort order.")
	@GetMapping
	public ResponseEntity<List<Product>> getProducts(
			@RequestParam(value = "category", required = false) @Parameter(description = "Category to filter products") String category,
			@RequestParam(value = "searchTerm", required = false) @Parameter(description = "Search term to filter by product name, ID, or SKU") String searchTerm,
			@RequestParam(value = "sortOrder", required = false) @Parameter(description = "Sort order for price: 'asc' or 'desc'") String sortOrder) {

		// Handle empty category or search term
		if ((category != null && category.isEmpty()) || (searchTerm != null && searchTerm.isEmpty())) {
			log.warn("Empty filter value provided.");
			return ResponseEntity.badRequest().body(null);
		}

		log.info("Fetching products. Category: {}, SearchTerm: {}, SortOrder: {}", category, searchTerm, sortOrder);
		List<Product> products;
		try {
			// Apply filters and sorting based on provided parameters
			if (category == null && searchTerm == null && sortOrder == null) {
				// If no filters are provided, return all products
				products = productService.findAll();
				log.info("Fetched all products. Total: {}", products.size());
			} else {
				// Filter products based on category, searchTerm, and sortOrder
				products = productService.findProducts(category, searchTerm, sortOrder);
				log.info("Fetched {} products based on filters.", products.size());
			}

			return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofMinutes(5))) // Enable caching
					.body(products);
		} catch (Exception e) {
			log.error("Error fetching products: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Retrieves a product by its ID.
	 */
	@Operation(summary = "Get product by ID", description = "Retrieve a product by its unique ID.")
	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable Long id) {
		if (id <= 0) {
			log.warn("Invalid product ID provided: {}", id);
			return ResponseEntity.badRequest().body(null);
		}

		log.info("Fetching product with ID: {}", id);
		return productService.findById(id).map(product -> {
			log.info("Product found: {}", product);
			return ResponseEntity.ok(product);
		}).orElseGet(() -> {
			log.warn("Product not found with ID: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		});
	}

	/**
	 * Retrieves a product by its SKU.
	 */
	@Operation(summary = "Get product by SKU", description = "Retrieve a product by its SKU (Stock Keeping Unit).")
	@GetMapping("/sku/{sku}")
	public ResponseEntity<Product> getBySku(@PathVariable String sku) {
		if (sku == null || sku.trim().isEmpty()) {
			log.warn("Invalid SKU provided: {}", sku);
			return ResponseEntity.badRequest().body(null);
		}

		log.info("Fetching product with SKU: {}", sku);
		return productService.findBySku(sku).map(product -> {
			log.info("Product found: {}", product);
			return ResponseEntity.ok(product);
		}).orElseGet(() -> {
			log.warn("Product not found with SKU: {}", sku);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		});
	}

	/**
	 * Retrieves all products sorted by price in ascending or descending order.
	 */
	@Operation(summary = "Get sorted products", description = "Retrieve all products sorted by price in ascending or descending order.")
	@GetMapping("/sorted")
	public ResponseEntity<List<Product>> getSortedByPrice(@RequestParam String direction) {
		if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
			log.warn("Invalid sort direction: {}", direction);
			return ResponseEntity.badRequest().body(null);
		}

		log.info("Fetching products sorted by price in '{}' order.", direction);
		try {
			List<Product> sortedProducts = productService.sortByPrice(direction);
			log.info("Fetched {} products sorted by price.", sortedProducts.size());
			return ResponseEntity.ok(sortedProducts);
		} catch (Exception e) {
			log.error("Error fetching sorted products: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Retrieves a list of unique product categories.
	 */
	@Operation(summary = "Get categories", description = "Retrieve a list of all unique product categories.")
	@GetMapping("/categories")
	public ResponseEntity<List<String>> getCategories() {
		log.info("Fetching unique product categories.");
		try {
			List<String> categories = productService.getCategories();
			if (categories.isEmpty()) {
				log.info("No categories found.");
				return ResponseEntity.noContent().build(); // 204 No Content
			}
			log.info("Fetched {} unique categories.", categories.size());
			return ResponseEntity.ok(categories);
		} catch (Exception e) {
			log.error("Error fetching categories: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
