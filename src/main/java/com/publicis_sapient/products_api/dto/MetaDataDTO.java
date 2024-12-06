package com.publicis_sapient.products_api.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MetaDataDTO {
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String barcode;
	private String qrCode;
}
