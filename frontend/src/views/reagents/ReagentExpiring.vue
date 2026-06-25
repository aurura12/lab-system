<template>
  <div class="page-container">
    <div class="page-header">
      <h2>临期预警看板</h2>
      <el-button @click="loadData" :loading="loading">刷新</el-button>
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
      <el-button size="small" :type="alertFilter === '' ? 'primary' : 'default'" @click="alertFilter = ''">全部</el-button>
      <el-button size="small" :type="alertFilter === 'warning_days' ? 'warning' : 'default'" @click="alertFilter = 'warning_days'">临期提醒</el-button>
      <el-button size="small" :type="alertFilter === 'warning_urgent' ? 'danger' : 'default'" @click="alertFilter = 'warning_urgent'">临期警告</el-button>
      <el-button size="small" :type="alertFilter === 'expired' ? 'danger' : 'default'" @click="alertFilter = 'expired'">已过期</el-button>
    </div>

    <el-table :data="filteredData" stripe v-loading="loading">
      <el-table-column prop="categoryName" label="名称" min-width="160" />
      <el-table-column prop="barcode" label="条码" width="110" />
      <el-table-column prop="batchNo" label="批次号" width="130" />
      <el-table-column prop="remainingQuantity" label="余量" width="90">
        <template #default="{ row }">
          {{ row.remainingQuantity }} {{ row.unit }}
        </template>
      </el-table-column>
      <el-table-column prop="expiryDate" label="有效期" width="110">
        <template #default="{ row }">
          <span :style="{ fontWeight: 600, color: alertType(row.alertLevel) === 'danger' ? 'var(--color-danger)' : 'var(--color-warning)' }">
            {{ row.expiryDate }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="alertLevel" label="预警" width="90">
        <template #default="{ row }">
          <el-tag :type="alertType(row.alertLevel)" size="small">{{ alertLabel(row.alertLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          {{ statusLabel(row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="locationPath" label="位置" min-width="160" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status !== 'expired' && row.status !== 'disposed'" text type="primary" size="small" @click="$router.push('/reagents/transaction')">出库</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && tableData.length === 0" class="empty-state">
      暂无临期或过期的试剂
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getExpiringList } from '@/api/reagent'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const alertFilter = ref('')

const stats = computed(() => {
  const counts = { normal: 0, warning_days: 0, warning_urgent: 0, expired: 0 }
  tableData.value.forEach((r: any) => {
    const key = r.alertLevel as keyof typeof counts
    if (counts[key] !== undefined) counts[key]++
  })
  return counts
})

const filteredData = computed(() => {
  if (!alertFilter.value) return tableData.value
  return tableData.value.filter((r: any) => r.alertLevel === alertFilter.value)
})

function alertType(s: string) {
  return { normal: 'success', warning_days: 'warning', warning_urgent: 'danger', expired: 'danger' }[s] || 'info'
}

function alertLabel(s: string) {
  return { normal: '正常', warning_days: '临期提醒', warning_urgent: '临期警告', expired: '已过期' }[s] || s
}

const statusMap: Record<string, string> = { unopened: '未开封', opened: '已开封' }
function statusLabel(s: string) {
  return statusMap[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getExpiringList()
    tableData.value = res.data || []
  } catch (e: any) {
    if (e?.response?.status !== 401) {
      ElMessage.error('加载预警数据失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
