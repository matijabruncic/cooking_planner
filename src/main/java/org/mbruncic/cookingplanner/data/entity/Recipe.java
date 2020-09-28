package org.mbruncic.cookingplanner.data.entity;

import org.mbruncic.cookingplanner.data.AbstractEntity;

import javax.persistence.Entity;

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
}
