<template>
  <div class="page-container">
    <div class="page-header">
      <h2>实验室管理</h2>
      <el-button type="primary" @click="showAddLab">添加实验室</el-button>
    </div>

    <div v-for="lab in labs" :key="lab.id" class="lab-section">
      <div class="lab-card">
        <div class="lab-card-header">
          <span class="lab-name">{{ lab.name }}</span>
          <div class="lab-actions">
            <el-button size="small" @click="showAddFloor(lab.id)">添加楼层</el-button>
            <el-button size="small" @click="handleDeleteLab(lab.id)">删除</el-button>
          </div>
        </div>
        <p class="lab-desc">{{ lab.description || '暂无描述' }}</p>
        <p class="lab-address">{{ lab.address || '' }}</p>

        <div class="floors-grid">
          <div v-for="floor in labFloors[lab.id]" :key="floor.id" class="floor-card" @click="enterFloor(floor)">
            <div class="floor-number">{{ floor.floorNumber }}F</div>
            <div class="floor-name">{{ floor.name || `第${floor.floorNumber}层` }}</div>
          </div>
          <div v-if="!labFloors[lab.id]?.length" class="empty-floors">
            暂无楼层，点击"添加楼层"创建
          </div>
        </div>
      </div>
    </div>

    <!-- Add Lab Dialog -->
    <el-dialog v-model="labDialogVisible" title="添加实验室" width="420px">
      <el-form :model="labForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="labForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="labForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="labForm.address" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="labDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddLab">确定</el-button>
      </template>
    </el-dialog>

    <!-- Add Floor Dialog -->
    <el-dialog v-model="floorDialogVisible" title="添加楼层" width="420px">
      <el-form :model="floorForm" label-width="100px">
        <el-form-item label="楼层号">
          <el-input-number v-model="floorForm.floorNumber" :min="1" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="floorForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="floorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddFloor">确定</el-button>
      </template>
    </el-dialog>

    <!-- Floor Detail (Rooms) -->
    <el-drawer v-model="floorDrawerVisible" size="50%">
      <template #header>
        <span>{{ currentFloor?.name || '楼层' }} · 房间列表</span>
      </template>
      <div class="rooms-header">
        <el-button type="primary" @click="showAddRoom">添加房间</el-button>
      </div>
      <div class="rooms-grid">
        <div v-for="room in floorRooms" :key="room.id" class="room-card">
          <div class="room-number">{{ room.roomNumber }}</div>
          <div class="room-name">{{ room.name || '' }}</div>
          <div class="room-area" v-if="room.areaSqm">{{ room.areaSqm }} m²</div>
        </div>
        <div v-if="!floorRooms.length" class="empty-rooms">暂无房间</div>
      </div>
    </el-drawer>

    <!-- Add Room Dialog -->
    <el-dialog v-model="roomDialogVisible" title="添加房间" width="420px">
      <el-form :model="roomForm" label-width="100px">
        <el-form-item label="房间号">
          <el-input v-model="roomForm.roomNumber" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="roomForm.name" />
        </el-form-item>
        <el-form-item label="面积(m²)">
          <el-input-number v-model="roomForm.areaSqm" :min="0" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRoom">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLabs, createLab, deleteLab, getFloors, createFloor, getRooms, createRoom } from '@/api/lab'
import { ElMessage, ElMessageBox } from 'element-plus'

const labs = ref<any[]>([])
const labFloors = ref<Record<string, any[]>>({})
const floorRooms = ref<any[]>([])
const currentFloor = ref<any>(null)

const labDialogVisible = ref(false)
const floorDialogVisible = ref(false)
const roomDialogVisible = ref(false)
const floorDrawerVisible = ref(false)

const labForm = reactive({ name: '', description: '', address: '' })
const floorForm = reactive({ floorNumber: 1, name: '' })
const roomForm = reactive({ roomNumber: '', name: '', areaSqm: 0 })

let currentLabId = ''

async function loadLabs() {
  const res: any = await getLabs()
  labs.value = res.data || []
  for (const lab of labs.value) {
    const floorsRes: any = await getFloors(lab.id)
    labFloors.value[lab.id] = floorsRes.data || []
  }
}

function showAddLab() {
  Object.assign(labForm, { name: '', description: '', address: '' })
  labDialogVisible.value = true
}

async function handleAddLab() {
  await createLab(labForm)
  ElMessage.success('实验室已创建')
  labDialogVisible.value = false
  loadLabs()
}

async function handleDeleteLab(id: string) {
  await ElMessageBox.confirm('确定要删除该实验室吗？', '警告', { type: 'warning' })
  await deleteLab(id)
  ElMessage.success('实验室已删除')
  loadLabs()
}

function showAddFloor(labId: string) {
  currentLabId = labId
  Object.assign(floorForm, { floorNumber: 1, name: '' })
  floorDialogVisible.value = true
}

async function handleAddFloor() {
  await createFloor(currentLabId, floorForm)
  ElMessage.success('楼层已创建')
  floorDialogVisible.value = false
  loadLabs()
}

async function enterFloor(floor: any) {
  currentFloor.value = floor
  const res: any = await getRooms(floor.id)
  floorRooms.value = res.data || []
  floorDrawerVisible.value = true
}

function showAddRoom() {
  Object.assign(roomForm, { roomNumber: '', name: '', areaSqm: 0 })
  roomDialogVisible.value = true
}

async function handleAddRoom() {
  await createRoom(currentFloor.value.id, roomForm)
  ElMessage.success('房间已创建')
  roomDialogVisible.value = false
  enterFloor(currentFloor.value)
}

onMounted(loadLabs)
</script>

<style scoped lang="scss">
.lab-section {
  margin-bottom: 20px;
}

.lab-card {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--color-hairline);
}

.lab-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.lab-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-ink);
  letter-spacing: -0.125px;
}

.lab-actions {
  display: flex;
  gap: 8px;
}

.lab-desc {
  font-size: 15px;
  color: var(--color-ink-muted);
  margin-bottom: 4px;
}

.lab-address {
  font-size: 14px;
  color: var(--color-ink-faint);
  margin-bottom: 20px;
}

.floors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.floor-card {
  background: var(--color-surface);
  border: 1px solid var(--color-hairline);
  border-radius: 12px;
  padding: 20px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    border-color: var(--color-primary);
    box-shadow:
      0 0.175px 1.041px rgba(0,0,0,0.01),
      0 0.8px 2.925px rgba(0,0,0,0.02),
      0 2.025px 7.847px rgba(0,0,0,0.027),
      0 4px 18px rgba(0,0,0,0.04);
  }

  .floor-number {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-primary);
    letter-spacing: -0.5px;
  }

  .floor-name {
    font-size: 13px;
    color: var(--color-ink-muted);
    margin-top: 4px;
  }
}

.empty-floors, .empty-rooms {
  color: var(--color-ink-faint);
  grid-column: 1 / -1;
  text-align: center;
  padding: 28px 20px;
  font-size: 14px;
}

.rooms-header {
  margin-bottom: 16px;
  padding: 0 24px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  padding: 0 24px 24px;
}

.room-card {
  background: var(--color-surface);
  border: 1px solid var(--color-hairline);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: border-color 0.15s;

  &:hover {
    border-color: var(--color-primary);
  }

  .room-number {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-ink);
    letter-spacing: -0.125px;
  }

  .room-name {
    font-size: 14px;
    color: var(--color-ink-muted);
    margin-top: 4px;
  }

  .room-area {
    font-size: 13px;
    color: var(--color-ink-faint);
    margin-top: 4px;
  }
}
</style>
