import request from './request'

export function getLabs() {
  return request.get('/labs')
}

export function createLab(data: any) {
  return request.post('/labs', data)
}

export function updateLab(id: string, data: any) {
  return request.put(`/labs/${id}`, data)
}

export function deleteLab(id: string) {
  return request.delete(`/labs/${id}`)
}

export function getFloors(labId: string) {
  return request.get(`/labs/${labId}/floors`)
}

export function createFloor(labId: string, data: any) {
  return request.post(`/labs/${labId}/floors`, data)
}

export function getRooms(floorId: string) {
  return request.get(`/floors/${floorId}/rooms`)
}

export function createRoom(floorId: string, data: any) {
  return request.post(`/floors/${floorId}/rooms`, data)
}

export function getRoomById(id: string) {
  return request.get(`/rooms/${id}`)
}
