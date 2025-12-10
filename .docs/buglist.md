Overview over bugs that needs to be fixed:
- invalid email or password shows "An unexpected error occurred. Please try again."

Need to remove from both backend and frontend and their respected bruno api test file:
 - remove all create recipe feature
 - remove all edit recipe feature
 - remove all delete recipe feature
 - remove all isPublic/isPrivate feature
 - remove all neutration recipe calculation
 - remove all difficulty feature
 - remove old search feature? 
    (src/main/java/com/vallexia/recipe/util/AllergenCompatibilityUtil.java)
    src/main/java/com/vallexia/recipe/service/specification/RecipeSortHelper.java
    src/main/java/com/vallexia/recipe/service/specification/DietaryRestrictionFilter.java
    src/main/java/com/vallexia/recipe/service/specification/RecipeSpecificationBuilder.java
 - remove all recipe seeding data

 - rework recipe service/controller/dto/enitiy to fit spoonacular API
 - rework isFavourite
