package org.mbruncic.cookingplanner.data.entity;

import org.mbruncic.cookingplanner.data.AbstractEntity;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Recipe extends AbstractEntity {

private String name;
public String getName() {
  return name;
}
public void setName(String name) {
  this.name = name;
}
private String description;
public String getDescription() {
  return description;
}
public void setDescription(String description) {
  this.description = description;
}
private Set<RecipeIngredient> ingredients;
@OneToMany( cascade = CascadeType.ALL, fetch = FetchType.EAGER )
@JoinColumn( name = "recipe_id")
public Set<RecipeIngredient> getIngredients() {
  return ingredients;
}
public void setIngredients(Set<RecipeIngredient> ingredients) {
  this.ingredients = ingredients;
}
}
