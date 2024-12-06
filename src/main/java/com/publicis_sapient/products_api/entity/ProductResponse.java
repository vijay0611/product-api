package com.publicis_sapient.products_api.entity;

import java.util.ArrayList;
import java.util.List;

import com.publicis_sapient.products_api.dto.ProductDTO;

import lombok.Data;

@Data
public class ProductResponse {
	private List<ProductDTO> products = new ArrayList<ProductDTO>();

}
