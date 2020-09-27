package org.mbruncic.cookingplanner.views.ingredient;

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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.mbruncic.cookingplanner.data.service.IngredientService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.Optional;

@Route(value = "ingredient", layout = MainView.class)
@PageTitle("Ingredient")
@CssImport("./styles/views/ingredient/ingredient-view.css")
public class IngredientView extends Div {

    private Grid<Ingredient> grid;

    private TextField name = new TextField();
    private Select<Unit> unit = new Select<>();

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private Binder<Ingredient> binder;

    private Ingredient ingredient = new Ingredient();

    private IngredientService ingredientService;

    public IngredientView(@Autowired IngredientService ingredientService) {
        setId("ingredient-view");
        this.ingredientService = ingredientService;
        // Configure Grid
        grid = new Grid<>(Ingredient.class);
        grid.setColumns("name", "unit");
        grid.setDataProvider(new CrudServiceDataProvider<Ingredient, Void>(ingredientService));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                Optional<Ingredient> ingredientFromBackend= ingredientService.get(event.getValue().getId());
                // when a row is selected but the data is no longer available, refresh grid
                if(ingredientFromBackend.isPresent()){
                    populateForm(ingredientFromBackend.get());
                } else {
                    refreshGrid();
                }
            } else {
                clearForm();
            }
        });

        // Configure Form
        binder = new Binder<>(Ingredient.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.ingredient == null) {
                    this.ingredient = new Ingredient();
                }
                binder.writeBean(this.ingredient);
                ingredientService.update(this.ingredient);
                clearForm();
                refreshGrid();
                Notification.show("Ingredient details stored.");
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the ingredient details.");
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

        unit.setItems(Unit.values());

        FormLayout formLayout = new FormLayout();
        addFormItem(editorDiv, formLayout, name, "Name");
        addFormItem(editorDiv, formLayout, unit, "Unit");
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
        buttonLayout.add(save, cancel);
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

    private void populateForm(Ingredient value) {
        this.ingredient = value;
        binder.readBean(this.ingredient);
    }
}
