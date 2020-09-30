package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {

    Page<RecipeIngredient> findByRecipeName(String name, Pageable pageable);

    int countByRecipeName(String name);
}