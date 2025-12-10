invalid email or password shows "An unexpected error occurred. Please try again."

need to remove from both backend and frontend and their respected bruno api test file:
 - all recpies is now only coming from spoonacular so no longer can edit, remove, create so only get
 - need to remove isPublic and isFavourite
 - no need to calculate neutration from the recpies anymore as that will come from their endpoint
 - clean up dtos remove anything that is no longer needed (eg. recpiesDTO and its helper methods)
 - refactor old dtos or make a new one to get the serach result from the api to show to the user
 - remove difficulty feature