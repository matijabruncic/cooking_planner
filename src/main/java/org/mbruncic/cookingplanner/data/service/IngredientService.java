package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class IngredientService extends CrudService<Ingredient, Integer> {

    private IngredientRepository repository;

    public IngredientService(@Autowired IngredientRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IngredientRepository getRepository() {
        return repository;
    }

}
