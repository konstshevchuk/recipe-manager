
package com.recipe.manager.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.recipe.manager")
public class RecipeManagerLauncher {

    public static void main(String[] args) {
        SpringApplication.run(RecipeManagerLauncher.class, args);
    }
}
