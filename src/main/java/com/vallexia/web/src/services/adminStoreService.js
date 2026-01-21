import api from './api';

export const adminStoreService = {
  async listStores() {
    const resp = await api.get('/v1/admin/stores');
    return resp.data;
  },

  async createStore(payload) {
    const resp = await api.post('/v1/admin/stores', payload);
    return resp.data;
  },

  async updateStore(storeId, payload) {
    const resp = await api.put(`/v1/admin/stores/${storeId}`, payload);
    return resp.data;
  },
};

