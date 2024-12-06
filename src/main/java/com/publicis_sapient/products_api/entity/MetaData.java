package com.publicis_sapient.products_api.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDateTime;

@Embeddable
@Data
public class MetaData {
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String barcode;
	private String qrCode;
}
