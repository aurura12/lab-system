<template>
  <div class="page-container">
    <div class="page-header">
      <h2>维保工单</h2>
      <el-button type="primary" @click="showAdd">新建工单</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filters.equipmentId" placeholder="选择设备" filterable clearable style="width: 200px" @change="loadData">
        <el-option v-for="e in equipmentOptions" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px" @change="loadData">
        <el-option label="待处理" value="pending" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="equipmentName" label="设备" min-width="130" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">{{ typeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assigneeName" label="负责人" width="100" />
      <el-table-column prop="scheduledDate" label="计划日期" width="100" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <div class="action-cell" style="flex-direction: row; gap: 4px;">
            <el-button v-if="row.status === 'pending' || row.status === 'in_progress'" text type="success" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status !== 'completed'" text type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'pending'" text type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑工单' : '新建工单'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备" required>
          <el-select v-model="form.equipmentId" filterable placeholder="选择设备" style="width: 100%">
            <el-option v-for="e in equipmentOptions" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="例行维护" value="routine" />
            <el-option label="维修" value="repair" />
            <el-option label="巡检" value="inspection" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.assigneeId" filterable clearable placeholder="选择负责人" style="width: 100%">
            <el-option v-for="u in userOptions" :key="u.id" :label="u.realName || u.username" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="form.scheduledDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="处理结果">
          <el-input v-model="form.resolution" type="textarea" :rows="2" />
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
import { getMaintenanceOrders, createMaintenanceOrder, updateMaintenanceOrder, completeMaintenanceOrder, deleteMaintenanceOrder, getEquipmentList } from '@/api/equipment'
import { getUsers } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const equipmentOptions = ref<any[]>([])
const userOptions = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''

const filters = reactive({ equipmentId: '', status: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  equipmentId: '', title: '', type: 'routine', priority: 'medium',
  assigneeId: '', scheduledDate: '', description: '', resolution: '',
})

function typeLabel(s: string) { return { routine: '例行', repair: '维修', inspection: '巡检' }[s] || s }
function priorityType(s: string) { return { low: 'info', medium: '', high: 'warning', urgent: 'danger' }[s] || 'info' }
function priorityLabel(s: string) { return { low: '低', medium: '中', high: '高', urgent: '紧急' }[s] || s }
function statusType(s: string) { return { pending: 'info', in_progress: 'primary', completed: 'success', cancelled: 'info' }[s] || 'info' }
function statusLabel(s: string) { return { pending: '待处理', in_progress: '进行中', completed: '已完成', cancelled: '已取消' }[s] || s }

async function loadEquipment() {
  try { const r: any = await getEquipmentList({ page: 0, size: 100 }); equipmentOptions.value = r.data?.content || [] } catch { /* ignore */ }
}
async function loadUsers() {
  try { const r: any = await getUsers({ page: 0, size: 100 }); userOptions.value = r.data?.content || [] } catch { /* ignore */ }
}
async function loadData() {
  loading.value = true
  try {
    const r: any = await getMaintenanceOrders({
      equipmentId: filters.equipmentId || undefined, status: filters.status || undefined,
      page: pagination.page, size: pagination.size,
    })
    tableData.value = r.data?.content || []; pagination.total = r.data?.totalElements || 0
  } finally { loading.value = false }
}

function showAdd() {
  isEdit.value = false; Object.assign(form, { equipmentId: '', title: '', type: 'routine', priority: 'medium', assigneeId: '', scheduledDate: '', description: '', resolution: '' })
  dialogVisible.value = true
}
function showEdit(row: any) {
  isEdit.value = true; editId = row.id
  Object.assign(form, { equipmentId: row.equipmentId, title: row.title, type: row.type, priority: row.priority, assigneeId: row.assigneeId || '', scheduledDate: row.scheduledDate || '', description: row.description || '', resolution: row.resolution || '' })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.equipmentId || !form.title) { ElMessage.warning('请填写设备和标题'); return }
  if (isEdit.value) { await updateMaintenanceOrder(editId, form); ElMessage.success('工单已更新') }
  else { await createMaintenanceOrder(form); ElMessage.success('工单已创建') }
  dialogVisible.value = false; loadData()
}

async function handleComplete(row: any) {
  const { value } = await ElMessageBox.prompt('请输入处理结果（可选）', '完成工单', { inputType: 'textarea', inputValue: '' })
  await completeMaintenanceOrder(row.id, { resolution: value || undefined })
  ElMessage.success('工单已完成'); loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定删除该工单？', '警告', { type: 'warning' })
  await deleteMaintenanceOrder(id); ElMessage.success('已删除'); loadData()
}

onMounted(() => { loadData(); loadEquipment(); loadUsers() })
</script>
