import request from './request'

export function getDataRecords(params?: any) {
  return request.get('/data', { params })
}

export function getDataRecordById(id: string) {
  return request.get(`/data/${id}`)
}

export function uploadDataRecord(formData: FormData) {
  return request.post('/data/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteDataRecord(id: string) {
  return request.delete(`/data/${id}`)
}

export function downloadDataRecord(id: string) {
  return request.get(`/data/${id}/download`, { responseType: 'blob' })
}
