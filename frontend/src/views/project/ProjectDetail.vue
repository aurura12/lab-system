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

    <el-dialog v-model="memberDialogVisible" title="添加成员" width="420px">
      <el-form label-width="80px">
        <el-form-item label="选择用户">
          <el-select
            ref="selectRef"
            v-model="memberForm.userIds"
            multiple
            filterable
            remote
            clearable
            :remote-method="searchUsers"
            :loading="searching"
            placeholder="输入姓名、用户名搜索"
            style="width: 100%"
            @focus="loadInitialUsers"
            @change="onMemberSelect"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="`${u.realName || u.username} (${u.username})`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.roleInProject" style="width: 100%">
            <el-option label="负责人" value="lead" />
            <el-option label="成员" value="member" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember" :disabled="!memberForm.userIds.length">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProjectById, getProjectMembers, addProjectMember, removeProjectMember } from '@/api/project'
import { getUsers } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const id = route.params.id as string
const project = ref<any>({})
const members = ref<any[]>([])
const memberDialogVisible = ref(false)
const memberForm = reactive({ userIds: [] as string[], roleInProject: 'member' })
const userOptions = ref<any[]>([])
const searching = ref(false)
const selectRef = ref<any>(null)

async function searchUsers(query: string) {
  if (!query) {
    userOptions.value = []
    return
  }
  searching.value = true
  try {
    const res: any = await getUsers({ keyword: query, size: 20 })
    userOptions.value = res.data?.content || []
  } finally {
    searching.value = false
  }
}

async function loadInitialUsers() {
  if (!userOptions.value.length) {
    searching.value = true
    try {
      const res: any = await getUsers({ size: 50 })
      userOptions.value = res.data?.content || []
    } finally {
      searching.value = false
    }
  }
}

function onMemberSelect() {
  nextTick(() => {
    const el = selectRef.value
    if (el) {
      el.blur()
      nextTick(() => {
        el.focus()
      })
    }
  })
}

async function loadData() {
  const res: any = await getProjectById(id)
  project.value = res.data
  const membersRes: any = await getProjectMembers(id)
  members.value = membersRes.data || []
}

function showAddMember() {
  Object.assign(memberForm, { userIds: [], roleInProject: 'member' })
  userOptions.value = []
  memberDialogVisible.value = true
}

async function handleAddMember() {
  if (!memberForm.userIds.length) return
  for (const uid of memberForm.userIds) {
    await addProjectMember(id, { userId: uid, roleInProject: memberForm.roleInProject })
  }
  ElMessage.success(`已添加 ${memberForm.userIds.length} 名成员`)
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
