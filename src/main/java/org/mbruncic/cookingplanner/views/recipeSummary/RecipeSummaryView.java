package org.mbruncic.cookingplanner.views.recipeSummary;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.mbruncic.cookingplanner.data.entity.Ingredient;
import org.mbruncic.cookingplanner.data.entity.Recipe;
import org.mbruncic.cookingplanner.data.entity.RecipeIngredient;
import org.mbruncic.cookingplanner.data.service.RecipeService;
import org.mbruncic.cookingplanner.views.main.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Route(value = "recipeSummary", layout = MainView.class)
@PageTitle("RecipeSummary")
@CssImport("./styles/views/recipeSummary/recipeSummary-view.css")
public class RecipeSummaryView extends Div {

    private MultiSelectListBox<Recipe> allRecipes;
    private RecipeService recipeService;

    public RecipeSummaryView(@Autowired RecipeService recipeService) {
        setId("recipeSummary-view");
        this.recipeService = recipeService;
        // Configure list
        allRecipes = new MultiSelectListBox<>();
        allRecipes.setDataProvider(new CrudServiceDataProvider<Recipe, Void>(recipeService));
        allRecipes.setHeightFull();
        allRecipes.setRenderer(new ComponentRenderer<>((SerializableFunction<Recipe, Span>) recipe -> new Span(recipe.getName())));

        // when a row is selected or deselected, populate form
        allRecipes.addSelectionListener(event -> {
            Set<Recipe> value = event.getValue();
            Map<Ingredient, Integer> amountOfIngredients = new HashMap<>();
            for (Recipe recipe : value) {
                for (RecipeIngredient it : recipe.getIngredients()) {
                    if (!amountOfIngredients.containsKey(it.getIngredient())){
                        amountOfIngredients.put(it.getIngredient(), it.getAmount());
                    } else {
                        Integer subtotal = amountOfIngredients.get(it.getIngredient());
                        amountOfIngredients.put(it.getIngredient(), subtotal+it.getAmount());
                    }
                }
            }
            if (amountOfIngredients.isEmpty())  return;
            StringBuilder notificationBuilder = new StringBuilder("Summary:");
            for (Map.Entry<Ingredient, Integer> entry : amountOfIngredients.entrySet()) {
                notificationBuilder.append("\n").append(entry.getKey().getName()).append(":\t").append(entry.getValue()).append(entry.getKey().getUnit());
            }
            Notification.show(notificationBuilder.toString());
        });

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);

        add(splitLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(allRecipes);
    }

    private void refreshGrid() {
        allRecipes.getDataProvider().refreshAll();
    }

}
