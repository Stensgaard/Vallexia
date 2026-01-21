import api from "./api";

export const storeOffersAdminService = {
  async getStores() {
    const resp = await api.get("/v1/store-offers/stores");
    return resp.data;
  },

  async triggerScrapeAllStores() {
    // Scraping can take a while; override default 10s timeout.
    const resp = await api.post("/v1/store-offers/scrape", null, { timeout: 120000 });
    return resp.data;
  },

  async triggerScrapeStore(storeId) {
    const resp = await api.post(`/v1/store-offers/scrape/stores/${storeId}`, null, {
      timeout: 120000,
    });
    return resp.data;
  },

  async getUnmatchedOffers({ storeId = null, date = null, includeDismissed = false } = {}) {
    const resp = await api.get("/v1/store-offers/unmatched", {
      params: { storeId, date, includeDismissed },
    });
    return resp.data;
  },

  async dismissOffer(offerId) {
    const resp = await api.post(`/v1/store-offers/offers/${offerId}/dismiss`);
    return resp.data;
  },

  async undoDismissOffer(offerId) {
    const resp = await api.post(`/v1/store-offers/offers/${offerId}/undo-dismiss`);
    return resp.data;
  },

  async triggerMatching({ date = null } = {}) {
    // Matching can take longer than 10s when many offers are present.
    const resp = await api.post("/v1/store-offers/match", null, {
      params: { date },
      timeout: 120000,
    });
    return resp.data;
  },

  async manualMatchOffer({
    offerId,
    ingredientId,
    locale = "da",
    createAlias = true,
  }) {
    const resp = await api.post(`/v1/store-offers/offers/${offerId}/match-ingredient`, null, {
      params: { ingredientId, locale, createAlias },
    });
    return resp.data;
  },
};

