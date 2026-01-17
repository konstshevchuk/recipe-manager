package com.recipe.manager.dto;

import org.springframework.data.domain.Sort;

import java.util.List;

public class RecipeSearchFilter {


    private final Integer servings;
    private final List<String> includeIngredients;
    private final List<String> excludeIngredients;
    private final String instructions;
    private final Boolean isVegetarian;
    private final int page;
    private final int pageSize;
    private final String orderBy;
    private final Sort.Direction direction;

    private RecipeSearchFilter(Builder builder) {
        this.servings = builder.servings;
        this.includeIngredients = builder.includeIngredients;
        this.excludeIngredients = builder.excludeIngredients;
        this.instructions = builder.instructions;
        this.isVegetarian = builder.isVegetarian;
        this.page = builder.page;
        this.pageSize = builder.pageSize;
        this.orderBy = builder.orderBy;
        this.direction = builder.direction;
    }


    public Integer getServings() {
        return servings;
    }

    public List<String> getIncludeIngredients() {
        return includeIngredients;
    }

    public List<String> getExcludeIngredients() {
        return excludeIngredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public Boolean getIsVegetarian() {
        return isVegetarian;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Sort.Direction getDirection() {
        return direction;
    }

    public static class Builder {
        private Integer servings;
        private List<String> includeIngredients;
        private List<String> excludeIngredients;
        private String instructions;
        private Boolean isVegetarian;
        private int page = 1;
        private int pageSize = 20;
        private String orderBy = "createdAt";
        private Sort.Direction direction = Sort.Direction.DESC;

        public Builder servings(Integer servings) {
            this.servings = servings;
            return this;
        }

        public Builder includeIngredients(List<String> includeIngredients) {
            this.includeIngredients = includeIngredients;
            return this;
        }

        public Builder excludeIngredients(List<String> excludeIngredients) {
            this.excludeIngredients = excludeIngredients;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this.self();
        }

        public Builder vegetarian(Boolean isVegetarian) {
            this.isVegetarian = isVegetarian;
            return this;
        }

        public Builder page(Integer page) {
            if (page != null) {
                this.page = page;
            }
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            if (pageSize != null) {
                this.pageSize = pageSize;
            }
            return this;
        }

        public Builder orderBy(String orderBy) {
            if (orderBy != null) {
                this.orderBy = orderBy;
            }
            return this;
        }

        public Builder direction(String direction) {
            if (direction != null) {
                try {
                    this.direction = Sort.Direction.fromString(direction);
                } catch (IllegalArgumentException e) {
                    // Fallback to default if invalid
                    this.direction = Sort.Direction.DESC;
                }
            }
            return this;
        }

        public RecipeSearchFilter build() {
            return new RecipeSearchFilter(this);
        }

        private Builder self() {
            return this;
        }
    }
}
