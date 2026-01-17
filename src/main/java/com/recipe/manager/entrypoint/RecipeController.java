package com.recipe.manager.entrypoint;

import com.recipe.manager.controller.api.RecipesApi;
import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.dto.RecipeSearchRequest;
import com.recipe.manager.entrypoint.exception.ApiErrorCode;
import com.recipe.manager.entrypoint.exception.ApiException;
import com.recipe.manager.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeController implements RecipesApi {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @Override
    public ResponseEntity<Recipe> addRecipe(CreateRecipeRequest createRecipeRequest) {
        //open api doesn't generate @Size annotation. Check it manually
        if (CollectionUtils.isEmpty(createRecipeRequest.getIngredients())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ingredients cannot be empty", ApiErrorCode.RequiredParameter);
        }
        return ResponseEntity.ok().body(recipeService.addRecipe(createRecipeRequest));
    }

    @Override
    public ResponseEntity<RecipeListResponse> searchRecipes(RecipeSearchRequest recipeSearchRequest) {
        return ResponseEntity.ok().body(recipeService.getRecipes(recipeSearchRequest));
    }

    @Override
    public ResponseEntity<Void> removeRecipe(Long id) {
        recipeService.removeRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
