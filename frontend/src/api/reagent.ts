import request from './request'

// ─── 试剂品类 ───
export function getCategoryList(params?: any) {
  return request.get('/reagent-categories', { params })
}

export function getCategoryById(id: string) {
  return request.get(`/reagent-categories/${id}`)
}

export function createCategory(data: any) {
  return request.post('/reagent-categories', data)
}

export function updateCategory(id: string, data: any) {
  return request.put(`/reagent-categories/${id}`, data)
}

export function deleteCategory(id: string) {
  return request.delete(`/reagent-categories/${id}`)
}

// ─── 库存管理 ───
export function getInventoryList(params?: any) {
  return request.get('/reagent-inventory', { params })
}

export function getInventoryById(id: string) {
  return request.get(`/reagent-inventory/${id}`)
}

export function inboundReagent(data: any) {
  return request.post('/reagent-inventory/inbound', data)
}

export function useReagent(id: string, data: any) {
  return request.post(`/reagent-inventory/${id}/use`, data)
}

export function batchUseReagent(data: any) {
  return request.post('/reagent-inventory/batch-use', data)
}

export function getExpiringList() {
  return request.get('/reagent-inventory/expiring')
}

export function getOutOfStockList() {
  return request.get('/reagent-inventory/out-of-stock')
}

export function recommendFefo(categoryId: string) {
  return request.get('/reagent-inventory/fefo', { params: { categoryId } })
}

export function updateInventory(id: string, data: any) {
  return request.put(`/reagent-inventory/${id}`, data)
}

export function deleteInventory(id: string) {
  return request.delete(`/reagent-inventory/${id}`)
}

export function updateAlertLevels() {
  return request.post('/reagent-inventory/update-alert-levels')
}

// ─── 交易记录 ───
export function getTransactionList(params?: any) {
  return request.get('/reagent-transactions', { params })
}

export function traceByBarcode(barcode: string) {
  return request.get('/reagent-transactions/trace', { params: { barcode } })
}

// ─── 位置管理 ───
export function getLocationTree(roomId: string) {
  return request.get('/storage-locations/tree', { params: { roomId } })
}

export function getLocationById(id: string) {
  return request.get(`/storage-locations/${id}`)
}

export function createLocation(data: any) {
  return request.post('/storage-locations', data)
}

export function updateLocation(id: string, data: any) {
  return request.put(`/storage-locations/${id}`, data)
}

export function deleteLocation(id: string) {
  return request.delete(`/storage-locations/${id}`)
}

// ─── 试剂位置搜索 ───
export function searchReagentLocation(keyword: string) {
  return request.get('/reagent-locations/search', { params: { keyword } })
}

// ─── 报表 ───
export function getReagentReport(params?: any) {
  return request.get('/reagent-reports/usage', { params })
}

// ─── 禁忌规则 ───
export function getIncompatibilityRules(params?: any) {
  return request.get('/incompatibility-rules', { params })
}

export function getIncompatibilityRuleById(id: string) {
  return request.get(`/incompatibility-rules/${id}`)
}

export function createIncompatibilityRule(data: any) {
  return request.post('/incompatibility-rules', data)
}

export function updateIncompatibilityRule(id: string, data: any) {
  return request.put(`/incompatibility-rules/${id}`, data)
}

export function deleteIncompatibilityRule(id: string) {
  return request.delete(`/incompatibility-rules/${id}`)
}

export function checkIncompatibility(data: any) {
  return request.post('/incompatibility-rules/check', data)
}
