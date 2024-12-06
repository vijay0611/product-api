package com.publicis_sapient.products_api.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewDTO {
	private Integer rating;

	private String comment;

	private LocalDateTime date;

	private String reviewerName;

	private String reviewerEmail;
}
