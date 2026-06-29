<template>
  <div class="page-container">
    <div class="page-header">
      <h2>新建预约</h2>
    </div>

    <div class="feature-card" style="max-width: 600px;">
      <el-form :model="form" label-width="100px">
        <el-form-item label="设备" required>
          <el-select v-model="form.equipmentId" filterable placeholder="选择设备" style="width: 100%">
            <el-option v-for="e in equipmentList" :key="e.id" :label="e.name + ' (' + e.model + ')'" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择日期和时间"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" :disabled-date="disablePastDate" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择日期和时间"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" :disabled-date="disablePastDate" />
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="使用目的（如：材料样品测试）" />
        </el-form-item>
        <el-form-item label="实验类型">
          <el-input v-model="form.experimentType" placeholder="如：光谱分析、离心分离" />
        </el-form-item>
        <el-form-item label="关联项目">
          <el-select v-model="form.projectId" filterable clearable placeholder="可选" style="width: 100%">
            <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleCreate" :loading="creating">提交预约</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { createBooking, getEquipmentList } from '@/api/equipment'
import { getProjects } from '@/api/project'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const creating = ref(false)
const equipmentList = ref<any[]>([])
const projectList = ref<any[]>([])

const form = reactive({
  equipmentId: '', startTime: '', endTime: '', purpose: '', experimentType: '', projectId: '',
})

function disablePastDate(date: Date) {
  return date.getTime() < Date.now() - 86400000
}

async function loadEquipment() {
  try {
    const res: any = await getEquipmentList({ page: 0, size: 100 })
    equipmentList.value = res.data?.content || []
  } catch { /* ignore */ }
}

async function loadProjects() {
  try {
    const res: any = await getProjects({ page: 0, size: 100 })
    projectList.value = res.data?.content || []
  } catch { /* ignore */ }
}

async function handleCreate() {
  if (!form.equipmentId || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写设备和时间')
    return
  }
  if (form.startTime >= form.endTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  creating.value = true
  try {
    await createBooking({
      equipmentId: form.equipmentId,
      startTime: form.startTime,
      endTime: form.endTime,
      purpose: form.purpose || undefined,
      experimentType: form.experimentType || undefined,
      projectId: form.projectId || undefined,
    })
    ElMessage.success('预约已提交，等待审批')
    router.push('/equipment/bookings')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

onMounted(() => { loadEquipment(); loadProjects() })
</script>
