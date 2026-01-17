package com.recipe.manager.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.manager.boot.RecipeManagerLauncher;
import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.IngredientInput;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.dto.RecipeSearchRequest;
import com.recipe.manager.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@ContextConfiguration(classes = {RecipeManagerLauncher.class})
@ActiveProfiles("test")
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    private CreateRecipeRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = new CreateRecipeRequest();
        validCreateRequest.setName("Test Recipe");
        validCreateRequest.setDescription("A valid description");
        validCreateRequest.setIsVegetarian(true);
        validCreateRequest.setServings(2);
        validCreateRequest.setInstructions("These are some valid instructions for the test recipe.");

        IngredientInput ingredient = new IngredientInput();
        ingredient.setName("Test Ingredient");
        ingredient.setQuantity(100);
        ingredient.setUnit(IngredientInput.UnitEnum.GR);
        validCreateRequest.setIngredients(Collections.singletonList(ingredient));
    }

    // --- Add Recipe Tests ---

    @Test
    void addRecipe_happyPath() throws Exception {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Test Recipe");
        recipe.setIsVegetarian(true);
        recipe.setServings(2);

        when(recipeService.addRecipe(any(CreateRecipeRequest.class))).thenReturn(recipe);

        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Recipe")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
        validCreateRequest.setName(null);
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("name")))
                .andExpect(jsonPath("$.message", containsString("must not be null")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenNameIsTooShort() throws Exception {
        validCreateRequest.setName("a");
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("name")))
                .andExpect(jsonPath("$.message", containsString("size must be between 3 and 255")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenInstructionsAreTooShort() throws Exception {
        validCreateRequest.setInstructions("short");
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("instructions")))
                .andExpect(jsonPath("$.message", containsString("size must be between 10 and 3000")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenServingsIsZero() throws Exception {
        validCreateRequest.setServings(0);
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("servings")))
                .andExpect(jsonPath("$.message", containsString("must be greater than or equal to 1")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenServingsIsTooHigh() throws Exception {
        validCreateRequest.setServings(101);
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("servings")))
                .andExpect(jsonPath("$.message", containsString("must be less than or equal to 100")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenIngredientsAreMissing() throws Exception {
        validCreateRequest.setIngredients(null);
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("ingredients")))
                .andExpect(jsonPath("$.message", containsString("must not be null")));
    }

    @Test
    void addRecipe_shouldReturnBadRequest_whenIsVegiterianIsMissing() throws Exception {
        validCreateRequest.setIsVegetarian(null);
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("isVegetarian")))
                .andExpect(jsonPath("$.message", containsString("must not be null")));
    }

    // --- Search Recipes Tests ---

    @Test
    void searchRecipes_happyPath() throws Exception {
        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setIsVegetarian(true);

        RecipeListResponse response = new RecipeListResponse();
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Test Recipe");
        recipe.setIsVegetarian(true);
        response.setData(Collections.singletonList(recipe));

        when(recipeService.getRecipes(any(RecipeSearchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Test Recipe")));
    }

    @Test
    void searchRecipes_shouldPassFiltersToService() throws Exception {
        RecipeSearchRequest request = new RecipeSearchRequest();
        request.setIsVegetarian(true);
        request.setServings(4);
        request.setIncludeIngredients(List.of("tomato", "onion"));

        when(recipeService.getRecipes(any(RecipeSearchRequest.class))).thenReturn(new RecipeListResponse());

        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(recipeService).getRecipes(argThat(arg ->
                arg.getIsVegetarian() &&
                        arg.getServings() == 4 &&
                        arg.getIncludeIngredients().contains("tomato") &&
                        arg.getIncludeIngredients().contains("onion")
        ));
    }

    @Test
    void searchRecipes_shouldReturnBadRequest_whenPageIsZero() throws Exception {
        String invalidJson = "{\"page\": 0}";
        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("page")))
                .andExpect(jsonPath("$.message", containsString("must be greater than or equal to 1")));
    }

    @Test
    void searchRecipes_shouldReturnBadRequest_whenPageSizeIsZero() throws Exception {
        String invalidJson = "{\"pageSize\": 0}";
        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("pageSize")))
                .andExpect(jsonPath("$.message", containsString("must be greater than or equal to 1")));
    }

    @Test
    void searchRecipes_shouldReturnBadRequest_whenPageSizeIsTooHigh() throws Exception {
        String invalidJson = "{\"pageSize\": 101}";
        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("pageSize")))
                .andExpect(jsonPath("$.message", containsString("must be less than or equal to 100")));
    }

    @Test
    void searchRecipes_shouldReturnBadRequest_whenInstructionIsTooShort() throws Exception {
        String invalidJson = "{\"instruction\": \"abc\"}";
        mockMvc.perform(post("/recipes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field", is("instruction")))
                .andExpect(jsonPath("$.message", containsString("size must be between 5 and 255")));
    }
}
