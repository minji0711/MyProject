package com.example.mealkit.dto;

import com.example.mealkit.domain.Product;

public record ProductRatingDto(Product product, Double averageRating) {}
