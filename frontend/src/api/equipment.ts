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

// ─── 设备预约 ───
export function getEquipmentBookings(params?: any) {
  return request.get('/equipment-bookings', { params })
}

export function getMyBookings() {
  return request.get('/equipment-bookings/my')
}

export function getBookingById(id: string) {
  return request.get(`/equipment-bookings/${id}`)
}

export function createBooking(data: any) {
  return request.post('/equipment-bookings', data)
}

export function updateBooking(id: string, data: any) {
  return request.put(`/equipment-bookings/${id}`, data)
}

export function cancelBooking(id: string) {
  return request.delete(`/equipment-bookings/${id}`)
}

export function checkinBooking(id: string) {
  return request.put(`/equipment-bookings/${id}/checkin`)
}

export function approveBooking(id: string, data: any) {
  return request.put(`/equipment-bookings/${id}/approve`, data)
}

export function getPendingApprovals(params?: any) {
  return request.get('/equipment-bookings/pending-approvals', { params })
}
