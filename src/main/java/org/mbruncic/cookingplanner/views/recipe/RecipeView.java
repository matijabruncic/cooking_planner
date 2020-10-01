package org.mbruncic.cookingplanner.views.recipe;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

    private final Grid<Recipe> grid = new Grid<>(Recipe.class);

    private final TextField name = new TextField();
    private final TextArea description = new TextArea();
    private final Grid<RecipeIngredient> recipeIngredients = new Grid<>(RecipeIngredient.class);
    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    private final Binder<Recipe> binder;
    private Recipe recipe = new Recipe();

    private final RecipeService recipeService;

    public RecipeView(@Autowired RecipeService recipeService) {
        setId("recipe-view");
        this.recipeService = recipeService;
        binder = new Binder<>(Recipe.class);
        binder.bindInstanceFields(this);

        SplitLayout recipeLayout = new SplitLayout();
        recipeLayout.setSizeFull();

        recipeLayout.addToPrimary(createGridLayout());
        recipeLayout.addToSecondary(createEditorLayout());

        add(recipeLayout);
    }

    private ComponentEventListener<ClickEvent<Button>> buttonDeleteClickListener() {
        return e -> {
            Dialog dialog = new Dialog();

            dialog.setCloseOnEsc(false);
            dialog.setCloseOnOutsideClick(false);

            dialog.add(new Span("Do you really want to delete recipe: '" + recipe.getName() + "'?"));

            HorizontalLayout buttonsLayout = new HorizontalLayout();
            buttonsLayout.setId("buttons-layout");
            Button confirmButton = new Button("Delete", event -> {
                if (recipe != null) {
                    this.recipeService.delete(recipe.getId());
                    clearForm();
                    refreshGrid();
                    Notification.show("Recipe deleted.");
                }
                dialog.close();
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            Button cancelButton = new Button("Cancel", event -> {
                dialog.close();
            });
            buttonsLayout.add(confirmButton, cancelButton);

            dialog.add(buttonsLayout);
            dialog.open();
        };
    }

    private ComponentEventListener<ClickEvent<Button>> buttonSaveClickListener() {
        return e -> {
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
        };
    }

    private ComponentEventListener<ClickEvent<Button>> buttonCancelClickListener() {
        return e -> {
            clearForm();
            refreshGrid();
        };
    }

    private HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Grid<Recipe>, Recipe>> gridValueChangeListener(RecipeService recipeService) {
        return event -> {
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
        };
    }

    private void populateIngredients(Recipe recipe) {
        if (recipe == null) {
            recipeIngredients.setItems(Collections.emptyList());
        } else {
            recipeIngredients.setItems(recipe.getIngredients());
        }
    }

    private Div createEditorLayout() {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        editorLayoutDiv.add(createFormLayout());
        editorLayoutDiv.add(createIngredientsLayout());
        editorLayoutDiv.add(createButtonLayout());

        return editorLayoutDiv;
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(description, "Description");
        return formLayout;
    }

    private HorizontalLayout createIngredientsLayout() {
        HorizontalLayout ingredientsLayout = new HorizontalLayout();
        ingredientsLayout.setId("ingredients-layout");
        ingredientsLayout.setWidthFull();
        ingredientsLayout.setSpacing(true);

        recipeIngredients.setColumns("amount", "ingredient.unit", "ingredient.name");
        ingredientsLayout.add(recipeIngredients);
        return ingredientsLayout;
    }

    private HorizontalLayout createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setId("button-layout");
        buttonLayout.setWidthFull();
        buttonLayout.setSpacing(true);

        save.addClickListener(buttonSaveClickListener());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel.addClickListener(buttonCancelClickListener());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        delete.addClickListener(buttonDeleteClickListener());
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, delete);
        return buttonLayout;
    }

    private Div createGridLayout() {
        Div gridLayout = new Div();
        gridLayout.setId("grid-layout");
        grid.setColumns("name");
        grid.setDataProvider(new CrudServiceDataProvider<Recipe, Void>(this.recipeService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();
        grid.asSingleSelect().addValueChangeListener(gridValueChangeListener(recipeService));

        gridLayout.add(grid);
        return gridLayout;
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
