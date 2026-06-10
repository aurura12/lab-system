<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备管理</h2>
      <el-button type="primary" @click="showAdd">添加设备</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="filters.keyword" placeholder="搜索设备..." clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 140px" @change="loadData">
        <el-option label="可用" value="available" />
        <el-option label="使用中" value="in_use" />
        <el-option label="维护中" value="maintenance" />
        <el-option label="已退役" value="retired" />
      </el-select>
      <el-select v-model="filters.category" placeholder="类型" clearable style="width: 140px" @change="loadData">
        <el-option label="光谱仪" value="spectrometer" />
        <el-option label="显微镜" value="microscope" />
        <el-option label="离心机" value="centrifuge" />
        <el-option label="分析仪" value="analyzer" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="model" label="型号" width="120" />
      <el-table-column prop="serialNumber" label="序列号" width="120" />
      <el-table-column prop="roomName" label="房间" width="100" />
      <el-table-column prop="category" label="类型" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ categoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="manufacturer" label="制造商" width="120" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button text type="primary" @click="viewDetail(row.id)">详情</el-button>
            <el-button text type="primary" @click="showEdit(row)">编辑</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '添加设备'" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" />
        </el-form-item>
        <el-form-item label="序列号">
          <el-input v-model="form.serialNumber" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.category" clearable>
            <el-option label="光谱仪" value="spectrometer" />
            <el-option label="显微镜" value="microscope" />
            <el-option label="离心机" value="centrifuge" />
            <el-option label="分析仪" value="analyzer" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="可用" value="available" />
            <el-option label="使用中" value="in_use" />
            <el-option label="维护中" value="maintenance" />
            <el-option label="已退役" value="retired" />
          </el-select>
        </el-form-item>
        <el-form-item label="制造商">
          <el-input v-model="form.manufacturer" />
        </el-form-item>
        <el-form-item label="购买日期">
          <el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEquipmentList, createEquipment, updateEquipment, deleteEquipment } from '@/api/equipment'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''

const filters = reactive({ keyword: '', status: '', category: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  name: '', model: '', serialNumber: '', category: '', status: 'available',
  manufacturer: '', purchaseDate: '', notes: '',
})

function statusType(s: string) {
  return { available: 'success', in_use: 'primary', maintenance: 'warning', retired: 'info' }[s] || 'info'
}

function statusLabel(s: string) {
  return { available: '可用', in_use: '使用中', maintenance: '维护中', retired: '已退役' }[s] || s
}

function categoryLabel(c: string) {
  return { spectrometer: '光谱仪', microscope: '显微镜', centrifuge: '离心机', analyzer: '分析仪', other: '其他' }[c] || c || '-'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getEquipmentList({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      category: filters.category || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { name: '', model: '', serialNumber: '', category: '', status: 'available', manufacturer: '', purchaseDate: '', notes: '' })
  dialogVisible.value = true
}

function showEdit(row: any) {
  isEdit.value = true
  editId = row.id
  Object.assign(form, { name: row.name, model: row.model, serialNumber: row.serialNumber, category: row.category, status: row.status, manufacturer: row.manufacturer, purchaseDate: row.purchaseDate, notes: row.notes })
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateEquipment(editId, form)
    ElMessage.success('设备已更新')
  } else {
    await createEquipment(form)
    ElMessage.success('设备已创建')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要删除该设备吗？', '警告', { type: 'warning' })
  await deleteEquipment(id)
  ElMessage.success('设备已删除')
  loadData()
}

function viewDetail(id: string) {
  router.push(`/equipment/${id}`)
}

onMounted(loadData)
</script>
