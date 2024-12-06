package com.publicis_sapient.products_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(length = 1000)
	private String description;

	// Keeping category as a String
	private String category;

	private Double price;

	private Double discountPercentage;

	private Double rating;

	private Integer stock;

	@ElementCollection
	private List<String> tags;

	private String brand;

	@Column(unique = true)
	private String sku;

	private Double weight;

	@Embedded
	private Dimensions dimensions;

	private String warrantyInformation;

	private String shippingInformation;

	private String availabilityStatus;

	private String returnPolicy;

	private Integer minimumOrderQuantity;

	@Embedded
	private MetaData meta;

	@ElementCollection
	@CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
	private List<String> images;

	private String thumbnail;

	@ElementCollection
	@CollectionTable(name = "product_reviews", joinColumns = @JoinColumn(name = "product_id"))
	private List<Review> reviews;
}
