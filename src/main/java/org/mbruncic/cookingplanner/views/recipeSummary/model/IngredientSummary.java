package org.mbruncic.cookingplanner.views.recipeSummary.model;

import org.mbruncic.cookingplanner.data.entity.Ingredient;

public class IngredientSummary {

    private final Ingredient ingredient;
    private final Integer amount;

    public IngredientSummary(Ingredient ingredient, Integer amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Integer getAmount() {
        return amount;
    }
}
