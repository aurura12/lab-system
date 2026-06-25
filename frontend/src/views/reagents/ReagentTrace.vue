<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂追溯查询</h2>
    </div>

    <div style="margin-bottom: 24px;">
      <el-input
        v-model="barcode"
        placeholder="输入条码或批次号查询..."
        size="large"
        clearable
        style="max-width: 500px;"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch" :loading="loading">查询</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="empty-state">查询中...</div>

    <template v-else-if="inventory">
      <!-- 试剂基本信息 -->
      <div class="feature-card" style="margin-bottom: 20px;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">基本信息</div>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="试剂名称">{{ inventory.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="条码">{{ inventory.barcode }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ inventory.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前余量">
            <span :style="{ color: inventory.remainingQuantity <= 0 ? 'var(--color-danger)' : 'var(--color-success)', fontWeight: 600 }">
              {{ inventory.remainingQuantity }} {{ inventory.unit }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="有效期">{{ inventory.expiryDate }}</el-descriptions-item>
          <el-descriptions-item label="预警">
            <el-tag :type="alertType(inventory.alertLevel)" size="small">{{ alertLabel(inventory.alertLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(inventory.status)" size="small">{{ statusLabel(inventory.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="供应商">{{ inventory.supplier || '-' }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ inventory.locationPath || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 全生命周期时间线 -->
      <div class="feature-card">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">
          全生命周期
          <span style="font-size: 14px; font-weight: 400; color: var(--color-ink-muted); margin-left: 8px;">
            共 {{ transactions.length }} 条记录
          </span>
        </div>

        <el-timeline v-if="transactions.length > 0">
          <el-timeline-item
            v-for="t in transactions"
            :key="t.id"
            :timestamp="t.createdAt"
            :type="t.type === 'inbound' ? 'success' : 'primary'"
            placement="top"
            size="large"
          >
            <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
              <el-tag :type="t.type === 'inbound' ? 'success' : 'primary'" size="small">
                {{ t.type === 'inbound' ? '入库' : '出库' }}
              </el-tag>
              <span style="font-weight: 600;">
                {{ t.type === 'inbound' ? `入库 ${t.quantity} ${inventory.unit}` : `出库 ${t.quantity} ${inventory.unit}` }}
              </span>
            </div>
            <div style="font-size: 14px; color: var(--color-ink-secondary);">
              <span v-if="t.operatorName">操作人：{{ t.operatorName }}</span>
              <span v-if="t.purpose" style="margin-left: 16px;">用途：{{ t.purpose }}</span>
              <span v-if="t.projectName" style="margin-left: 16px;">项目：{{ t.projectName }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>

        <div v-else class="empty-state">
          暂无交易记录
        </div>
      </div>
    </template>

    <div v-else-if="searched && !inventory" class="empty-state">
      未找到该条码对应的试剂
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getInventoryList, getTransactionList } from '@/api/reagent'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const barcode = ref('')
const loading = ref(false)
const searched = ref(false)
const inventory = ref<any>(null)
const transactions = ref<any[]>([])

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

async function handleSearch() {
  const code = barcode.value.trim()
  if (!code) return

  loading.value = true
  searched.value = true
  inventory.value = null
  transactions.value = []

  try {
    // 查询库存信息
    const invRes: any = await getInventoryList({ keyword: code, size: 1 })
    const list = invRes.data?.content || []
    if (list.length === 0) {
      ElMessage.info('未找到该条码对应的试剂')
      return
    }
    inventory.value = list[0]

    // 查询全生命周期交易记录
    const txRes: any = await getTransactionList({ inventoryId: list[0].id, size: 200 })
    transactions.value = txRes.data?.content || []
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '查询失败')
  } finally {
    loading.value = false
  }
}
</script>
