package com.recipe.manager.service;

import com.recipe.manager.boot.RecipeManagerLauncher;
import com.recipe.manager.data.UnitType;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.dto.RecipeSearchRequest;
import com.recipe.manager.entity.IngredientEntity;
import com.recipe.manager.entity.RecipeEntity;
import com.recipe.manager.entrypoint.exception.RecipeNotFoundException;
import com.recipe.manager.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

        createRecipe("Spaghetti Carbonara", "A classic Roman pasta dish.", "Italian", false, 2, "Cook pasta, mix with eggs, cheese, and pancetta.", "pasta", "eggs", "cheese");
        createRecipe("Vegetable Stir-Fry", "A quick and healthy vegetable dish.", "Asian", true, 4, "Stir-fry vegetables in a wok.", "broccoli", "carrot", "onion");
        createRecipe("Chicken Salad", "A light and refreshing salad.", "American", false, 2, "Mix chicken, lettuce, and dressing.", "chicken", "lettuce", "tomato");
        createRecipe("Salmon with Asparagus", "A simple and elegant meal.", "French", false, 2, "Bake salmon and asparagus.", "salmon", "asparagus", "lemon");
        createRecipe("Lentil Soup", "A hearty and nutritious soup.", "Middle Eastern", true, 6, "Simmer lentils and carrots.", "lentils", "carrot", "celery");
        createRecipe("Pesto Pasta", "A vibrant and flavorful pasta dish.", "Italian", true, 3, "Toss pasta with pesto sauce.", "pasta", "pesto", "pine nuts");
        createRecipe("Beef Tacos", "Classic Mexican tacos.", "Mexican", false, 4, "Cook ground beef and serve in taco shells.", "ground beef", "taco shells", "salsa");
        createRecipe("Tuna Sandwich", "A quick and easy sandwich.", "American", false, 1, "Mix tuna with mayonnaise and serve on bread.", "tuna", "mayonnaise", "bread");
        createRecipe("Mushroom Risotto", "A creamy and savory Italian rice dish.", "Italian", true, 4, "Cook risotto with mushrooms and parmesan.", "risotto rice", "mushrooms", "parmesan");
        createRecipe("Chicken Curry", "A flavorful and aromatic curry.", "Indian", false, 4, "Simmer chicken in a curry sauce.", "chicken", "curry powder", "coconut milk");
    }

    private void createRecipe(String name, String description, String category, boolean isVegetarian, int servings, String instructions, String... ingredientNames) {
        RecipeEntity recipe = new RecipeEntity();
        recipe.setName(name);
        recipe.setDescription(description);
        recipe.setCategory(category);
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
        recipeRepository.save(recipe);
    }

    @Test
    void testGetRecipes_NoFilters() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertNotNull(response);
        assertEquals(10, response.getData().size());
    }


    @Test
    void testGetRecipes_FilterByVegetarian() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setIsVegetarian(true);
        RecipeListResponse response = recipeService.getRecipes(filter);
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
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setServings(4);
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(4, response.getData().size());
        assertTrue(response.getData().stream().allMatch(r -> r.getServings() == 4));
        List<String> names = response.getData().stream().map(Recipe::getName).toList();
        assertTrue(names.containsAll(Arrays.asList("Vegetable Stir-Fry", "Beef Tacos", "Mushroom Risotto", "Chicken Curry")));
    }

    @Test
    void testGetRecipes_IncludeSingleIngredient() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setIncludeIngredients(Collections.singletonList("carrot"));
        RecipeListResponse response = recipeService.getRecipes(filter);
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
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setIncludeIngredients(Arrays.asList("pasta", "eggs"));
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(2, response.getData().size());
        assertEquals("Pesto Pasta", response.getData().getFirst().getName());
        assertEquals("Spaghetti Carbonara", response.getData().get(1).getName());
    }

    @Test
    void testGetRecipes_ExcludeSingleIngredient() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setExcludeIngredients(Collections.singletonList("chicken"));
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(8, response.getData().size());
        assertTrue(response.getData().stream().noneMatch(r -> r.getName().contains("Chicken")));
    }

    @Test
    void testGetRecipes_ExcludeMultipleIngredients() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setExcludeIngredients(Arrays.asList("chicken", "pasta"));
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(6, response.getData().size());
        // More robust check: ensure none of the excluded ingredients are in the results
        for (Recipe recipe : response.getData()) {
            List<String> ingredientNames = recipe.getIngredients().stream().map(com.recipe.manager.dto.Ingredient::getName).map(String::toLowerCase).toList();
            assertFalse(ingredientNames.contains("chicken"));
            assertFalse(ingredientNames.contains("pasta"));
        }
    }

    @Test
    void testGetRecipes_IncludeAndExcludeIngredients() {
        // Test for recipes that have 'chicken' but NOT 'tomato'
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setIncludeIngredients(Collections.singletonList("chicken"));
        filter.setExcludeIngredients(Collections.singletonList("tomato"));
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(1, response.getData().size());
        Recipe recipe = response.getData().getFirst();
        assertEquals("Chicken Curry", recipe.getName());
        assertEquals("A flavorful and aromatic curry.", recipe.getDescription());
        assertFalse(recipe.getIsVegetarian());
        assertEquals(4, recipe.getServings());
    }

    @Test
    void testGetRecipes_FilterByInstruction() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setInstruction("wok");
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(1, response.getData().size());
        assertEquals("Vegetable Stir-Fry", response.getData().getFirst().getName());
    }

    @Test
    void testGetRecipes_ComplexFilter() {
        RecipeSearchRequest filter = new RecipeSearchRequest();
        filter.setIsVegetarian(true);
        filter.setServings(4);
        filter.setIncludeIngredients(Collections.singletonList("mushrooms"));
        RecipeListResponse response = recipeService.getRecipes(filter);
        assertEquals(1, response.getData().size());
        assertEquals("Mushroom Risotto", response.getData().getFirst().getName());
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
        Long nonExistentId = -1L;

        RecipeNotFoundException exception = assertThrows(RecipeNotFoundException.class, () -> {
            recipeService.removeRecipe(nonExistentId);
        });

        assertEquals("Recipe not found", exception.getMessage());
    }
}
