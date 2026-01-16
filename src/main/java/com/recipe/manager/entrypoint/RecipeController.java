package com.recipe.manager.entrypoint;

import com.recipe.manager.controller.api.RecipesApi;
import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeSearchFilter;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecipeController implements RecipesApi {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Override
    public ResponseEntity<Recipe> addRecipe(CreateRecipeRequest createRecipeRequest) {
        return ResponseEntity.ok().body(recipeService.addRecipe(createRecipeRequest));
    }

    @Override
    public ResponseEntity<RecipeListResponse> getRecipes(Boolean isVegetarian,
                                                         Integer servings,
                                                         List<String> includeIngredients,
                                                         List<String> excludeIngredients,
                                                         String instruction,
                                                         Integer page,
                                                         Integer limit) {

        RecipeSearchFilter filter = new RecipeSearchFilter.Builder()
                .vegetarian(isVegetarian)
                .servings(servings)
                .includeIngredients(includeIngredients)
                .excludeIngredients(excludeIngredients)
                .instructions(instruction)
                .vegetarian(isVegetarian)
                .page(page)
                .limit(limit)
                .build();

        return ResponseEntity.ok().body(recipeService.getRecipes(filter));
    }

    @Override
    public ResponseEntity<Void> removeRecipe(Long id) {
        recipeService.removeRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
