<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂位置查询</h2>
    </div>

    <div style="margin-bottom: 24px;">
      <el-input
        v-model="keyword"
        placeholder="输入试剂名称、CAS 号或条码搜索..."
        size="large"
        clearable
        style="max-width: 500px;"
        @keyup.enter="handleSearch"
        @clear="clearResults"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="empty-state">搜索中...</div>

    <div v-else-if="keyword && results.length === 0" class="empty-state">
      未找到匹配的试剂
    </div>

    <div v-else-if="results.length > 0" class="feature-card">
      <div style="font-size: 14px; color: var(--color-ink-muted); margin-bottom: 16px;">
        共找到 {{ results.length }} 条结果
      </div>
      <el-table :data="results" stripe>
        <el-table-column prop="categoryName" label="名称" min-width="160" />
        <el-table-column prop="barcode" label="条码" width="110" />
        <el-table-column prop="remainingQuantity" label="余量" width="90">
          <template #default="{ row }">
            {{ row.remainingQuantity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="有效期" width="110" />
        <el-table-column prop="alertLevel" label="预警" width="80">
          <template #default="{ row }">
            <el-tag :type="alertType(row.alertLevel)" size="small">{{ alertLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="位置" min-width="220">
          <template #default="{ row }">
            <div v-if="row.locationPath" style="display: flex; align-items: center; gap: 6px;">
              <el-icon style="color: var(--color-primary);"><Location /></el-icon>
              <span style="font-weight: 500;">{{ row.locationPath }}</span>
            </div>
            <span v-else style="color: var(--color-ink-faint);">未指定位置</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { searchReagentLocation } from '@/api/reagent'
import { Search, Location } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const results = ref<any[]>([])
const loading = ref(false)

function alertType(s: string) {
  return { normal: 'success', warning_days: 'warning', warning_urgent: 'danger', expired: 'danger' }[s] || 'info'
}

function alertLabel(s: string) {
  return { normal: '正常', warning_days: '临期', warning_urgent: '紧急', expired: '过期' }[s] || s
}

async function handleSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  try {
    const res: any = await searchReagentLocation(keyword.value.trim())
    results.value = res.data || []
    if (results.value.length === 0) {
      ElMessage.info('未找到匹配的试剂')
    }
  } catch {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

function clearResults() {
  results.value = []
}
</script>
