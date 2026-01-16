package com.recipe.manager.service;

import com.recipe.manager.dto.CreateRecipeRequest;
import com.recipe.manager.dto.PaginationInfo;
import com.recipe.manager.dto.Recipe;
import com.recipe.manager.dto.RecipeListResponse;
import com.recipe.manager.entity.IngredientEntity;
import com.recipe.manager.entity.RecipeEntity;
import com.recipe.manager.entrypoint.exception.ApiErrorCode;
import com.recipe.manager.entrypoint.exception.ApiException;
import com.recipe.manager.repository.RecipeRepository;
import com.recipe.manager.service.mapper.IngredientMapper;
import com.recipe.manager.service.mapper.RecipeMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final IngredientMapper ingredientMapper;

    public RecipeService(RecipeRepository recipeRepository, RecipeMapper recipeMapper, IngredientMapper ingredientMapper) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.ingredientMapper = ingredientMapper;
    }

    @Transactional
    public Recipe addRecipe(CreateRecipeRequest createRecipeRequest) {
        RecipeEntity recipeEntity = recipeMapper.toEntity(createRecipeRequest);

        List<IngredientEntity> ingredientEntities = createRecipeRequest.getIngredients().stream()
                .map(ingredientMapper::map)
                .peek(ingredientEntity -> ingredientEntity.setRecipe(recipeEntity))
                .collect(Collectors.toList());

        recipeEntity.setIngredients(ingredientEntities);

        RecipeEntity savedRecipe = recipeRepository.save(recipeEntity);
        return recipeMapper.toDto(savedRecipe);
    }

    public RecipeListResponse getRecipes(Boolean isVegetarian, Integer servings, List<String> includeIngredients, List<String> excludeIngredients, String instruction, Integer page, Integer limit) {
        Specification<RecipeEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isVegetarian != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVegetarian"), isVegetarian));
            }

            if (servings != null) {
                predicates.add(criteriaBuilder.equal(root.get("serving"), servings));
            }

            if (instruction != null && !instruction.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("instructions")), "%" + instruction.toLowerCase() + "%"));
            }

            if (includeIngredients != null && !includeIngredients.isEmpty()) {
                Join<RecipeEntity, IngredientEntity> ingredientJoin = root.join("ingredients");
                List<Predicate> ingredientPredicates = new ArrayList<>();
                for (String ingredient : includeIngredients) {
                    ingredientPredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(ingredientJoin.get("name")), ingredient.toLowerCase()));
                }
                query.distinct(true);
                predicates.add(criteriaBuilder.or(ingredientPredicates.toArray(new Predicate[0])));
            }

            if (excludeIngredients != null && !excludeIngredients.isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<RecipeEntity> subRoot = subquery.from(RecipeEntity.class);
                Join<RecipeEntity, IngredientEntity> subJoin = subRoot.join("ingredients");

                List<Predicate> excludePredicates = new ArrayList<>();
                for (String ingredient : excludeIngredients) {
                    excludePredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(subJoin.get("name")), ingredient.toLowerCase()));
                }

                subquery.select(subRoot.get("id"))
                        .where(criteriaBuilder.or(excludePredicates.toArray(new Predicate[0])));

                predicates.add(criteriaBuilder.not(root.get("id").in(subquery)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page != null ? page : 0, limit != null ? limit : 10);
        Page<RecipeEntity> recipePage = recipeRepository.findAll(spec, pageable);

        RecipeListResponse response = new RecipeListResponse();
        PaginationInfo paginationInfo = new PaginationInfo();

        paginationInfo.setPage(recipePage.getNumber());
        paginationInfo.setLimit(recipePage.getSize());
        paginationInfo.setTotalItems((int) recipePage.getTotalElements());

        response.setData(recipePage.getContent().stream().map(recipeMapper::toDto).collect(Collectors.toList()));
        response.setPagination(paginationInfo);

        return response;
    }

    @Transactional
    public void removeRecipe(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Recipe not found", ApiErrorCode.NotFound);
        }
        recipeRepository.deleteById(id);
    }
}
