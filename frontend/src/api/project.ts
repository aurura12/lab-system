import request from './request'

export function getProjects(params?: { keyword?: string; page?: number; size?: number }) {
  return request.get('/projects', { params })
}

export function getProjectById(id: string) {
  return request.get(`/projects/${id}`)
}

export function createProject(data: any) {
  return request.post('/projects', data)
}

export function updateProject(id: string, data: any) {
  return request.put(`/projects/${id}`, data)
}

export function deleteProject(id: string) {
  return request.delete(`/projects/${id}`)
}

export function getProjectMembers(projectId: string) {
  return request.get(`/projects/${projectId}/members`)
}

export function addProjectMember(projectId: string, data: { userId: string; roleInProject?: string }) {
  return request.post(`/projects/${projectId}/members`, data)
}

export function removeProjectMember(projectId: string, userId: string) {
  return request.delete(`/projects/${projectId}/members/${userId}`)
}
