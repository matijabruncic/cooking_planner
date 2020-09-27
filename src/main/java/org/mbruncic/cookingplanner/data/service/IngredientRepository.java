package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

}