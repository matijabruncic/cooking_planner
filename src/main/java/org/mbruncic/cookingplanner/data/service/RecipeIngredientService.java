package org.mbruncic.cookingplanner.data.service;

import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.vaadin.artur.helpers.CrudService;

@Service
public class RecipeIngredientService extends CrudService<RecipeIngredient, Integer> {

    private RecipeIngredientRepository repository;

    public RecipeIngredientService(@Autowired RecipeIngredientRepository repository) {
        this.repository = repository;
    }

    @Override
    protected RecipeIngredientRepository getRepository() {
        return repository;
    }

    public Page<RecipeIngredient> findByName(String name, Pageable pageable) {
        return getRepository().findByRecipeName(name, pageable);
    }

    public int countByName(String name) {
        return getRepository().countByRecipeName(name);
    }
}
