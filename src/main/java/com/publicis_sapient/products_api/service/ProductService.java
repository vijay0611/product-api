package com.publicis_sapient.products_api.service;

import com.publicis_sapient.products_api.dao.ProductRepository;
import com.publicis_sapient.products_api.dto.ProductDTO;
import com.publicis_sapient.products_api.entity.Product;
import com.publicis_sapient.products_api.entity.ProductResponse;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProductService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository repository;
	private final RestTemplate restTemplate;
	private final ModelMapper modelMapper;

	@Value("${external.api.products}")
	private String productsApiUrl;

	public ProductService(ProductRepository repository, RestTemplate restTemplate, ModelMapper modelMapper) {
		this.repository = repository;
		this.restTemplate = restTemplate;
		this.modelMapper = modelMapper;
	}

	@Async
	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	@Retry(name = "productApi", fallbackMethod = "loadProductsFallback")
	@CircuitBreaker(name = "productApi", fallbackMethod = "loadProductsFallback")
	public CompletableFuture<Void> loadProducts() {
		LOGGER.info("Fetching products from external API: {}", productsApiUrl);

		try {

			ProductResponse productResponse = restTemplate.getForObject(productsApiUrl, ProductResponse.class);

			if (productResponse == null || productResponse.getProducts() == null
					|| productResponse.getProducts().isEmpty()) {
				LOGGER.warn("No products found in the response from external API.");
				return CompletableFuture.completedFuture(null);
			}

			LOGGER.info("Successfully fetched {} products from external API.", productResponse.getProducts().size());

			List<Product> productsToSave = productResponse.getProducts().parallelStream().map(this::convertToEntity) // Convert
																														// each
																														// ProductDTO
																														// to
																														// Product
																														// entity
					.collect(Collectors.toList()); // Collect all converted products

			if (productsToSave.isEmpty()) {
				LOGGER.warn("No products to save.");
				return CompletableFuture.completedFuture(null);
			}

			int batchSize = 15;
			List<List<Product>> batches = partitionList(productsToSave, batchSize);

			List<CompletableFuture<Void>> futures = batches.stream()
					.map(batch -> CompletableFuture.runAsync(() -> saveBatch(batch))) // Asynchronous saving of each
																						// batch
					.collect(Collectors.toList());

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

			LOGGER.info("All products have been saved successfully.");

		} catch (Exception e) {
			LOGGER.error("Error occurred while fetching products from external API: {}", e.getMessage(), e);
			throw e;
		}

		return CompletableFuture.completedFuture(null);
	}

	private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
		List<List<T>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += batchSize) {
			partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
		}
		return partitions;
	}

	private void saveBatch(List<Product> batch) {
		try {
			repository.saveAll(batch);
			repository.flush();
		} catch (Exception e) {
			LOGGER.error("Failed to save batch of products: {}", e.getMessage(), e);
		}
	}

	@Cacheable(value = "products", key = "{#category?.toLowerCase(), #searchTerm?.toLowerCase(), #sortOrder?.toLowerCase()}")
	public List<Product> findProducts(String category, String searchTerm, String sortOrder) {
		LOGGER.info("Retrieving products with filters. Category: {}, SearchTerm: {}, SortOrder: {}", category,
				searchTerm, sortOrder);

		List<Product> products = repository.findAll(); // Start with all products

		// Apply category filter if provided
		if (category != null && !category.isEmpty()) {
			products = products.stream().filter(product -> product.getCategory().equalsIgnoreCase(category))
					.collect(Collectors.toList());
			LOGGER.debug("Filtered products by category '{}'.", category);
		}

		// Apply searchTerm filter if provided (by name, SKU, or ID)
		if (searchTerm != null && !searchTerm.isEmpty()) {
			products = products.stream()
					.filter(product -> product.getTitle().toLowerCase().contains(searchTerm.toLowerCase())
							|| product.getSku().toLowerCase().contains(searchTerm.toLowerCase())
							|| product.getId().toString().equals(searchTerm)) // Can filter by name, SKU, or ID
					.collect(Collectors.toList());
			LOGGER.debug("Filtered products by search term '{}'.", searchTerm);
		}

		// Apply sorting if provided
		if (sortOrder != null && !sortOrder.isEmpty()) {
			if ("asc".equalsIgnoreCase(sortOrder)) {
				products.sort(Comparator.comparing(Product::getPrice));
				LOGGER.debug("Sorted products by price in ascending order.");
			} else if ("desc".equalsIgnoreCase(sortOrder)) {
				products.sort(Comparator.comparing(Product::getPrice).reversed());
				LOGGER.debug("Sorted products by price in descending order.");
			}
		}

		LOGGER.debug("Found {} products after applying filters and sorting.", products.size());
		return products;
	}

	@Cacheable("products")
	public List<Product> findAll() {
		LOGGER.info("Retrieving all products from the database.");
		List<Product> products = repository.findAll();
		LOGGER.debug("Found {} products in the database.", products.size());
		return products;
	}

	@Cacheable(value = "productsByCategory", key = "#category.toLowerCase()")
	public List<Product> findByCategory(String category) {
		LOGGER.info("Retrieving products by category: {}", category);
		List<Product> products = repository.findByCategoryIgnoreCase(category);
		LOGGER.debug("Found {} products for category '{}'.", products.size(), category);
		return products;
	}

	public Optional<Product> findById(Long id) {
		LOGGER.info("Searching for product with ID: {}", id);
		Optional<Product> product = repository.findById(id);
		if (product.isPresent()) {
			LOGGER.debug("Product found: {}", product.get());
		} else {
			LOGGER.warn("No product found with ID: {}", id);
		}
		return product;
	}

	public Optional<Product> findBySku(String sku) {
		LOGGER.info("Searching for product with SKU: {}", sku);
		Optional<Product> product = repository.findBySku(sku);
		if (product.isPresent()) {
			LOGGER.debug("Product found: {}", product.get());
		} else {
			LOGGER.warn("No product found with SKU: {}", sku);
		}
		return product;
	}

	public List<Product> sortByPrice(String direction) {
		LOGGER.info("Sorting products by price in {} order.", direction);
		List<Product> products = direction.equalsIgnoreCase("asc")
				? repository.findAll(Sort.by(Sort.Direction.ASC, "price"))
				: repository.findAll(Sort.by(Sort.Direction.DESC, "price"));
		LOGGER.debug("Found {} products after sorting.", products.size());
		return products;
	}

	public List<String> getCategories() {
		LOGGER.info("Fetching unique categories from the database.");
		List<String> categories = repository.findDistinctCategories();
		if (categories.isEmpty()) {
			LOGGER.warn("No categories found in the database.");
		} else {
			LOGGER.debug("Found {} unique categories: {}", categories.size(), categories);
		}
		return categories;
	}

	public Product convertToEntity(ProductDTO productDTO) {
		LOGGER.debug("Converting ProductDTO to Product entity: {}", productDTO);
		Product product = modelMapper.map(productDTO, Product.class);
		product.setId(null);
		LOGGER.debug("Converted Product entity: {}", product);
		return product;
	}

	public ProductDTO convertToDTO(Product product) {
		LOGGER.debug("Converting Product entity to ProductDTO: {}", product);
		ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
		LOGGER.debug("Converted ProductDTO: {}", productDTO);
		return productDTO;
	}

	public void loadProductsFallback(Throwable throwable) {
		LOGGER.error("Fallback invoked for loadProducts due to: {}", throwable.getMessage(), throwable);
	}
}
