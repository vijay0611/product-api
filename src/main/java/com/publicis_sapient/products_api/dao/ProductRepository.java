package com.publicis_sapient.products_api.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.publicis_sapient.products_api.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findBySku(String sku);

	List<Product> findByCategoryIgnoreCase(String category);

	@Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
	List<String> findDistinctCategories();
}
