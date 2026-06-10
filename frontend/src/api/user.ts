import request from './request'

export function getUsers(params?: { keyword?: string; page?: number; size?: number }) {
  return request.get('/users', { params })
}

export function getUserById(id: string) {
  return request.get(`/users/${id}`)
}

export function createUser(data: any) {
  return request.post('/users', data)
}

export function updateUser(id: string, data: any) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id: string) {
  return request.delete(`/users/${id}`)
}
