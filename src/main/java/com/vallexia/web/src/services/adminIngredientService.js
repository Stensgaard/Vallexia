import api from "./api";

export const adminIngredientService = {
  async searchIngredients({ q, locale = "da", limit = 20 }) {
    const resp = await api.get("/v1/admin/ingredients/search", {
      params: { q, locale, limit },
    });
    return resp.data;
  },

  async createIngredient({ canonicalName, translations }) {
    const resp = await api.post("/v1/admin/ingredients", {
      canonicalName,
      translations,
    });
    return resp.data;
  },

  async addAlias(ingredientId, { locale, alias, priority = 0 }) {
    const resp = await api.post(`/v1/admin/ingredients/${ingredientId}/aliases`, {
      locale,
      alias,
      priority,
    });
    return resp.data;
  },
};

