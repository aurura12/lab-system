import request from './request'

export function getEquipmentList(params?: any) {
  return request.get('/equipment', { params })
}

export function getEquipmentById(id: string) {
  return request.get(`/equipment/${id}`)
}

export function createEquipment(data: any) {
  return request.post('/equipment', data)
}

export function updateEquipment(id: string, data: any) {
  return request.put(`/equipment/${id}`, data)
}

export function deleteEquipment(id: string) {
  return request.delete(`/equipment/${id}`)
}

export function updateEquipmentStatus(id: string, data: { status: string }) {
  return request.put(`/equipment/${id}/status`, data)
}
