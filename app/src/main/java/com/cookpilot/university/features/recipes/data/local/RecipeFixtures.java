package com.cookpilot.university.features.recipes.data.local;

import com.cookpilot.university.features.recipes.domain.model.Ingredient;
import com.cookpilot.university.features.recipes.domain.model.NutritionInfo;
import com.cookpilot.university.features.recipes.domain.model.Recipe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo temporal para el MVP universitario.
 *
 * Los IDs, imágenes, tiempos, nutrición, precios e ingredientes provienen
 * de CookPilot DEV. La fuente remota podrá sustituir esta clase cuando entre
 * Retrofit sin cambiar los consumidores del dominio.
 */
public final class RecipeFixtures {

    public static final String LOMO_SALTADO_ID =
            "6eefbbc4-7a7c-4934-94a0-352875f95a63";
    public static final String ARROZ_CON_POLLO_ID =
            "8a5bb7cb-0eab-4afd-8f24-a6e7b8d0b33b";
    public static final String TALLARINES_ROJOS_ID =
            "d08e6b64-95a8-4307-8fcd-c22256c881a7";
    public static final String PANQUEQUES_ID =
            "9ddc1894-2523-4622-be69-604061c39e52";
    public static final String ENSALADA_PASTA_POLLO_ID =
            "0a1acbe3-6695-469c-a091-292e2e8435e0";

    private static final List<Recipe> RECIPES = Collections.unmodifiableList(
            Arrays.asList(
                    panqueques(),
                    lomoSaltado(),
                    arrozConPollo(),
                    tallarinesRojos(),
                    ensaladaPastaPollo()
            )
    );

    private RecipeFixtures() {
    }

    public static List<Recipe> all() {
        return RECIPES;
    }

    private static Recipe lomoSaltado() {
        return new Recipe(
                LOMO_SALTADO_ID,
                "Lomo Saltado",
                "lomo_saltado",
                "https://media.cookpilot.pro/recipes/images/oficial_images/lomo_saltado.webp",
                4,
                35,
                32.82,
                87.19,
                new NutritionInfo(793, 42.34, 83.94, 30.76, 6.77),
                Arrays.asList(
                        ingredient(
                                "c919947a-e4cb-5c17-8649-b27708420be3",
                                "Papa Blanca",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/tubers.webp",
                                700,
                                "g"
                        ),
                        ingredient(
                                "b7dd16bc-4fbe-4fc2-9c3e-39ca1a3aaad0",
                                "Arroz Blanco Cocido",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/grains_cereal.webp",
                                600,
                                "g"
                        ),
                        ingredient(
                                "7e8f9a0b-1c2d-3e4f-5a6b-7c8d9e0f1a2b",
                                "Bistec de Res",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/meats_steak.webp",
                                600,
                                "g"
                        ),
                        ingredient(
                                "e5f6a7b8-0005-4000-8000-000000000005",
                                "Aceite Vegetal",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/fats_oils.webp",
                                500,
                                "ml"
                        ),
                        ingredient(
                                "e1808743-4931-50e1-8c22-554f4d2cd634",
                                "Cebolla Morada",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/vegetables_greens.webp",
                                250,
                                "g"
                        )
                )
        );
    }

    private static Recipe arrozConPollo() {
        return new Recipe(
                ARROZ_CON_POLLO_ID,
                "Arroz con Pollo Peruano",
                "arroz_con_pollo_verde",
                "https://media.cookpilot.pro/recipes/images/oficial_images/arroz_con_pollo_verde.webp",
                4,
                65,
                22.98,
                37.02,
                new NutritionInfo(951, 51.22, 86.18, 42.03, 4.43),
                Arrays.asList(
                        ingredient(
                                "36d9a081-dd2e-487b-966f-71ab9cd6331a",
                                "Presas de Pollo",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/meats_steak.webp",
                                900,
                                "g"
                        ),
                        ingredient(
                                "71fe2f2a-d5a9-42de-b868-c0eada81f5ac",
                                "Agua",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/beverages_drink.webp",
                                540,
                                "ml"
                        ),
                        ingredient(
                                "e8018315-d164-49b2-819a-9ad39c35da20",
                                "Arroz de Grano Largo",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/grains_cereal.webp",
                                360,
                                "g"
                        ),
                        ingredient(
                                "e1808743-4931-50e1-8c22-554f4d2cd634",
                                "Cebolla Morada",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/vegetables_greens.webp",
                                140,
                                "g"
                        ),
                        ingredient(
                                "d4e5f6a7-0021-4000-9000-000000000021",
                                "Cerveza Negra",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/beverages_drink.webp",
                                120,
                                "ml"
                        )
                )
        );
    }

    private static Recipe tallarinesRojos() {
        return new Recipe(
                TALLARINES_ROJOS_ID,
                "Tallarines Rojos con Pollo",
                "tallarines_rojos_con_pollo",
                "https://media.cookpilot.pro/recipes/images/oficial_images/tallarines_rojos_con_pollo.webp",
                4,
                50,
                23.78,
                36.22,
                new NutritionInfo(993, 51.50, 113.38, 36.55, 9.00),
                Arrays.asList(
                        ingredient(
                                "71fe2f2a-d5a9-42de-b868-c0eada81f5ac",
                                "Agua",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/beverages_drink.webp",
                                3000,
                                "ml"
                        ),
                        ingredient(
                                "36d9a081-dd2e-487b-966f-71ab9cd6331a",
                                "Presas de Pollo",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/meats_steak.webp",
                                850,
                                "g"
                        ),
                        ingredient(
                                "73041b26-6ecb-57a5-9afb-7f79b8762de3",
                                "Tomate",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/vegetables_greens.webp",
                                750,
                                "g"
                        ),
                        ingredient(
                                "f1f07868-fc7f-5d69-8e14-240b7a890954",
                                "Fideos",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/grains_cereal.webp",
                                500,
                                "g"
                        ),
                        ingredient(
                                "e1808743-4931-50e1-8c22-554f4d2cd634",
                                "Cebolla Morada",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/vegetables_greens.webp",
                                160,
                                "g"
                        )
                )
        );
    }

    private static Recipe panqueques() {
        return new Recipe(
                PANQUEQUES_ID,
                "Panqueques Americanos",
                "panqueques_americanos",
                "https://media.cookpilot.pro/recipes/images/oficial_images/panqueques_americanos.webp",
                4,
                25,
                7.54,
                30.47,
                new NutritionInfo(379, 11.29, 54.10, 12.50, 1.49),
                Arrays.asList(
                        ingredient(
                                "6aa95610-4b7e-556c-99eb-b582daebfc5c",
                                "Leche Fresca",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/dairy_milk.webp",
                                300,
                                "ml"
                        ),
                        ingredient(
                                "1a2b3c4d-0008-4000-8000-000000000008",
                                "Harina sin Preparar",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/grains_cereal.webp",
                                220,
                                "g"
                        ),
                        ingredient(
                                "d08eaf55-fb30-4096-8bd9-f68faacbe159",
                                "Mantequilla sin Sal",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/fats_oils.webp",
                                35,
                                "g"
                        ),
                        ingredient(
                                "e99d25bc-26f1-5ee1-ad8e-84a96c068a09",
                                "Azúcar Blanca",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/sweets_sugar.webp",
                                30,
                                "g"
                        ),
                        ingredient(
                                "4991affc-bade-51e3-985d-8edc45c7948a",
                                "Huevo",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/eggs.webp",
                                2,
                                "unit"
                        )
                )
        );
    }

    private static Recipe ensaladaPastaPollo() {
        return new Recipe(
                ENSALADA_PASTA_POLLO_ID,
                "Ensalada de Pasta con Pollo",
                "ensalada_de_pasta_con_pollo",
                "https://media.cookpilot.pro/recipes/images/oficial_images/ensalada_de_pasta_con_pollo.webp",
                4,
                40,
                26.91,
                61.10,
                new NutritionInfo(740, 43.49, 80.46, 31.63, 11.43),
                Arrays.asList(
                        ingredient(
                                "71fe2f2a-d5a9-42de-b868-c0eada81f5ac",
                                "Agua",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/beverages_drink.webp",
                                2500,
                                "ml"
                        ),
                        ingredient(
                                "e5f6a7b8-c9d0-4000-8000-000000000099",
                                "Pechuga de Pollo",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/meats_steak.webp",
                                400,
                                "g"
                        ),
                        ingredient(
                                "b40308af-e0cf-454c-b016-dbd6578eb5bf",
                                "Pasta Corta",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/grains_cereal.webp",
                                280,
                                "g"
                        ),
                        ingredient(
                                "bd0d3dca-7863-5c01-9482-5d8c50966de8",
                                "Mayonesa",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/misc_spices.webp",
                                140,
                                "g"
                        ),
                        ingredient(
                                "9357995b-d263-5cfd-8380-9a209fa54d19",
                                "Zanahoria",
                                "https://media.cookpilot.pro/cookpilot_media/images/icons/food_icons/vegetables_greens.webp",
                                120,
                                "g"
                        )
                )
        );
    }

    private static Ingredient ingredient(
            String id,
            String name,
            String imageUrl,
            double quantity,
            String unit
    ) {
        return new Ingredient(id, name, imageUrl, quantity, unit);
    }
}
