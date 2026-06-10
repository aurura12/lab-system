<template>
  <div class="page-container">
    <el-page-header @back="router.back()" title="返回" :content="equipment.name || '设备详情'" />

    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="16">
        <el-card>
          <template #header>基本信息</template>
          <el-descriptions :column="2">
            <el-descriptions-item label="名称">{{ equipment.name }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ equipment.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="序列号">{{ equipment.serialNumber || '-' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ categoryLabel(equipment.category) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusType(equipment.status)" size="small">{{ { available: '可用', in_use: '使用中', maintenance: '维护中', retired: '已退役' }[equipment.status] || equipment.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="制造商">{{ equipment.manufacturer || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房间">{{ equipment.roomName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="购买日期">{{ equipment.purchaseDate || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" :disabled="equipment.status !== 'available'" @click="handleStartUsage" style="width:100%">
              登录设备
            </el-button>
            <el-button type="warning" :disabled="equipment.status !== 'in_use'" @click="handleEndUsage" style="width:100%">
              登出设备
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>使用历史</template>
      <el-table :data="usageRecords" stripe>
        <el-table-column prop="userName" label="使用人" width="120" />
        <el-table-column prop="projectName" label="项目" width="160" />
        <el-table-column label="登录时间" width="180">
          <template #default="{ row }">{{ row.loginTime ? row.loginTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
        </el-table-column>
        <el-table-column label="登出时间" width="180">
          <template #default="{ row }">{{ row.logoutTime ? row.logoutTime.replace('T', ' ').substring(0, 19) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'primary' : 'success'" size="small">{{ { active: '使用中', completed: '已完成' }[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purpose" label="用途" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEquipmentById } from '@/api/equipment'
import { startUsage, endUsage, getUsageRecords } from '@/api/usage'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const equipment = ref<any>({})
const usageRecords = ref<any[]>([])

function statusType(s: string) {
  return { available: 'success', in_use: 'primary', maintenance: 'warning', retired: 'info' }[s] || 'info'
}

function categoryLabel(c: string) {
  return { spectrometer: '光谱仪', microscope: '显微镜', centrifuge: '离心机', analyzer: '分析仪', other: '其他' }[c] || c || '-'
}

async function loadData() {
  const res: any = await getEquipmentById(id)
  equipment.value = res.data
  const usageRes: any = await getUsageRecords({ equipmentId: id, size: 50 })
  usageRecords.value = usageRes.data?.content || []
}

async function handleStartUsage() {
  await startUsage({ equipmentId: id })
  ElMessage.success('已登录设备')
  loadData()
}

async function handleEndUsage() {
  const activeRecord = usageRecords.value.find((r: any) => r.status === 'active')
  if (activeRecord) {
    await endUsage(activeRecord.id)
    ElMessage.success('已登出设备')
    loadData()
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-actions .el-button {
  margin-left: 0 !important;
  width: 100%;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px !important;
}

:deep(.el-descriptions__label) {
  color: var(--color-ink-muted);
  font-weight: 500;
}
</style>
