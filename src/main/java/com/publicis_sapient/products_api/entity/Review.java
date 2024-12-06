package com.publicis_sapient.products_api.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDateTime;

@Embeddable
@Data
public class Review {

	private Integer rating;

	private String comment;

	private LocalDateTime date;

	private String reviewerName;

	private String reviewerEmail;
}
