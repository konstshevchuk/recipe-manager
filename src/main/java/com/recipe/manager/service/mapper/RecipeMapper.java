package com.recipe.manager.service.mapper;

import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.PaginationInfo;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    private final IngredientMapper ingredientMapper;

    public RecipeMapper(IngredientMapper ingredientMapper) {
        this.ingredientMapper = ingredientMapper;
    }

    public RecipeEntity toEntity(CreateRecipeRequest request) {
        RecipeEntity entity = new RecipeEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setInstructions(request.getInstructions());
        entity.setVegeterian(request.getIsVegetarian());
        entity.setServing(request.getServings());
        entity.setIngredients(request.getIngredients().stream().map(ingredientMapper::map).collect(Collectors.toList()));
        return entity;
    }

    public Recipe toDto(RecipeEntity entity) {
        Recipe recipe = new Recipe();
        recipe.setId(entity.getId());
        recipe.setName(entity.getName());
        recipe.setDescription(entity.getDescription());
        recipe.setInstructions(entity.getInstructions());
        recipe.setIsVegetarian(entity.getVegeterian());
        recipe.setServings(entity.getServing());
        recipe.setIngredients(entity.getIngredients().stream()
                .map(ingredientMapper::map)
                .collect(Collectors.toList()));
        recipe.setCreatedAt(OffsetDateTime.ofInstant(entity.getCreatedAt(), OffsetDateTime.now().getOffset()));
        return recipe;
    }

    public RecipeListResponse map(Page<RecipeEntity> recipePage, int page, int pageSize, long totalCount) {
        RecipeListResponse response = new RecipeListResponse();
        response.setData(recipePage.getContent().stream().map(this::toDto).collect(Collectors.toList()));
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(page);
        paginationInfo.setPageSize(pageSize);
        paginationInfo.setTotalItems(totalCount);
        response.setPagination(paginationInfo);
        return response;
    }
}
