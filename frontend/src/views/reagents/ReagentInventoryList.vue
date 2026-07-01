<template>
  <div class="page-container">
    <div class="page-header">
      <h2>库存总览</h2>
      <div style="display: flex; gap: 8px;">
        <el-button type="primary" @click="$router.push('/reagents/transaction')">出入库操作</el-button>
      </div>
    </div>

    <div class="stat-cards" v-if="stats">
      <div class="stat-card">
        <div class="stat-value" style="color: var(--color-success);">{{ stats.normal }}</div>
        <div class="stat-label">正常</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: var(--color-warning);">{{ stats.warning_days }}</div>
        <div class="stat-label">临期提醒（≤90 天）</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: var(--color-danger);">{{ stats.warning_urgent }}</div>
        <div class="stat-label">临期警告（≤30 天）</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #9b59b6;">{{ stats.expired }}</div>
        <div class="stat-label">已过期</div>
      </div>
    </div>

    <div style="display: flex; gap: 8px; margin-bottom: 16px;">
      <el-button size="small" :type="filters.alertLevel === '' ? 'primary' : 'default'" @click="filters.alertLevel = ''; loadData()">全部</el-button>
      <el-button size="small" :type="filters.alertLevel === 'warning_days' ? 'warning' : 'default'" @click="filters.alertLevel = 'warning_days'; loadData()">临期提醒</el-button>
      <el-button size="small" :type="filters.alertLevel === 'warning_urgent' ? 'danger' : 'default'" @click="filters.alertLevel = 'warning_urgent'; loadData()">临期警告</el-button>
      <el-button size="small" :type="filters.alertLevel === 'expired' ? 'danger' : 'default'" @click="filters.alertLevel = 'expired'; loadData()">已过期</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="filters.keyword" placeholder="搜索试剂名称、条码或批次号..." clearable style="width: 300px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="filters.status" placeholder="状态" clearable style="width: 130px" @change="loadData">
        <el-option label="未开封" value="unopened" />
        <el-option label="已开封" value="opened" />
        <el-option label="已过期" value="expired" />
        <el-option label="已废弃" value="disposed" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="categoryName" label="名称" min-width="150" />
      <el-table-column prop="barcode" label="条码" width="110" />
      <el-table-column prop="batchNo" label="批次号" width="130" />
      <el-table-column prop="remainingQuantity" label="余量" width="90">
        <template #default="{ row }">
          <span :style="{ color: row.remainingQuantity < 1 ? 'var(--color-danger)' : '' }">
            {{ row.remainingQuantity }} {{ row.unit }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="expiryDate" label="有效期" width="110" />
      <el-table-column prop="alertLevel" label="预警" width="90">
        <template #default="{ row }">
          <el-tag :type="alertType(row.alertLevel)" size="small">{{ alertLabel(row.alertLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="locationPath" label="位置" min-width="160">
        <template #default="{ row }">
          <span style="color: var(--color-ink-muted); font-size: 14px;">{{ row.locationPath || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="supplier" label="供应商" width="120" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button v-if="row.status !== 'disposed' && row.status !== 'expired'" text type="primary" @click="showUse(row)">出库</el-button>
            <el-button v-if="row.alertLevel === 'warning_urgent' || row.alertLevel === 'warning_days'" text type="primary" style="color: var(--color-warning);" @click="handleMarkExpired(row)">标过期</el-button>
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

    <!-- 出库对话框 -->
    <el-dialog v-model="useDialogVisible" title="出库" width="400px">
      <el-form :model="useForm" label-width="80px">
        <el-form-item label="试剂">
          <span style="font-weight: 600;">{{ currentReagent?.categoryName }}</span>
        </el-form-item>
        <el-form-item label="条码">
          <span>{{ currentReagent?.barcode }}</span>
        </el-form-item>
        <el-form-item label="当前余量">
          <span>{{ currentReagent?.remainingQuantity }} {{ currentReagent?.unit }}</span>
        </el-form-item>
        <el-form-item label="取用量" required>
          <el-input-number v-model="useForm.quantity" :min="0.1" :max="currentReagent?.remainingQuantity" :step="0.5" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="useForm.purpose" type="textarea" :rows="2" placeholder="如：色谱柱活化" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="useDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUse">确认出库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getInventoryList, getExpiringList, useReagent, updateInventory } from '@/api/reagent'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const useDialogVisible = ref(false)
const currentReagent = ref<any>(null)

const filters = reactive({ keyword: '', status: '', alertLevel: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const useForm = reactive({ quantity: 1, purpose: '' })

const stats = ref<{ normal: number; warning_days: number; warning_urgent: number; expired: number } | null>(null)

async function loadStats() {
  try {
    const res: any = await getExpiringList()
    const counts = { normal: 0, warning_days: 0, warning_urgent: 0, expired: 0 }
    for (const r of (res.data || [])) {
      const key = r.alertLevel as keyof typeof counts
      if (counts[key] !== undefined) counts[key]++
    }
    stats.value = counts
  } catch { /* ignore */ }
}

function alertType(s: string) {
  return { normal: 'success', warning_days: 'warning', warning_urgent: 'danger', expired: 'danger' }[s] || 'info'
}

function alertLabel(s: string) {
  return { normal: '正常', warning_days: '临期', warning_urgent: '紧急', expired: '过期' }[s] || s
}

function statusType(s: string) {
  return { unopened: 'info', opened: 'primary', expired: 'danger', disposed: 'info' }[s] || 'info'
}

function statusLabel(s: string) {
  return { unopened: '未开封', opened: '已开封', expired: '已过期', disposed: '已废弃' }[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getInventoryList({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      alertLevel: filters.alertLevel || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showUse(row: any) {
  currentReagent.value = row
  useForm.quantity = row.remainingQuantity > 1 ? 1 : row.remainingQuantity
  useForm.purpose = ''
  useDialogVisible.value = true
}

async function handleUse() {
  if (!currentReagent.value) return
  await useReagent(currentReagent.value.id, {
    quantity: useForm.quantity,
    purpose: useForm.purpose || undefined,
  })
  ElMessage.success('出库成功')
  useDialogVisible.value = false
  loadData()
}

async function handleMarkExpired(row: any) {
  await ElMessageBox.confirm(`确定将 "${row.categoryName}" (${row.barcode}) 标记为过期吗？`, '确认', { type: 'warning' })
  await updateInventory(row.id, { status: 'expired' })
  ElMessage.success('已标记过期')
  loadData()
}

onMounted(() => { loadData(); loadStats() })
</script>
