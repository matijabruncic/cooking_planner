package org.mbruncic.cookingplanner.data.entity;

import com.vaadin.flow.data.binder.PropertyId;
import org.mbruncic.cookingplanner.data.AbstractEntity;
import org.mbruncic.cookingplanner.views.ingredient.Unit;

import javax.persistence.Entity;

@Entity
public class Ingredient extends AbstractEntity {

@PropertyId("name")
private String name;
public String getName() {
  return name;
}
public void setName(String name) {
  this.name = name;
}

@PropertyId("unit")
private Unit unit;
public Unit getUnit() {
  return unit;
}
public void setUnit(Unit unit) {
  this.unit = unit;
}

}
