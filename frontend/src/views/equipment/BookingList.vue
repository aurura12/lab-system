<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备预约</h2>
      <el-button type="primary" @click="$router.push('/equipment/bookings/new')">新建预约</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filters.equipmentId" placeholder="选择设备" filterable clearable style="width: 200px" @change="loadData">
        <el-option v-for="e in equipmentOptions" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px" @change="loadData">
        <el-option label="待审批" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已拒绝" value="rejected" />
        <el-option label="已完成" value="completed" />
        <el-option label="已签到" value="checked_in" />
        <el-option label="爽约" value="no_show" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="equipmentName" label="设备" min-width="130" />
      <el-table-column prop="userRealName" label="预约人" width="100" />
      <el-table-column label="时段" min-width="240">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="purpose" label="用途" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <div class="action-cell" style="flex-direction: row; gap: 4px;">
            <el-button v-if="row.status === 'approved'" text type="success" size="small" @click="handleCheckin(row)">签到</el-button>
            <el-button v-if="row.status === 'pending' && isMine(row)" text type="primary" size="small" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="row.status === 'pending' && isAdmin" text type="warning" size="small" @click="showApprove(row)">审批</el-button>
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

    <el-dialog v-model="approveDialogVisible" title="预约审批" width="450px">
      <div v-if="currentBooking" style="margin-bottom: 16px;">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="设备">{{ currentBooking.equipmentName }}</el-descriptions-item>
          <el-descriptions-item label="预约人">{{ currentBooking.userRealName }}</el-descriptions-item>
          <el-descriptions-item label="时段">{{ formatTime(currentBooking.startTime) }} ~ {{ formatTime(currentBooking.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="用途">{{ currentBooking.purpose }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div style="display: flex; gap: 12px; margin-bottom: 12px;">
        <el-radio-group v-model="approveAction">
          <el-radio-button value="approve">通过</el-radio-button>
          <el-radio-button value="reject">拒绝</el-radio-button>
        </el-radio-group>
      </div>
      <el-input v-model="approveNote" type="textarea" placeholder="审批备注（可选）" :rows="2" />
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getEquipmentBookings, getMyBookings, cancelBooking, checkinBooking, approveBooking, getEquipmentList } from '@/api/equipment'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const equipmentOptions = ref<any[]>([])
const approveDialogVisible = ref(false)
const currentBooking = ref<any>(null)
const approveAction = ref('approve')
const approveNote = ref('')
const isAdmin = ref(authStore.user?.role === 'admin' || authStore.user?.role === 'lab_manager')

const filters = reactive({ equipmentId: '', status: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })

function statusType(s: string) {
  return { pending: 'info', approved: 'success', rejected: 'danger', checked_in: 'primary', no_show: 'warning', completed: '', cancelled: 'info' }[s] || 'info'
}
function statusLabel(s: string) {
  return { pending: '待审批', approved: '已通过', rejected: '已拒绝', checked_in: '已签到', no_show: '爽约', completed: '已完成', cancelled: '已取消' }[s] || s
}

function formatTime(t: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

function isMine(row: any) {
  return row.userId === authStore.user?.id
}

async function loadEquipment() {
  try {
    const res: any = await getEquipmentList({ page: 0, size: 100 })
    equipmentOptions.value = res.data?.content || []
  } catch { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getEquipmentBookings({
      equipmentId: filters.equipmentId || undefined,
      status: filters.status || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data?.content || []
    pagination.total = res.data?.totalElements || 0
  } finally {
    loading.value = false
  }
}

async function handleCheckin(row: any) {
  await ElMessageBox.confirm(`确认 "${row.userRealName}" 在 "${row.equipmentName}" 上签到？`, '签到确认', { type: 'info' })
  await checkinBooking(row.id)
  ElMessage.success('签到成功')
  loadData()
}

async function handleCancel(row: any) {
  await ElMessageBox.confirm('确定取消该预约？', '取消确认', { type: 'warning' })
  await cancelBooking(row.id)
  ElMessage.success('已取消')
  loadData()
}

function showApprove(row: any) {
  currentBooking.value = row
  approveAction.value = 'approve'
  approveNote.value = ''
  approveDialogVisible.value = true
}

async function handleApprove() {
  if (!currentBooking.value) return
  try {
    await approveBooking(currentBooking.value.id, {
      approved: approveAction.value === 'approve',
      note: approveNote.value || undefined,
    })
    ElMessage.success(approveAction.value === 'approve' ? '已通过' : '已拒绝')
    approveDialogVisible.value = false
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '审批失败')
  }
}

onMounted(() => { loadData(); loadEquipment() })
</script>
