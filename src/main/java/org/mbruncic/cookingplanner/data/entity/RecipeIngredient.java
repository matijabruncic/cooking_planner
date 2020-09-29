package org.mbruncic.cookingplanner.data.entity;

import org.mbruncic.cookingplanner.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class RecipeIngredient extends AbstractEntity {
private int amount;
public int getAmount() {
    return amount;
}
public void setAmount(int amount) {
    this.amount = amount;
}

private Recipe recipe;
@ManyToOne(fetch = FetchType.EAGER)
public Recipe getRecipe() {
    return recipe;
}
public void setRecipe(Recipe recipe) {
    this.recipe = recipe;
}

private Ingredient ingredient;
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "ingredient_id")
public Ingredient getIngredient() {
    return ingredient;
}
public void setIngredient(Ingredient ingredient) {
    this.ingredient = ingredient;
}
}
