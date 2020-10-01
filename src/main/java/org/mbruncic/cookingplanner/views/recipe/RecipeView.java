package org.mbruncic.cookingplanner.views.recipe;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.mbruncic.cookingplanner.data.entity.Recipe;
import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.mbruncic.cookingplanner.data.service.IngredientService;
import org.mbruncic.cookingplanner.data.service.RecipeIngredientService;
import org.mbruncic.cookingplanner.data.service.RecipeService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
    private final IngredientService ingredientService;
    private final RecipeIngredientService recipeIngredientService;
    private Recipe recipe = new Recipe();

    private final RecipeService recipeService;

    public RecipeView(@Autowired RecipeService recipeService
            , @Autowired IngredientService ingredientService
            , @Autowired RecipeIngredientService recipeIngredientService
    ) {
        setId("recipe-view");
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.recipeIngredientService = recipeIngredientService;
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

    private HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Grid<Recipe>, Recipe>> gridValueChangeListener() {
        return event -> {
            if (event.getValue() != null) {
                Integer recipeId = event.getValue().getId();
                refreshViewWithBackendData(recipeId);
            } else {
                clearForm();
            }
        };
    }

    private void refreshViewWithBackendData(Integer recipeId) {
        Optional<Recipe> recipeFromBackend = this.recipeService.get(recipeId);
        if (recipeFromBackend.isPresent()) {
            populateForm(recipeFromBackend.get());
        } else {
            refreshGrid();
        }
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

    private VerticalLayout createIngredientsLayout() {
        VerticalLayout ingredientsLayout = new VerticalLayout();
        ingredientsLayout.setId("ingredients-layout");
        ingredientsLayout.setWidthFull();
        ingredientsLayout.setSpacing(true);

        recipeIngredients.setColumns("amount", "ingredient.unit", "ingredient.name");
        Button addNewButton = new Button();
        addNewButton.setIcon(new Icon(VaadinIcon.PLUS));
        addNewButton.addClickListener(event -> {
            if (recipe == null || recipe.getId() == null) {
                Notification.show("Select recipe first");
                return;
            }
            Dialog addNewIngredientDialog = new Dialog();
            FormLayout formLayout = new FormLayout();
            formLayout.setWidthFull();

            HorizontalLayout amountLayout = new HorizontalLayout();
            NumberField amountField = new NumberField();
            Span amountUnit = new Span();
            amountLayout.add(amountField, amountUnit);
            formLayout.addFormItem(amountLayout, "Amount");

            Select<Ingredient> ingredientSelect = new Select<>();
            ingredientSelect.setItems(this.ingredientService.list(Pageable.unpaged()).getContent());
            ingredientSelect.addValueChangeListener(e -> amountUnit.setText(e.getValue().getUnit().toString()));
            ingredientSelect.setTextRenderer((ItemLabelGenerator<Ingredient>) Ingredient::getName);
            formLayout.addFormItem(ingredientSelect, "Ingredient");

            HorizontalLayout buttonLayout = new HorizontalLayout();
            Button save = new Button("Save");
            save.addClickListener(e -> {
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(this.recipe);
                recipeIngredient.setAmount(amountField.getValue());
                recipeIngredient.setIngredient(ingredientSelect.getValue());
                this.recipeIngredientService.update(recipeIngredient);
                refreshViewWithBackendData(this.recipe.getId());
                addNewIngredientDialog.close();
            });
            Button cancel = new Button("Cancel");
            cancel.addClickListener(e -> addNewIngredientDialog.close());
            buttonLayout.add(save, cancel);
            addNewIngredientDialog.add(formLayout, buttonLayout);
            addNewIngredientDialog.open();
        });
        ingredientsLayout.add(recipeIngredients, addNewButton);
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
        grid.asSingleSelect().addValueChangeListener(gridValueChangeListener());

        gridLayout.add(grid);
        return gridLayout;
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
