package com.aab.homehub.shopping;

import com.aab.homehub.shopping.dto.ShoppingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ShoppingMapper {
    //ShoppingMapper INSTANCE = Mappers.getMapper(ShoppingMapper.class);

    ShoppingResponse toResponse(ShoppingItem save);
}
