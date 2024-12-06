package com.publicis_sapient.products_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

	private Long id;

	private String title;

	private String description;

	// Keeping category as String
	private String category;

	private Double price;

	private Double discountPercentage;

	private Double rating;

	private Integer stock;

	private String brand;

	private String sku;

	private Double weight;

	private String warrantyInformation;

	private String shippingInformation;

	private String availabilityStatus;

	private String returnPolicy;

	private Integer minimumOrderQuantity;

	private String thumbnail;

	private List<String> tags;

	private DimensionsDTO dimensions;

	private MetaDataDTO meta;

	private List<String> images;

	private List<ReviewDTO> reviews;
}
