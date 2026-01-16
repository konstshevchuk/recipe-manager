package com.recipe.manager.dto;

import java.util.List;

public class RecipeSearchFilter {

    private final Integer servings;
    private final List<String> includeIngredients;
    private final List<String> excludeIngredients;
    private final String instructions;
    private final Boolean isVegetarian;
    private final int page;
    private final int limit;

    private RecipeSearchFilter(Builder builder) {
        this.servings = builder.servings;
        this.includeIngredients = builder.includeIngredients;
        this.excludeIngredients = builder.excludeIngredients;
        this.instructions = builder.instructions;
        this.isVegetarian = builder.isVegetarian;
        this.page = builder.page;
        this.limit = builder.limit;
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

    public int getLimit() {
        return limit;
    }

    public static class Builder {
        private Integer servings;
        private List<String> includeIngredients;
        private List<String> excludeIngredients;
        private String instructions;
        private Boolean isVegetarian;
        private int page = 1;
        private int limit = 20;

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

        public Builder limit(Integer limit) {
            if (limit != null) {
                this.limit = limit;
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
