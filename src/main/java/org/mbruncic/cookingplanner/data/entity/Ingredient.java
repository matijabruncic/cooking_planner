package org.mbruncic.cookingplanner.data.entity;

import org.mbruncic.cookingplanner.data.AbstractEntity;
import org.mbruncic.cookingplanner.views.ingredient.Unit;

import javax.persistence.Entity;

@Entity
public class Ingredient extends AbstractEntity {

private String name;
public String getName() {
  return name;
}
public void setName(String name) {
  this.name = name;
}
private Unit unit;
public Unit getUnit() {
  return unit;
}
public void setUnit(Unit unit) {
  this.unit = unit;
}

}
