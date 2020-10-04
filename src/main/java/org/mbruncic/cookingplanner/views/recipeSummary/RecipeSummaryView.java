package org.mbruncic.cookingplanner.views.recipeSummary;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.mbruncic.cookingplanner.data.entity.Recipe;
import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.mbruncic.cookingplanner.data.service.RecipeService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.mbruncic.cookingplanner.views.recipeSummary.model.IngredientSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "recipeSummary", layout = MainView.class)
@PageTitle("RecipeSummary")
@CssImport("./styles/views/recipeSummary/recipeSummary-view.css")
public class RecipeSummaryView extends Div {

    private Set<IngredientSummary> ingredientSummarySet;
    private final MultiSelectListBox<Recipe> allRecipes;
    private final Grid<IngredientSummary> ingredientSummaryGrid=new Grid<>(IngredientSummary.class);

    private final RecipeService recipeService;

    public RecipeSummaryView(@Autowired RecipeService recipeService) {
        setId("recipeSummary-view");
        this.recipeService = recipeService;
        // Configure list
        allRecipes = new MultiSelectListBox<>();
        allRecipes.setDataProvider(new CrudServiceDataProvider<Recipe, Void>(this.recipeService));
        allRecipes.setHeightFull();
        allRecipes.setRenderer(new ComponentRenderer<>((SerializableFunction<Recipe, Span>) recipe -> new Span(recipe.getName())));

        ingredientSummaryGrid.setDataProvider(new AbstractBackEndDataProvider<IngredientSummary, Object>() {
            @Override
            protected Stream<IngredientSummary> fetchFromBackEnd(Query<IngredientSummary, Object> query) {
                return ingredientSummarySet.stream();
            }

            @Override
            protected int sizeInBackEnd(Query<IngredientSummary, Object> query) {
                if (ingredientSummarySet==null) return 0;
                return ingredientSummarySet.size();
            }
        });
        ingredientSummaryGrid.setColumns("ingredient.name", "amount", "ingredient.unit");

        // when a row is selected or deselected, populate form
        allRecipes.addSelectionListener(event -> {
            Set<Recipe> value = event.getValue();
            Map<Ingredient, Double> amountOfIngredients = new HashMap<>();
            for (Recipe recipe : value) {
                for (RecipeIngredient it : recipe.getIngredients()) {
                    if (!amountOfIngredients.containsKey(it.getIngredient())){
                        amountOfIngredients.put(it.getIngredient(), it.getAmount());
                    } else {
                        Double subtotal = amountOfIngredients.get(it.getIngredient());
                        double roundedTo2Decimals = Math.round((subtotal + it.getAmount()) * 100) / (double) 100;
                        amountOfIngredients.put(it.getIngredient(), roundedTo2Decimals);
                    }
                }
            }
            this.ingredientSummarySet = amountOfIngredients.entrySet().stream()
                    .map(e -> new IngredientSummary(e.getKey(), e.getValue()))
                    .collect(Collectors.toSet());
            this.ingredientSummaryGrid.setItems(this.ingredientSummarySet);
        });

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createIngredientSummaryGridLayout(splitLayout);

        add(splitLayout);
    }

    private void createIngredientSummaryGridLayout(SplitLayout splitLayout) {
        Div ingredientSummaryDiv = new Div();
        ingredientSummaryDiv.setId("ingredient-summary-layout");

        ingredientSummaryDiv.add(ingredientSummaryGrid);
        ingredientSummaryDiv.setWidthFull();

        splitLayout.addToSecondary(ingredientSummaryDiv);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(allRecipes);
    }

}
