import request from './request'

export function getDashboardOverview() {
  return request.get('/dashboard/overview')
}

export function getEquipmentUtilization(params?: { startDate?: string; endDate?: string }) {
  return request.get('/dashboard/equipment-utilization', { params })
}

export function getUsageTrend(params?: { days?: number }) {
  return request.get('/dashboard/usage-trend', { params })
}

export function getProjectDataDistribution() {
  return request.get('/dashboard/project-data-distribution')
}

export function getRecentActivity() {
  return request.get('/dashboard/recent-activity')
}
