# Spoonacular API Endpoint Costs (key endpoints)

| Endpoint | Cost model |
| --- | --- |
| GET https://api.spoonacular.com/recipes/findByIngredients | 1 point + 0.01 points per recipe returned |
| GET https://api.spoonacular.com/recipes/complexSearch | 1 point + 0.01 points per result; extras: `fillIngredients=true` +0.025 per recipe; any nutrient filter +1 point; `addRecipeInformation=true` +0.025 per recipe; `addRecipeInstructions=true` +0.025 per recipe; `addRecipeNutrition=true` +0.025 per recipe (and auto-adds recipe info). **Note:** All filter parameters (`cuisine`, `excludeCuisine`, `intolerances`, `diet`, etc.) are FREE - only nutrient filters (e.g., `minCarbs`, `maxCalories`) add extra points. |
| GET https://api.spoonacular.com/recipes/informationBulk | 1 point for the first recipe + 0.5 points for each additional recipe returned |

## Recommended Approach: Option B (One-Step)

For finding recipes with discounted ingredients, use **Option B (complexSearch)** with the one-step approach to get all recipe details in a single call.

### Endpoint Configuration
```
GET https://api.spoonacular.com/recipes/complexSearch
?includeIngredients=<discounted_list>
&addRecipeInformation=true
&addRecipeInstructions=true
&addRecipeNutrition=true
&number=20
&intolerances=<user_intolerances>        # Optional: e.g., gluten,dairy
&cuisine=<preferred_cuisines>             # Optional: e.g., italian,mexican
&excludeCuisine=<excluded_cuisines>      # Optional: e.g., chinese,japanese
&diet=<diet_type>                         # Optional: e.g., vegetarian,vegan
&apiKey=your_api_key_here
```

**Filter Parameters (No Extra Cost - All Free):**
- `intolerances`: Comma-separated list (e.g., "gluten,dairy,soy")
- `cuisine`: Comma-separated list of preferred cuisines to include
- `excludeCuisine`: Comma-separated list of cuisines to exclude
- `diet`: Single diet type (e.g., "vegetarian", "vegan", "ketogenic")
- All other filter parameters (except nutrient filters) are also free
- These filters help narrow results to user preferences and dietary restrictions

### Cost Calculation (20 recipes)
- Base cost: 1 point
- Per result: 0.01 × 20 = 0.2 points
- Recipe information: 0.025 × 20 = 0.5 points
- Recipe instructions: 0.025 × 20 = 0.5 points
- Recipe nutrition: 0.025 × 20 = 0.5 points
- **Total: 2.7 points per call**

**Note:** All filter parameters (`cuisine`, `excludeCuisine`, `intolerances`, `diet`, etc.) are FREE and do NOT increase the cost. Only nutrient filters (e.g., `minCarbs`, `maxCalories`, `minProtein`, `maxFat`) add an extra 1 point to the base cost.

### Why This Approach?
- Most cost-effective: Getting full details upfront (2.7 points) is cheaper than a two-step approach (4.7+ points) due to the base 1 point cost per API call
- Better UX: Users get all information (instructions, nutrition, ingredients) immediately to make informed decisions
- Single request: Reduces latency and simplifies error handling



Cache recipes in the databaes for 1 hour, and modifiy search to allow to serach for cached first and then make a endpoint call