import request from './request'

export function startUsage(data: { equipmentId: string; projectId?: string; purpose?: string }) {
  return request.post('/usage/start', data)
}

export function endUsage(id: string) {
  return request.put(`/usage/${id}/end`)
}

export function getUsageRecords(params?: any) {
  return request.get('/usage', { params })
}

export function getActiveUsages() {
  return request.get('/usage/active')
}
