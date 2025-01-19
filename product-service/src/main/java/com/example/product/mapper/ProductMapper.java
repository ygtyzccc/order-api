package com.example.product.mapper;

import com.example.common.dto.ProductDto;
import com.example.product.domain.Product;
import com.example.product.dto.CreateProductRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDto toDto(Product product);
    Product toEntity(CreateProductRequest request);
} 