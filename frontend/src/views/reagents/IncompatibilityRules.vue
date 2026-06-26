<template>
  <div class="page-container">
    <div class="page-header">
      <h2>禁忌规则管理</h2>
      <el-button type="primary" @click="showAdd">添加规则</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="filters.scenario" placeholder="适用场景" clearable style="width: 140px" @change="loadData">
        <el-option label="储存" value="storage" />
        <el-option label="使用" value="usage" />
        <el-option label="废液" value="disposal" />
        <el-option label="全部" value="all" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="categoryAName" label="试剂 A" min-width="150" />
      <el-table-column label="↓" width="40" align="center">
        <template #default><el-icon><ArrowDown /></el-icon></template>
      </el-table-column>
      <el-table-column prop="categoryBName" label="试剂 B" min-width="150" />
      <el-table-column label="危害类型" width="100">
        <template #default="{ row }">
          <el-tag :type="hazardType(row.hazardType)" size="small">{{ hazardLabel(row.hazardType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="严重程度" width="90">
        <template #default="{ row }">
          <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="规则说明" min-width="260" show-overflow-tooltip />
      <el-table-column prop="scenario" label="场景" width="80">
        <template #default="{ row }">
          {{ scenarioLabel(row.scenario) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button text type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '添加规则'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="试剂 A" required>
          <el-select v-model="form.categoryAId" filterable placeholder="选择试剂品类..." style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name + (c.specification ? ' (' + c.specification + ')' : '')" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="试剂 B" required>
          <el-select v-model="form.categoryBId" filterable placeholder="选择试剂品类..." style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name + (c.specification ? ' (' + c.specification + ')' : '')" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="危害类型" required>
          <el-select v-model="form.hazardType" style="width: 100%">
            <el-option label="火灾" value="fire" />
            <el-option label="爆炸" value="explosion" />
            <el-option label="毒气" value="toxic_gas" />
            <el-option label="腐蚀" value="corrosion" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" required>
          <el-select v-model="form.severity" style="width: 100%">
            <el-option label="注意" value="warning" />
            <el-option label="危险" value="danger" />
            <el-option label="致命" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则说明" required>
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="处理建议">
          <el-input v-model="form.actionRequired" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="适用场景">
          <el-select v-model="form.scenario" style="width: 100%">
            <el-option label="储存" value="storage" />
            <el-option label="使用" value="usage" />
            <el-option label="废液" value="disposal" />
            <el-option label="全部" value="all" />
          </el-select>
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
import { getIncompatibilityRules, createIncompatibilityRule, updateIncompatibilityRule, deleteIncompatibilityRule, getCategoryList } from '@/api/reagent'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''
const categoryOptions = ref<any[]>([])

const filters = reactive({ scenario: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  categoryAId: '', categoryBId: '', hazardType: 'explosion', severity: 'danger',
  description: '', actionRequired: '', scenario: 'storage',
})

function hazardType(s: string) {
  return { fire: 'danger', explosion: 'danger', toxic_gas: 'danger', corrosion: 'warning', other: 'info' }[s] || 'info'
}
function hazardLabel(s: string) {
  return { fire: '火灾', explosion: '爆炸', toxic_gas: '毒气', corrosion: '腐蚀', other: '其他' }[s] || s
}
function severityType(s: string) {
  return { warning: 'warning', danger: 'danger', critical: 'danger' }[s] || 'info'
}
function severityLabel(s: string) {
  return { warning: '注意', danger: '危险', critical: '致命' }[s] || s
}
const scenarioMap: Record<string, string> = { storage: '储存', usage: '使用', disposal: '废液', all: '全部' }
function scenarioLabel(s: string) {
  return scenarioMap[s] || s
}

async function loadCategories() {
  try {
    const res: any = await getCategoryList({ page: 0, size: 200 })
    categoryOptions.value = res.data?.content || []
  } catch { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getIncompatibilityRules({
      scenario: filters.scenario || undefined,
      page: pagination.page, size: pagination.size,
    })
    tableData.value = res.data?.content || []
    pagination.total = res.data?.totalElements || 0
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { categoryAId: '', categoryBId: '', hazardType: 'explosion', severity: 'danger', description: '', actionRequired: '', scenario: 'storage' })
  dialogVisible.value = true
}

function showEdit(row: any) {
  isEdit.value = true
  editId = row.id
  Object.assign(form, {
    categoryAId: row.categoryAId, categoryBId: row.categoryBId,
    hazardType: row.hazardType, severity: row.severity,
    description: row.description, actionRequired: row.actionRequired || '',
    scenario: row.scenario,
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.description) { ElMessage.warning('请输入规则说明'); return }
  if (form.categoryAId === form.categoryBId) { ElMessage.warning('试剂 A 和 B 不能相同'); return }
  if (isEdit.value) {
    await updateIncompatibilityRule(editId, form)
    ElMessage.success('规则已更新')
  } else {
    await createIncompatibilityRule(form)
    ElMessage.success('规则已创建')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要删除该禁忌规则吗？', '警告', { type: 'warning' })
  await deleteIncompatibilityRule(id)
  ElMessage.success('规则已删除')
  loadData()
}

onMounted(() => { loadData(); loadCategories() })
</script>
