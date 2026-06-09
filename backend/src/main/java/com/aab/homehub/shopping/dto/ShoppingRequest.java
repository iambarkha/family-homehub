package com.aab.homehub.shopping.dto;

import com.aab.homehub.shopping.ShoppingCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingRequest (
@NotBlank(message = "Item name is required")
String name,

String quantity,

@NotNull(message = "Category is required")
ShoppingCategory category
) {}

