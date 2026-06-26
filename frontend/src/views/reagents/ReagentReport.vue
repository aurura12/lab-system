<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂使用报表</h2>
      <el-button type="primary" @click="exportExcel">导出 Excel</el-button>
    </div>

    <div class="search-bar">
      <el-date-picker v-model="startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width: 160px;" />
      <span style="color: var(--color-ink-muted);">至</span>
      <el-date-picker v-model="endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 160px;" />
      <el-button type="primary" @click="loadData" :loading="loading">查询</el-button>
    </div>

    <template v-if="report">
      <!-- 月度趋势图 -->
      <div class="chart-row">
        <div class="chart-card">
          <div class="chart-title">月度出入库趋势</div>
          <v-chart v-if="monthlyChartData" :option="monthlyChartData" style="height: 300px;" autoresize />
        </div>
        <div class="chart-card">
          <div class="chart-title">品类用量分布</div>
          <v-chart v-if="categoryChartData" :option="categoryChartData" style="height: 300px;" autoresize />
        </div>
      </div>

      <!-- 按月明细 -->
      <div class="feature-card" style="margin-bottom: 20px;">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">月度明细</div>
        <el-table :data="report.monthly" stripe size="small">
          <el-table-column label="年份" width="80">
            <template #default="{ row }">{{ row.year }}</template>
          </el-table-column>
          <el-table-column label="月份" width="80">
            <template #default="{ row }">{{ row.month }} 月</template>
          </el-table-column>
          <el-table-column label="入库次数" prop="inboundCount" width="100" />
          <el-table-column label="入库总量" width="120">
            <template #default="{ row }">{{ row.inboundTotal || 0 }}</template>
          </el-table-column>
          <el-table-column label="出库次数" prop="outboundCount" width="100" />
          <el-table-column label="出库总量" width="120">
            <template #default="{ row }">{{ row.outboundTotal || 0 }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 按品类 -->
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
        <div class="feature-card">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">品类用量排行</div>
          <el-table :data="report.byCategory" stripe size="small">
            <el-table-column label="品类" prop="categoryName" min-width="140" />
            <el-table-column label="出库次数" prop="outboundCount" width="90" />
            <el-table-column label="出库总量" width="120">
              <template #default="{ row }">{{ row.outboundTotal }} {{ row.unit }}</template>
            </el-table-column>
          </el-table>
          <div v-if="report.byCategory.length === 0" class="empty-state" style="padding: 20px;">暂无出库数据</div>
        </div>
        <div class="feature-card">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">项目用量排行</div>
          <el-table :data="report.byProject" stripe size="small">
            <el-table-column label="项目" prop="projectName" min-width="140" />
            <el-table-column label="出库次数" prop="outboundCount" width="90" />
            <el-table-column label="出库总量" width="120">
              <template #default="{ row }">{{ row.outboundTotal }}</template>
            </el-table-column>
          </el-table>
          <div v-if="report.byProject.length === 0" class="empty-state" style="padding: 20px;">暂无出库数据</div>
        </div>
      </div>
    </template>

    <div v-else-if="!loading" class="empty-state">
      选择日期范围后点击查询
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getReagentReport } from '@/api/reagent'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const startDate = ref('')
const endDate = ref('')
const report = ref<any>(null)

function setDefaultDateRange() {
  const end = new Date()
  const start = new Date(end)
  start.setFullYear(start.getFullYear() - 1)
  startDate.value = start.toISOString().slice(0, 10)
  endDate.value = end.toISOString().slice(0, 10)
}

const monthlyChartData = computed(() => {
  if (!report.value?.monthly?.length) return null
  const months = report.value.monthly.map((m: any) => `${m.year}-${String(m.month).padStart(2, '0')}`)
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库量', '出库量'] },
    grid: { left: 50, right: 20, bottom: 30, top: 40 },
    xAxis: { type: 'category', data: months, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value' },
    series: [
      { name: '入库量', type: 'bar', data: report.value.monthly.map((m: any) => m.inboundTotal || 0), itemStyle: { color: '#1aae39' } },
      { name: '出库量', type: 'bar', data: report.value.monthly.map((m: any) => m.outboundTotal || 0), itemStyle: { color: '#0075de' } },
    ],
  }
})

const categoryChartData = computed(() => {
  if (!report.value?.byCategory?.length) return null
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    series: [{
      type: 'pie',
      radius: ['30%', '60%'],
      data: report.value.byCategory.map((c: any) => ({ name: c.categoryName, value: c.outboundTotal })),
      label: { fontSize: 12 },
    }],
  }
})

async function loadData() {
  loading.value = true
  try {
    const res: any = await getReagentReport({
      startDate: startDate.value || undefined,
      endDate: endDate.value || undefined,
    })
    report.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载报表失败')
  } finally {
    loading.value = false
  }
}

function exportExcel() {
  if (!report.value) { ElMessage.warning('请先查询数据'); return }

  const wb = XLSX.utils.book_new()

  // 月度 sheet
  const monthlyData = report.value.monthly.map((m: any) => ({
    '年份': m.year, '月份': m.month + '月',
    '入库次数': m.inboundCount, '入库总量': m.inboundTotal || 0,
    '出库次数': m.outboundCount, '出库总量': m.outboundTotal || 0,
  }))
  const ws1 = XLSX.utils.json_to_sheet(monthlyData)
  XLSX.utils.book_append_sheet(wb, ws1, '月度报表')

  // 品类 sheet
  if (report.value.byCategory?.length) {
    const catData = report.value.byCategory.map((c: any) => ({
      '品类': c.categoryName, '出库次数': c.outboundCount,
      '出库总量': c.outboundTotal, '单位': c.unit,
    }))
    const ws2 = XLSX.utils.json_to_sheet(catData)
    XLSX.utils.book_append_sheet(wb, ws2, '品类排行')
  }

  // 项目 sheet
  if (report.value.byProject?.length) {
    const projData = report.value.byProject.map((p: any) => ({
      '项目': p.projectName, '出库次数': p.outboundCount, '出库总量': p.outboundTotal,
    }))
    const ws3 = XLSX.utils.json_to_sheet(projData)
    XLSX.utils.book_append_sheet(wb, ws3, '项目排行')
  }

  XLSX.writeFile(wb, `试剂报表_${new Date().toISOString().slice(0, 10)}.xlsx`)
  ElMessage.success('报表已导出')
}

onMounted(() => {
  setDefaultDateRange()
  loadData()
})
</script>
