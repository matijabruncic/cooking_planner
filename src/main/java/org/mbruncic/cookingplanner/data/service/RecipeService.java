package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.Recipe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class RecipeService extends CrudService<Recipe, Integer> {

    private RecipeRepository repository;

    public RecipeService(@Autowired RecipeRepository repository) {
        this.repository = repository;
    }

    @Override
    protected RecipeRepository getRepository() {
        return repository;
    }

}
