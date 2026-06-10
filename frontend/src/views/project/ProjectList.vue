<template>
  <div class="page-container">
    <div class="page-header">
      <h2>项目管理</h2>
      <el-button type="primary" @click="showAdd">添加项目</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索项目..." clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="180" />
      <el-table-column prop="code" label="编号" width="130" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'paused' ? 'warning' : 'info'" size="small">
            {{ { active: '进行中', paused: '暂停', completed: '已完成' }[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ownerName" label="负责人" width="120" />
      <el-table-column prop="memberCount" label="成员数" width="90" />
      <el-table-column prop="startDate" label="开始日期" width="110" />
      <el-table-column prop="endDate" label="结束日期" width="110" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="viewDetail(row.id)">详情</el-button>
          <el-button text type="primary" @click="showEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row.id)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑项目' : '添加项目'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编号">
          <el-input v-model="form.code" placeholder="如 PROJ-2026-001" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="进行中" value="active" />
            <el-option label="暂停" value="paused" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
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
import { useRouter } from 'vue-router'
import { getProjects, createProject, updateProject, deleteProject } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  name: '', code: '', description: '', status: 'active', startDate: '', endDate: '',
})

async function loadData() {
  loading.value = true
  try {
    const res: any = await getProjects({ keyword: keyword.value || undefined, page: pagination.page, size: pagination.size })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { name: '', code: '', description: '', status: 'active', startDate: '', endDate: '' })
  dialogVisible.value = true
}

function showEdit(row: any) {
  isEdit.value = true
  editId = row.id
  Object.assign(form, { name: row.name, code: row.code, description: row.description, status: row.status, startDate: row.startDate, endDate: row.endDate })
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateProject(editId, form)
    ElMessage.success('项目已更新')
  } else {
    await createProject(form)
    ElMessage.success('项目已创建')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要删除该项目吗？', '警告', { type: 'warning' })
  await deleteProject(id)
  ElMessage.success('项目已删除')
  loadData()
}

function viewDetail(id: string) {
  router.push(`/projects/${id}`)
}

onMounted(loadData)
</script>
