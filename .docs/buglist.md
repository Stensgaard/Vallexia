Overview over bugs that needs to be fixed:
- invalid email or password shows "An unexpected error occurred. Please try again."
- CANCELLED, EXPIRED from sub should be removed so only keep free, premium, family
- make sonar CI not run when it is dependablebot that made the pull request

Need to remove from both backend and frontend and their respected bruno api test file:
 - rework recipe service/controller/dto/enitiy to fit spoonacular API
 - rework Dietary Preferences on the profile setting, to make sure they allign with 
    what spoonacular API supports
 - rework isFavourite
