<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAdd">添加用户</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户..." clearable style="width: 240px" @clear="loadData" @keyup.enter="loadData" />
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : row.role === 'lab_manager' ? 'warning' : 'info'" size="small">
            {{ { admin: '系统管理员', lab_manager: '实验室管理员', researcher: '研究员' }[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="isActive" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'" size="small">{{ row.isActive ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="450px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '留空则保持不变' : ''" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="系统管理员" value="admin" />
            <el-option label="实验室管理员" value="lab_manager" />
            <el-option label="研究员" value="researcher" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.isActive" />
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
import { getUsers, createUser, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  username: '', password: '', realName: '', email: '', role: 'researcher', phone: '', isActive: true,
})

async function loadData() {
  loading.value = true
  try {
    const res: any = await getUsers({ keyword: keyword.value || undefined, page: pagination.page, size: pagination.size })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { username: '', password: '', realName: '', email: '', role: 'researcher', phone: '', isActive: true })
  dialogVisible.value = true
}

function showEdit(row: any) {
  isEdit.value = true
  editId = row.id
  Object.assign(form, { username: row.username, password: '', realName: row.realName, email: row.email, role: row.role, phone: row.phone, isActive: row.isActive })
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    const data = { ...form }
    if (!data.password) delete data.password
    await updateUser(editId, data)
    ElMessage.success('用户已更新')
  } else {
    await createUser(form)
    ElMessage.success('用户已创建')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要停用该用户吗？', '警告', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('用户已停用')
  loadData()
}

onMounted(loadData)
</script>
