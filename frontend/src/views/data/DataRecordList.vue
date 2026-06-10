<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据记录</h2>
      <el-button type="primary" @click="showUpload">上传数据</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column prop="dataType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ { measurement: '测量数据', spectrum: '光谱数据', image: '图像数据', other: '其他' }[row.dataType] || row.dataType || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="equipmentName" label="设备" width="150" />
      <el-table-column prop="projectName" label="项目" width="150" />
      <el-table-column prop="userName" label="上传者" width="120" />
      <el-table-column prop="fileSize" label="大小" width="100">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ row.createdAt ? row.createdAt.replace('T', ' ').substring(0, 19) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button text type="primary" @click="handleDownload(row.id)">下载</el-button>
            <el-button text type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 16px; justify-content: flex-end;"
      @size-change="loadData"
      @current-change="loadData"
    />

    <el-dialog v-model="uploadDialogVisible" title="上传数据" width="500px">
      <el-form label-width="100px">
        <el-form-item label="文件" required>
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" :on-change="handleFileChange">
            <el-button>选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="uploadForm.dataType" clearable>
            <el-option label="测量数据" value="measurement" />
            <el-option label="光谱数据" value="spectrum" />
            <el-option label="图像数据" value="image" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getDataRecords, uploadDataRecord, deleteDataRecord, downloadDataRecord } from '@/api/data'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref<any[]>([])
const uploadDialogVisible = ref(false)
const pagination = reactive({ page: 0, size: 10, total: 0 })
const uploadForm = reactive({ dataType: '', description: '' })
const selectedFile = ref<File | null>(null)

function formatFileSize(bytes: number | null) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getDataRecords({ page: pagination.page, size: pagination.size })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showUpload() {
  Object.assign(uploadForm, { dataType: '', description: '' })
  selectedFile.value = null
  uploadDialogVisible.value = true
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    if (uploadForm.dataType) formData.append('dataType', uploadForm.dataType)
    if (uploadForm.description) formData.append('description', uploadForm.description)
    await uploadDataRecord(formData)
    ElMessage.success('文件已上传')
    uploadDialogVisible.value = false
    loadData()
  } finally {
    uploading.value = false
  }
}

async function handleDownload(id: string) {
  const res: any = await downloadDataRecord(id)
  const url = window.URL.createObjectURL(new Blob([res]))
  const a = document.createElement('a')
  a.href = url
  a.download = 'file'
  a.click()
  window.URL.revokeObjectURL(url)
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要删除该记录吗？', '警告', { type: 'warning' })
  await deleteDataRecord(id)
  ElMessage.success('记录已删除')
  loadData()
}

onMounted(loadData)
</script>
