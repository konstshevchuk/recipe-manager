package com.recipe.manager.service.mapper;

import com.recipe.manager.data.UnitType;
import com.recipe.manager.dto.Ingredient;
import com.recipe.manager.dto.IngredientInput;
import com.recipe.manager.entity.IngredientEntity;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public IngredientEntity map(IngredientInput ingredient) {
        IngredientEntity ingredientEntity = new IngredientEntity();
        ingredientEntity.setName(ingredient.getName());
        ingredientEntity.setQuantity(ingredient.getQuantity());
        ingredientEntity.setUnit(UnitType.valueOf(ingredient.getUnit().getValue()));
        return ingredientEntity;
    }

    public Ingredient map(IngredientEntity ingredientEntity) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(ingredientEntity.getName());
        ingredient.setQuantity(ingredientEntity.getQuantity());
        ingredient.setUnit(Ingredient.UnitEnum.fromValue(ingredientEntity.getUnit().name()));
        return ingredient;
    }
}
