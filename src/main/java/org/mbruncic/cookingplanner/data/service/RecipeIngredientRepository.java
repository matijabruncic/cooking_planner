package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {

}