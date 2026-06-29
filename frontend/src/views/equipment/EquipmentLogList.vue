<template>
  <div class="page-container">
    <div class="page-header">
      <h2>设备日志</h2>
      <el-button type="primary" @click="showAdd">记录日志</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filters.equipmentId" placeholder="选择设备" filterable clearable style="width: 200px" @change="loadData">
        <el-option v-for="e in equipmentOptions" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-select v-model="filters.logType" placeholder="日志类型" clearable style="width: 130px" @change="loadData">
        <el-option label="使用" value="usage" />
        <el-option label="异常" value="anomaly" />
        <el-option label="维护" value="maintenance" />
      </el-select>
      <el-button @click="loadData">查询</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="equipmentName" label="设备" min-width="140" />
      <el-table-column label="类型" width="70">
        <template #default="{ row }">
          <el-tag :type="logTypeColor(row.logType)" size="small">
            {{ logTypeLabel(row.logType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
      <el-table-column prop="recordedByName" label="记录人" width="100" />
      <el-table-column prop="createdAt" label="时间" width="160">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ').slice(0, 16) }}</template>
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

    <el-dialog v-model="dialogVisible" title="记录日志" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="设备" required>
          <el-select v-model="form.equipmentId" filterable placeholder="选择设备" style="width: 100%">
            <el-option v-for="e in equipmentOptions" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日志类型" required>
          <el-select v-model="form.logType" style="width: 100%">
            <el-option label="使用" value="usage" />
            <el-option label="异常" value="anomaly" />
            <el-option label="维护" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" required>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述设备使用情况或异常现象" />
        </el-form-item>
        <el-form-item label="异常标记">
          <el-switch v-model="form.anomalyFlag" active-text="标记为异常" />
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
import { getEquipmentLogs, createEquipmentLog, getEquipmentList } from '@/api/equipment'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const equipmentOptions = ref<any[]>([])
const dialogVisible = ref(false)

const filters = reactive({ equipmentId: '', logType: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({ equipmentId: '', logType: 'usage', description: '', anomalyFlag: false })

const logTypeColorMap: Record<string, string> = { usage: 'primary', anomaly: 'danger', maintenance: 'success' }
const logTypeLabelMap: Record<string, string> = { usage: '使用', anomaly: '异常', maintenance: '维护' }
function logTypeColor(s: string) { return logTypeColorMap[s] || 'info' }
function logTypeLabel(s: string) { return logTypeLabelMap[s] || s }

async function loadEquipment() {
  try { const r: any = await getEquipmentList({ page: 0, size: 100 }); equipmentOptions.value = r.data?.content || [] } catch { /* ignore */ }
}
async function loadData() {
  loading.value = true
  try {
    const r: any = await getEquipmentLogs({
      equipmentId: filters.equipmentId || undefined, logType: filters.logType || undefined,
      page: pagination.page, size: pagination.size,
    })
    tableData.value = r.data?.content || []; pagination.total = r.data?.totalElements || 0
  } finally { loading.value = false }
}

function showAdd() {
  Object.assign(form, { equipmentId: '', logType: 'usage', description: '', anomalyFlag: false })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.equipmentId || !form.description) { ElMessage.warning('请填写设备和描述'); return }
  try {
    await createEquipmentLog(form)
    ElMessage.success('日志已记录'); dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '记录失败') }
}

onMounted(() => { loadData(); loadEquipment() })
</script>
