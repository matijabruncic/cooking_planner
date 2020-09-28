package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.Recipe;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

}