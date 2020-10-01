package org.mbruncic.cookingplanner.views.recipe;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.mbruncic.cookingplanner.data.entity.Recipe;
import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.mbruncic.cookingplanner.data.service.RecipeService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.Collections;
import java.util.Optional;

@Route(value = "recipe", layout = MainView.class)
@PageTitle("Recipe")
@CssImport("./styles/views/recipe/recipe-view.css")
@RouteAlias(value = "", layout = MainView.class)
public class RecipeView extends Div {

    private final Grid<Recipe> grid;

    private final TextField name = new TextField();
    private final TextArea description = new TextArea();
    private final Grid<RecipeIngredient> recipeIngredients;
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    private final Binder<Recipe> binder;
    private Recipe recipe = new Recipe();

    private final RecipeService recipeService;

    public RecipeView(@Autowired RecipeService recipeService) {
        setId("recipe-view");
        this.recipeService = recipeService;
        // Configure Grid
        grid = new Grid<>(Recipe.class);
        grid.setColumns("name");
        grid.setDataProvider(new CrudServiceDataProvider<Recipe, Void>(this.recipeService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        recipeIngredients = new Grid<>(RecipeIngredient.class);
        recipeIngredients.setColumns("amount", "ingredient.unit", "ingredient.name");

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<Recipe> recipeFromBackend = recipeService.get(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if (recipeFromBackend.isPresent()) {
                    populateForm(recipeFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new Binder<>(Recipe.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.recipe == null) {
                    this.recipe = new Recipe();
                }
                binder.writeBean(this.recipe);
                this.recipeService.update(this.recipe);
                clearForm();
                refreshGrid();
                Notification.show("Recipe details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the recipe details.");
            }
        });

        delete.addClickListener(e -> {
            if (recipe != null) {
                this.recipeService.delete(recipe.getId());
                clearForm();
                refreshGrid();
                Notification.show("Recipe deleted.");
            }
        });

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);
    }

    private void populateIngredients(Recipe recipe) {
        if (recipe == null) {
            recipeIngredients.setItems(Collections.emptyList());
        } else {
            recipeIngredients.setItems(recipe.getIngredients());
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        addFormItem(editorDiv, formLayout, name, "Name");
        addFormItem(editorDiv, formLayout, description, "Description");
        editorDiv.add(formLayout);

        createIngredientsLayout(editorLayoutDiv);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createIngredientsLayout(Div editorLayoutDiv) {
        HorizontalLayout ingredientsLayout = new HorizontalLayout();
        ingredientsLayout.setId("ingredients-layout");
        ingredientsLayout.setWidthFull();
        ingredientsLayout.setSpacing(true);
        ingredientsLayout.add(recipeIngredients);
        editorLayoutDiv.add(ingredientsLayout);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void addFormItem(Div wrapper, FormLayout formLayout, AbstractField field, String fieldName) {
        formLayout.addFormItem(field, fieldName);
        wrapper.add(formLayout);
        field.getElement().getClassList().add("full-width");
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Recipe value) {
        this.recipe = value;
        populateIngredients(recipe);
        binder.readBean(this.recipe);
    }
}
