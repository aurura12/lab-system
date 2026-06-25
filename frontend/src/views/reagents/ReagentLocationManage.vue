<template>
  <div class="page-container">
    <div class="page-header">
      <h2>存储位置管理</h2>
      <el-button type="primary" @click="showAddCabinet">添加柜子</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="selectedRoomId" placeholder="选择房间" style="width: 200px;" @change="loadTree">
        <el-option v-for="r in rooms" :key="r.id" :label="r.name + ' (' + r.roomNumber + ')'" :value="r.id" />
      </el-select>
    </div>

    <div v-if="!selectedRoomId" class="empty-state">
      请先选择一个房间
    </div>

    <div v-else class="feature-card" style="padding: 16px;">
      <div v-if="loading" style="text-align: center; padding: 40px; color: var(--color-ink-muted);">加载中...</div>
      <el-tree
        v-else
        :data="treeData"
        :props="{ children: 'children', label: 'label' }"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        style="font-size: 15px;"
      >
        <template #default="{ node, data }">
          <div style="display: flex; align-items: center; gap: 8px; flex: 1; padding: 4px 0;">
            <span :style="{ fontWeight: data.level === 'cabinet' ? 600 : 400 }">
              <el-icon v-if="data.level === 'cabinet'" style="margin-right: 4px;"><Folder /></el-icon>
              <el-icon v-else-if="data.level === 'shelf'" style="margin-right: 4px; color: var(--color-ink-muted);"><FolderOpened /></el-icon>
              <el-icon v-else style="margin-right: 4px; color: var(--color-ink-muted);"><HomeFilled /></el-icon>
              {{ data.label }}
            </span>
            <span v-if="data.path" style="color: var(--color-ink-faint); font-size: 13px; margin-left: 4px;">{{ data.path }}</span>
            <div style="margin-left: auto; display: flex; gap: 4px;">
              <el-button v-if="data.level === 'cabinet'" text type="primary" size="small" @click.stop="showAddShelf(data)">+ 层</el-button>
              <el-button v-if="data.level === 'shelf'" text type="primary" size="small" @click.stop="showAddGrid(data)">+ 格</el-button>
              <el-button text type="primary" size="small" @click.stop="showEdit(data)">编辑</el-button>
              <el-button text type="danger" size="small" @click.stop="handleDelete(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="locationForm" label-width="80px">
        <el-form-item label="编码" required>
          <el-input v-model="locationForm.code" :placeholder="'如：柜' + (treeData.length + 1)" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="locationForm.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="locationForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveLocation">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getLocationTree, createLocation, updateLocation, deleteLocation } from '@/api/reagent'
import { getLabs, getFloors, getRooms } from '@/api/lab'
import { Folder, FolderOpened, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const selectedRoomId = ref('')
const treeData = ref<any[]>([])
const rooms = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
let editLocationId = ''
let currentParent: any = null
const currentLevel = ref('cabinet')

const locationForm = reactive({ code: '', name: '', sortOrder: 0 })

function buildTree(nodes: any[]): any[] {
  return nodes.map((n: any) => ({
    id: n.id,
    label: n.name ? `${n.code} (${n.name})` : `${n.level === 'cabinet' ? '柜' : n.level === 'shelf' ? '层' : '格'} ${n.code}`,
    level: n.level,
    code: n.code,
    path: n.path,
    children: buildTree(n.children || []),
  }))
}

async function loadRooms() {
  const labsRes: any = await getLabs()
  const allRooms: any[] = []
  for (const lab of (labsRes.data || [])) {
    const floorsRes: any = await getFloors(lab.id)
    for (const floor of (floorsRes.data || [])) {
      const roomsRes: any = await getRooms(floor.id)
      allRooms.push(...(roomsRes.data || []))
    }
  }
  rooms.value = allRooms
  if (allRooms.length > 0) selectedRoomId.value = allRooms[0].id
}

async function loadTree() {
  if (!selectedRoomId.value) return
  loading.value = true
  try {
    const res: any = await getLocationTree(selectedRoomId.value)
    treeData.value = buildTree(res.data || [])
  } finally {
    loading.value = false
  }
}

function showAddCabinet() {
  isEdit.value = false
  currentLevel.value = 'cabinet'
  currentParent = null
  dialogTitle.value = '添加柜子'
  Object.assign(locationForm, { code: '', name: '', sortOrder: 0 })
  dialogVisible.value = true
}

function showAddShelf(parent: any) {
  isEdit.value = false
  currentLevel.value = 'shelf'
  currentParent = parent
  dialogTitle.value = `在 "${parent.label}" 下添加层`
  Object.assign(locationForm, { code: '', name: '', sortOrder: 0 })
  dialogVisible.value = true
}

function showAddGrid(parent: any) {
  isEdit.value = false
  currentLevel.value = 'grid'
  currentParent = parent
  dialogTitle.value = `在 "${parent.label}" 下添加格子`
  Object.assign(locationForm, { code: '', name: '', sortOrder: 0 })
  dialogVisible.value = true
}

function showEdit(data: any) {
  isEdit.value = true
  editLocationId = data.id
  currentLevel.value = data.level
  dialogTitle.value = `编辑 ${data.label}`
  locationForm.code = data.code
  locationForm.name = data.label.includes('(') ? data.label.split('(')[1]?.replace(')', '') || '' : ''
  locationForm.sortOrder = 0
  dialogVisible.value = true
}

async function handleSaveLocation() {
  const payload: any = {
    level: currentLevel.value,
    code: locationForm.code,
    name: locationForm.name || undefined,
    sortOrder: locationForm.sortOrder,
    roomId: selectedRoomId.value,
  }
  if (currentParent && !isEdit.value) {
    payload.parentId = currentParent.id
  }

  if (isEdit.value) {
    await updateLocation(editLocationId, payload)
    ElMessage.success('位置已更新')
  } else {
    await createLocation(payload)
    ElMessage.success('位置已创建')
  }
  dialogVisible.value = false
  loadTree()
}

async function handleDelete(data: any) {
  await ElMessageBox.confirm(`确定删除 "${data.label}" 及其所有子节点？`, '警告', { type: 'warning' })
  await deleteLocation(data.id)
  ElMessage.success('已删除')
  loadTree()
}

onMounted(() => {
  loadRooms()
})
</script>
