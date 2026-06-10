<template>
  <div class="page-container">
    <div class="page-header">
      <h2>使用记录</h2>
    </div>

    <div class="search-bar">
      <el-date-picker v-model="dateRange" type="daterange" range-separator="~" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 280px" @change="loadData" />
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="equipmentName" label="设备" min-width="150" />
      <el-table-column prop="userName" label="使用人" width="120" />
      <el-table-column prop="projectName" label="项目" width="160" />
      <el-table-column label="登录时间" width="180">
        <template #default="{ row }">{{ row.loginTime ? row.loginTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
      </el-table-column>
      <el-table-column label="登出时间" width="180">
        <template #default="{ row }">
          {{ row.logoutTime ? row.logoutTime.replace('T', ' ').substring(0, 19) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="durationMinutes" label="时长(分钟)" width="120">
        <template #default="{ row }">
          {{ row.durationMinutes != null ? row.durationMinutes + ' 分钟' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'primary' : 'success'" size="small">{{ { active: '使用中', completed: '已完成' }[row.status] || row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="purpose" label="用途" min-width="150" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUsageRecords } from '@/api/usage'

const loading = ref(false)
const tableData = ref<any[]>([])
const dateRange = ref<any>(null)
const pagination = reactive({ page: 0, size: 10, total: 0 })

async function loadData() {
  loading.value = true
  try {
    const params: any = { page: pagination.page, size: pagination.size }
    if (dateRange.value) {
      params.startTime = dateRange.value[0] + 'T00:00:00'
      params.endTime = dateRange.value[1] + 'T23:59:59'
    }
    const res: any = await getUsageRecords(params)
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
