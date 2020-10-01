package org.mbruncic.cookingplanner.views.recipeIngredient;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.mbruncic.cookingplanner.data.entity.Recipe;
import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.mbruncic.cookingplanner.data.service.IngredientService;
import org.mbruncic.cookingplanner.data.service.RecipeIngredientService;
import org.mbruncic.cookingplanner.data.service.RecipeService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.Optional;

@Route(value = "recipeIngredient", layout = MainView.class)
@PageTitle("RecipeIngredient")
@CssImport("./styles/views/recipeIngredient/recipeIngredient-view.css")
public class RecipeIngredientView extends Div {

    private final Grid<RecipeIngredient> grid;

    private final NumberField amount = new NumberField();
    private final Select<Recipe> recipeSelect = new Select<>();
    private final Select<Ingredient> ingredientSelect = new Select<>();

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");

    private final Binder<RecipeIngredient> binder;
    private RecipeIngredient recipeIngredient = new RecipeIngredient();

    private final RecipeIngredientService recipeIngredientService;
    private final RecipeService recipeService;
    private final IngredientService ingredientService;

    public RecipeIngredientView(@Autowired RecipeIngredientService recipeIngredientService
            , @Autowired RecipeService recipeService
            , @Autowired IngredientService ingredientService) {
        setId("recipeIngredient-view");
        this.recipeIngredientService = recipeIngredientService;
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        // Configure Grid
        grid = new Grid<>(RecipeIngredient.class);
        grid.setColumns("recipe.name", "amount", "ingredient.unit", "ingredient.name");
        grid.setDataProvider(new CrudServiceDataProvider<RecipeIngredient, Void>(recipeIngredientService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        Page<Recipe> recipes = recipeService.list(Pageable.unpaged());
        recipeSelect.setItems(recipes.getContent());
        recipeSelect.setTextRenderer((ItemLabelGenerator<Recipe>) Recipe::getName);

        Page<Ingredient> ingredients = ingredientService.list(Pageable.unpaged());
        ingredientSelect.setItems(ingredients.getContent());
        ingredientSelect.setTextRenderer((ItemLabelGenerator<Ingredient>) Ingredient::getName);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<RecipeIngredient> recipeIngredientFromBackend = recipeIngredientService.get(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if (recipeIngredientFromBackend.isPresent()) {
                    populateForm(recipeIngredientFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new Binder<>(RecipeIngredient.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);
        binder.forField(recipeSelect).bind(RecipeIngredient::getRecipe, RecipeIngredient::setRecipe);
        binder.forField(ingredientSelect).bind(RecipeIngredient::getIngredient, RecipeIngredient::setIngredient);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.recipeIngredient == null) {
                    this.recipeIngredient = new RecipeIngredient();
                }
                binder.writeBean(this.recipeIngredient);
                recipeIngredientService.update(this.recipeIngredient);
                clearForm();
                refreshGrid();
                Notification.show("RecipeIngredient details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the recipeIngredient details.");
            }
        });

        delete.addClickListener(e -> {
            try {
                if (this.recipeIngredient == null) {
                    this.recipeIngredient = new RecipeIngredient();
                }
                binder.writeBean(this.recipeIngredient);
                recipeIngredientService.delete(this.recipeIngredient.getId());
                clearForm();
                refreshGrid();
                Notification.show("RecipeIngredient details deleted.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to delete the recipeIngredient details.");
            }
        });

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setId("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setId("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        addFormItem(editorDiv, formLayout, recipeSelect, "Recipe select");
        addFormItem(editorDiv, formLayout, amount, "Amount");
        addFormItem(editorDiv, formLayout, ingredientSelect, "Ingredient select");
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
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

    private void populateForm(RecipeIngredient value) {
        this.recipeIngredient = value;
        binder.readBean(this.recipeIngredient);
    }
}
