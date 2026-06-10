<template>
  <div class="page-container">
    <el-page-header @back="router.back()" title="返回" :content="project.name || '项目详情'" />

    <el-card style="margin-top: 24px">
      <template #header>项目信息</template>
      <el-descriptions :column="2">
        <el-descriptions-item label="名称">{{ project.name }}</el-descriptions-item>
        <el-descriptions-item label="编号">{{ project.code || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="project.status === 'active' ? 'success' : project.status === 'paused' ? 'warning' : 'info'" size="small">{{ { active: '进行中', paused: '暂停', completed: '已完成' }[project.status] || project.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="负责人">{{ project.ownerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ project.startDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ project.endDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ project.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header-flex">
          <span>成员列表</span>
          <el-button size="small" @click="showAddMember">添加成员</el-button>
        </div>
      </template>
      <el-table :data="members" stripe>
        <el-table-column prop="user.username" label="用户名" width="150" />
        <el-table-column prop="user.realName" label="姓名" width="150" />
        <el-table-column prop="roleInProject" label="角色" width="100" />
        <el-table-column prop="joinedAt" label="加入时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button text type="danger" @click="handleRemoveMember(row.user.id)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="memberDialogVisible" title="添加成员" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="memberForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.roleInProject">
            <el-option label="负责人" value="lead" />
            <el-option label="成员" value="member" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProjectById, getProjectMembers, addProjectMember, removeProjectMember } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const project = ref<any>({})
const members = ref<any[]>([])
const memberDialogVisible = ref(false)
const memberForm = reactive({ userId: '', roleInProject: 'member' })

async function loadData() {
  const res: any = await getProjectById(id)
  project.value = res.data
  const membersRes: any = await getProjectMembers(id)
  members.value = membersRes.data || []
}

function showAddMember() {
  Object.assign(memberForm, { userId: '', roleInProject: 'member' })
  memberDialogVisible.value = true
}

async function handleAddMember() {
  await addProjectMember(id, memberForm)
  ElMessage.success('成员已添加')
  memberDialogVisible.value = false
  loadData()
}

async function handleRemoveMember(userId: string) {
  await ElMessageBox.confirm('确定要移除该成员吗？', '警告', { type: 'warning' })
  await removeProjectMember(id, userId)
  ElMessage.success('成员已移除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
