package com.recipe.manager.e2e;

import com.recipe.manager.boot.RecipeManagerLauncher;
import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.IngredientInput;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.dto.RecipeSearchRequest;
import com.recipe.manager.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RecipeManagerLauncher.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@ActiveProfiles("test")
class RecipeControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();
    }

    @Test
    void addRecipe_happyPath() {
        // Given
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName("Integration Test Recipe");
        request.setDescription("A recipe created during an integration test.");
        request.setIsVegetarian(false);
        request.setServings(4);
        request.setInstructions("1. Run the test. 2. Verify it passes.");

        IngredientInput ingredient = new IngredientInput();
        ingredient.setName("Test Ingredient");
        ingredient.setQuantity(1);
        ingredient.setUnit(IngredientInput.UnitEnum.PCS);
        request.setIngredients(Collections.singletonList(ingredient));

        // When
        ResponseEntity<Recipe> response = restTemplate.postForEntity("/recipes", request, Recipe.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Integration Test Recipe");
        assertThat(response.getBody().getIngredients()).hasSize(1);
    }

    @Test
    void searchRecipes_happyPath() {
        // Given: a recipe in the database
        CreateRecipeRequest createRequest = new CreateRecipeRequest();
        createRequest.setName("Searchable Recipe");
        createRequest.setDescription("A recipe to be found by search.");
        createRequest.setIsVegetarian(true);
        createRequest.setServings(2);
        createRequest.setInstructions("Some instructions to search for.");
        IngredientInput ingredient = new IngredientInput();
        ingredient.setName("Searchable Ingredient");
        ingredient.setQuantity(100);
        ingredient.setUnit(IngredientInput.UnitEnum.GR);
        createRequest.setIngredients(Collections.singletonList(ingredient));
        restTemplate.postForEntity("/recipes", createRequest, Recipe.class);

        // When: searching for vegetarian recipes
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        searchRequest.setIsVegetarian(true);
        ResponseEntity<RecipeListResponse> response = restTemplate.postForEntity("/recipes/search", searchRequest, RecipeListResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().getFirst().getName()).isEqualTo("Searchable Recipe");
    }

    @Test
    void removeRecipe_happyPath() {
        // Given: a recipe in the database
        CreateRecipeRequest createRequest = new CreateRecipeRequest();
        createRequest.setName("Recipe to be Deleted");
        createRequest.setDescription("This recipe will be removed.");
        createRequest.setIsVegetarian(false);
        createRequest.setServings(1);
        createRequest.setInstructions("Instructions for a short-lived recipe.");
        IngredientInput ingredient = new IngredientInput();
        ingredient.setName("Ephemeral Ingredient");
        ingredient.setQuantity(1);
        ingredient.setUnit(IngredientInput.UnitEnum.PCS);
        createRequest.setIngredients(Collections.singletonList(ingredient));
        ResponseEntity<Recipe> createdResponse = restTemplate.postForEntity("/recipes", createRequest, Recipe.class);
        Long recipeId = createdResponse.getBody().getId();

        // When: deleting the recipe
        ResponseEntity<Void> deleteResponse = restTemplate.exchange("/recipes/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class, recipeId);

        // Then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // And When: searching for it again
        RecipeSearchRequest searchRequest = new RecipeSearchRequest();
        ResponseEntity<RecipeListResponse> searchResponse = restTemplate.postForEntity("/recipes/search", searchRequest, RecipeListResponse.class);

        // Then: it should not be found
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody().getData()).noneMatch(recipe -> recipe.getId().equals(recipeId));
    }
}
