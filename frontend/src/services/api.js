import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
};

export const medicineAPI = {
  getAll: (params) => api.get('/medicines', { params }),
  getById: (id) => api.get(`/medicines/${id}`),
  create: (data) => api.post('/medicines', data),
  update: (id, data) => api.put(`/medicines/${id}`, data),
  delete: (id) => api.delete(`/medicines/${id}`),
  getLowStock: () => api.get('/medicines/low-stock'),
  getCategories: () => api.get('/medicines/categories'),
  getStockCount: (id) => api.get(`/medicines/${id}/stock-count`),
};

export const batchAPI = {
  add: (data) => api.post('/batches', data),
  getByMedicine: (medicineId) => api.get(`/batches/medicine/${medicineId}`),
  getExpiring: (days) => api.get('/batches/expiring', { params: { days } }),
  getExpired: () => api.get('/batches/expired'),
  dispose: (id) => api.put(`/batches/${id}/dispose`),
  getValuation: () => api.get('/batches/valuation'),
};

export const saleAPI = {
  create: (data) => api.post('/sales', data),
  getAll: (params) => api.get('/sales', { params }),
  getById: (id) => api.get(`/sales/${id}`),
  downloadInvoice: (id) => api.get(`/sales/${id}/invoice`, { responseType: 'blob' }),
  getTodaySummary: () => api.get('/sales/today/summary'),
};

export const supplierAPI = {
  getAll: (params) => api.get('/suppliers', { params }),
  getById: (id) => api.get(`/suppliers/${id}`),
  create: (data) => api.post('/suppliers', data),
  update: (id, data) => api.put(`/suppliers/${id}`, data),
  delete: (id) => api.delete(`/suppliers/${id}`),
};

export const purchaseOrderAPI = {
  getAll: (params) => api.get('/purchase-orders', { params }),
  getById: (id) => api.get(`/purchase-orders/${id}`),
  create: (supplierId, notes) =>
    api.post('/purchase-orders', null, { params: { supplierId, notes } }),
  markReceived: (id) => api.put(`/purchase-orders/${id}/receive`),
  cancel: (id) => api.put(`/purchase-orders/${id}/cancel`),
};

export const alertAPI = {
  getAll: (params) => api.get('/alerts', { params }),
  getUnresolvedCount: () => api.get('/alerts/count'),
  resolve: (id) => api.put(`/alerts/${id}/resolve`),
};

export const reportAPI = {
  expiryReport: (days) => api.get('/reports/expiry-report', { params: { days }, responseType: 'blob' }),
  salesSummary: (from, to) => api.get('/reports/sales-summary', { params: { from, to } }),
  stockValuation: () => api.get('/reports/stock-valuation'),
};

export default api;