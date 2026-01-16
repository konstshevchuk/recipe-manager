package com.recipe.manager.service;

import com.recipe.manager.boot.RecipeManagerLauncher;
import com.recipe.manager.data.UnitType;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.entity.IngredientEntity;
import com.recipe.manager.entity.RecipeEntity;
import com.recipe.manager.entrypoint.exception.ApiException;
import com.recipe.manager.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = RecipeManagerLauncher.class)
@Transactional
class RecipeServiceTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    void setUp() {
        recipeRepository.deleteAll();

        RecipeEntity recipe1 = createRecipe("Spaghetti Carbonara", "A classic Roman pasta dish.", false, 2, "Cook pasta, mix with eggs, cheese, and pancetta.", "pasta", "eggs", "cheese");
        RecipeEntity recipe2 = createRecipe("Vegetable Stir-Fry", "A quick and healthy vegetable dish.", true, 4, "Stir-fry vegetables in a wok.", "broccoli", "carrot", "onion");
        RecipeEntity recipe3 = createRecipe("Chicken Salad", "A light and refreshing salad.", false, 2, "Mix chicken, lettuce, and dressing.", "chicken", "lettuce", "tomato");
        RecipeEntity recipe4 = createRecipe("Salmon with Asparagus", "A simple and elegant meal.", false, 2, "Bake salmon and asparagus.", "salmon", "asparagus", "lemon");
        RecipeEntity recipe5 = createRecipe("Lentil Soup", "A hearty and nutritious soup.", true, 6, "Simmer lentils and carrots.", "lentils", "carrot", "celery");
        RecipeEntity recipe6 = createRecipe("Pesto Pasta", "A vibrant and flavorful pasta dish.", true, 3, "Toss pasta with pesto sauce.", "pasta", "pesto", "pine nuts");
        RecipeEntity recipe7 = createRecipe("Beef Tacos", "Classic Mexican tacos.", false, 4, "Cook ground beef and serve in taco shells.", "ground beef", "taco shells", "salsa");
        RecipeEntity recipe8 = createRecipe("Tuna Sandwich", "A quick and easy sandwich.", false, 1, "Mix tuna with mayonnaise and serve on bread.", "tuna", "mayonnaise", "bread");
        RecipeEntity recipe9 = createRecipe("Mushroom Risotto", "A creamy and savory Italian rice dish.", true, 4, "Cook risotto with mushrooms and parmesan.", "risotto rice", "mushrooms", "parmesan");
        RecipeEntity recipe10 = createRecipe("Chicken Curry", "A flavorful and aromatic curry.", false, 4, "Simmer chicken in a curry sauce.", "chicken", "curry powder", "coconut milk");

        recipeRepository.saveAll(Arrays.asList(recipe1, recipe2, recipe3, recipe4, recipe5, recipe6, recipe7, recipe8, recipe9, recipe10));
    }

    private RecipeEntity createRecipe(String name, String description, boolean isVegetarian, int servings, String instructions, String... ingredientNames) {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setVegeterian(isVegetarian);
        recipe.setServing(servings);
        recipe.setInstructions(instructions);

        List<IngredientEntity> ingredients = Arrays.stream(ingredientNames)
                .map(ingredientName -> {
                    IngredientEntity ingredient = new IngredientEntity();
                    ingredient.setName(ingredientName);
                    ingredient.setRecipe(recipe);
                    ingredient.setQuantity(100);
                    ingredient.setUnit(UnitType.gr);
                    return ingredient;
                })
                .collect(Collectors.toList());

        recipe.setIngredients(ingredients);
        return recipe;
    }

    @Test
    void testGetRecipes_NoFilters() {
        RecipeListResponse response = recipeService.getRecipes(null, null, null, null, null, 0, 10);
        assertNotNull(response);
        assertEquals(10, response.getData().size());
    }

    @Test
    void testGetRecipes_FilterByVegetarian() {
        RecipeListResponse response = recipeService.getRecipes(true, null, null, null, null, 0, 10);
        assertEquals(4, response.getData().size());
        assertTrue(response.getData().stream().allMatch(Recipe::getIsVegetarian));
        List<String> names = response.getData().stream().map(Recipe::getName).toList();
        assertTrue(names.containsAll(Arrays.asList("Vegetable Stir-Fry", "Lentil Soup", "Pesto Pasta", "Mushroom Risotto")));

        Recipe stirFry = response.getData().stream().filter(r -> r.getName().equals("Vegetable Stir-Fry")).findFirst().orElse(null);
        assertNotNull(stirFry);
        assertEquals("A quick and healthy vegetable dish.", stirFry.getDescription());
        assertEquals(4, stirFry.getServings());
    }

    @Test
    void testGetRecipes_FilterByServings() {
        RecipeListResponse response = recipeService.getRecipes(null, 4, null, null, null, 0, 10);
        assertEquals(4, response.getData().size());
        assertTrue(response.getData().stream().allMatch(r -> r.getServings() == 4));
        List<String> names = response.getData().stream().map(Recipe::getName).toList();
        assertTrue(names.containsAll(Arrays.asList("Vegetable Stir-Fry", "Beef Tacos", "Mushroom Risotto", "Chicken Curry")));
    }

    @Test
    void testGetRecipes_IncludeSingleIngredient() {
        RecipeListResponse response = recipeService.getRecipes(null, null, Collections.singletonList("carrot"), null, null, 0, 10);
        assertEquals(2, response.getData().size());
        List<String> names = response.getData().stream().map(Recipe::getName).toList();
        assertTrue(names.containsAll(Arrays.asList("Vegetable Stir-Fry", "Lentil Soup")));

        Recipe lentilSoup = response.getData().stream().filter(r -> r.getName().equals("Lentil Soup")).findFirst().orElse(null);
        assertNotNull(lentilSoup);
        assertEquals("A hearty and nutritious soup.", lentilSoup.getDescription());
        assertEquals(6, lentilSoup.getServings());
        assertTrue(lentilSoup.getIsVegetarian());
    }

    @Test
    void testGetRecipes_IncludeMultipleIngredients() {
        RecipeListResponse response = recipeService.getRecipes(null, null, Arrays.asList("pasta", "chicken"), null, null, 0, 10);
        assertEquals(4, response.getData().size());
        List<String> names = response.getData().stream().map(Recipe::getName).toList();
        assertTrue(names.containsAll(Arrays.asList("Spaghetti Carbonara", "Pesto Pasta", "Chicken Salad", "Chicken Curry")));
    }

    @Test
    void testGetRecipes_ExcludeSingleIngredient() {
        RecipeListResponse response = recipeService.getRecipes(null, null, null, Collections.singletonList("chicken"), null, 0, 10);
        assertEquals(8, response.getData().size());
        assertTrue(response.getData().stream().noneMatch(r -> r.getName().contains("Chicken")));
    }

    @Test
    void testGetRecipes_ExcludeMultipleIngredients() {
        RecipeListResponse response = recipeService.getRecipes(null, null, null, Arrays.asList("chicken", "pasta"), null, 0, 10);
        assertEquals(6, response.getData().size());
        assertTrue(response.getData().stream().noneMatch(r -> r.getName().contains("Chicken") || r.getName().contains("Pasta")));
    }

    @Test
    void testGetRecipes_IncludeAndExcludeIngredients() {
        RecipeListResponse response = recipeService.getRecipes(null, null, Collections.singletonList("pasta"), Collections.singletonList("eggs"), null, 0, 10);
        assertEquals(1, response.getData().size());
        Recipe recipe = response.getData().getFirst();
        assertEquals("Pesto Pasta", recipe.getName());
        assertEquals("A vibrant and flavorful pasta dish.", recipe.getDescription());
        assertEquals(3, recipe.getServings());
        assertTrue(recipe.getIsVegetarian());
    }

    @Test
    void testGetRecipes_FilterByInstruction() {
        RecipeListResponse response = recipeService.getRecipes(null, null, null, null, "wok", 0, 10);
        assertEquals(1, response.getData().size());
        Recipe recipe = response.getData().getFirst();
        assertEquals("Vegetable Stir-Fry", recipe.getName());
        assertEquals("A quick and healthy vegetable dish.", recipe.getDescription());
        assertEquals(4, recipe.getServings());
        assertTrue(recipe.getIsVegetarian());
    }

    @Test
    void testGetRecipes_Pagination() {
        RecipeListResponse response = recipeService.getRecipes(null, null, null, null, null, 1, 3);
        assertEquals(3, response.getData().size());
        assertEquals(1, response.getPagination().getPage());
        assertEquals(3, response.getPagination().getLimit());
    }

    @Test
    void testGetRecipes_NoResults() {
        RecipeListResponse response = recipeService.getRecipes(true, null, Collections.singletonList("chicken"), null, null, 0, 10);
        assertEquals(0, response.getData().size());
    }

    @Test
    void testGetRecipes_ComplexFilter_NegativeMatch() {
        RecipeListResponse response = recipeService.getRecipes(true, 4, Collections.singletonList("broccoli"), Collections.singletonList("onion"), "wok", 0, 10);
        assertEquals(0, response.getData().size());
    }

    @Test
    void testGetRecipes_ComplexFilter_PositiveMatch() {
        RecipeListResponse response = recipeService.getRecipes(true, 4, Collections.singletonList("mushrooms"), Collections.singletonList("onion"), null, 0, 10);
        assertEquals(1, response.getData().size());
        Recipe recipe = response.getData().getFirst();
        assertEquals("Mushroom Risotto", recipe.getName());
        assertEquals("A creamy and savory Italian rice dish.", recipe.getDescription());
        assertTrue(recipe.getIsVegetarian());
        assertEquals(4, recipe.getServings());
    }

    @Test
    void testRemoveRecipe_HappyPath() {
        RecipeEntity recipe = recipeRepository.findAll().getFirst();
        Long recipeId = recipe.getId();
        assertNotNull(recipeId);

        recipeService.removeRecipe(recipeId);

        Optional<RecipeEntity> deletedRecipe = recipeRepository.findById(recipeId);
        assertFalse(deletedRecipe.isPresent());
    }

    @Test
    void testRemoveRecipe_NotFound() {
        Long nonExistentId = 999L;

        ApiException exception = assertThrows(ApiException.class, () -> {
            recipeService.removeRecipe(nonExistentId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Recipe not found", exception.getMessage());
    }
}
