<template>
  <div class="page-container">
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalEquipment }}</div>
        <div class="stat-label">设备总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.activeEquipment }}</div>
        <div class="stat-label">使用中</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalProjects }}</div>
        <div class="stat-label">项目总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalDataRecords }}</div>
        <div class="stat-label">数据记录</div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-title">设备利用率</div>
        <v-chart :option="utilizationOption" style="height: 300px" autoresize />
      </div>
      <div class="chart-card">
        <div class="chart-title">使用趋势（近30天）</div>
        <v-chart :option="trendOption" style="height: 300px" autoresize />
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-title">项目数据分布</div>
        <v-chart :option="distributionOption" style="height: 300px" autoresize />
      </div>
      <div class="chart-card">
        <div class="chart-title">最近活动</div>
        <el-timeline>
          <el-timeline-item
            v-for="item in recentActivity"
            :key="item.timestamp"
            :timestamp="item.timestamp"
            placement="top"
          >
            <div class="activity-item">
              <el-tag :type="item.type === 'usage' ? 'primary' : 'success'" size="small">
                {{ item.type === 'usage' ? '使用' : '数据' }}
              </el-tag>
              <span class="activity-user">{{ item.userName }}</span>
              <span class="activity-desc">{{ item.description }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import {
  getDashboardOverview,
  getEquipmentUtilization,
  getUsageTrend,
  getProjectDataDistribution,
  getRecentActivity,
} from '@/api/dashboard'

use([CanvasRenderer, BarChart, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const overview = ref({
  totalEquipment: 0,
  activeEquipment: 0,
  totalProjects: 0,
  totalDataRecords: 0,
  totalUsageHoursThisMonth: 0,
})

const utilizationData = ref<any[]>([])
const trendData = ref<any[]>([])
const distributionData = ref<any[]>([])
const recentActivity = ref<any[]>([])

const utilizationOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: utilizationData.value.map((d: any) => d.equipmentName),
    axisLabel: { rotate: 30 },
  },
  yAxis: { type: 'value', name: '小时' },
  series: [{
    type: 'bar',
    data: utilizationData.value.map((d: any) => d.usageHours),
    itemStyle: { color: '#0075de' },
  }],
}))

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: trendData.value.map((d: any) => d.date),
  },
  yAxis: { type: 'value', name: '小时' },
  series: [{
    type: 'line',
    data: trendData.value.map((d: any) => d.totalHours),
    smooth: true,
    areaStyle: { opacity: 0.3 },
    itemStyle: { color: '#0075de' },
  }],
}))

const distributionOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: distributionData.value.map((d: any) => ({
      name: d.projectName,
      value: d.dataCount,
    })),
  }],
}))

onMounted(async () => {
  try {
    const [overviewRes, utilRes, trendRes, distRes, activityRes] = await Promise.all([
      getDashboardOverview() as any,
      getEquipmentUtilization() as any,
      getUsageTrend() as any,
      getProjectDataDistribution() as any,
      getRecentActivity() as any,
    ])
    overview.value = overviewRes.data
    utilizationData.value = utilRes.data || []
    trendData.value = trendRes.data || []
    distributionData.value = distRes.data || []
    recentActivity.value = activityRes.data || []
  } catch (e) {
    // Dashboard data will be empty on first load
  }
})
</script>

<style scoped lang="scss">
.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;

  .activity-user {
    font-weight: 600;
    color: var(--color-ink);
  }

  .activity-desc {
    color: var(--color-ink-muted);
  }
}
</style>
