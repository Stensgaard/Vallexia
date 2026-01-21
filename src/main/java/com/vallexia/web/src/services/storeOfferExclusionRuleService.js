import api from './api';

export const storeOfferExclusionRuleService = {
  async listRules() {
    const resp = await api.get('/v1/admin/store-offer-filters');
    return resp.data;
  },

  async createRule(payload) {
    const resp = await api.post('/v1/admin/store-offer-filters', payload);
    return resp.data;
  },

  async updateRule(ruleId, payload) {
    const resp = await api.put(`/v1/admin/store-offer-filters/${ruleId}`, payload);
    return resp.data;
  },

  async enableRule(ruleId) {
    const resp = await api.patch(`/v1/admin/store-offer-filters/${ruleId}/enable`);
    return resp.data;
  },

  async disableRule(ruleId) {
    const resp = await api.patch(`/v1/admin/store-offer-filters/${ruleId}/disable`);
    return resp.data;
  },

  async deleteRule(ruleId) {
    const resp = await api.delete(`/v1/admin/store-offer-filters/${ruleId}`);
    return resp.data;
  },
};
