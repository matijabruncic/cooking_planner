package org.mbruncic.cookingplanner.views.recipeSummary.model;

import org.mbruncic.cookingplanner.data.entity.Ingredient;

public class IngredientSummary {

    private final Ingredient ingredient;
    private final double amount;

    public IngredientSummary(Ingredient ingredient, double amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public double getAmount() {
        return amount;
    }
}
