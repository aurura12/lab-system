<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂位置导航</h2>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入试剂名称、CAS 号或条码搜索..."
        clearable
        style="width: 300px;"
        @input="handleSearch"
        @keyup.enter="handleSearch"
        @clear="clearSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      <el-select v-model="selectedRoomId" placeholder="选择房间" style="width: 200px;" @change="loadCabinets">
        <el-option v-for="r in rooms" :key="r.id" :label="r.name + ' (' + r.roomNumber + ')'" :value="r.id" />
      </el-select>
    </div>

    <!-- 搜索结果表格 -->
    <div v-if="searchResults.length > 0" class="feature-card" style="margin-bottom: 16px;">
      <div style="font-size: 14px; color: var(--color-ink-muted); margin-bottom: 12px;">
        共找到 {{ searchResults.length }} 条结果
      </div>
      <el-table :data="searchResults" stripe size="small">
        <el-table-column prop="categoryName" label="名称" min-width="160" />
        <el-table-column prop="barcode" label="条码" width="110" />
        <el-table-column prop="remainingQuantity" label="余量" width="90">
          <template #default="{ row }">
            {{ row.remainingQuantity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="有效期" width="110" />
        <el-table-column prop="alertLevel" label="预警" width="80">
          <template #default="{ row }">
            <el-tag :type="alertType(row.alertLevel)" size="small">{{ alertLabel(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="位置" min-width="220">
          <template #default="{ row }">
            <div v-if="row.locationPath" style="display: flex; align-items: center; gap: 6px;">
              <el-icon style="color: var(--color-primary);"><Location /></el-icon>
              <span style="font-weight: 500;">{{ row.locationPath }}</span>
            </div>
            <span v-else style="color: var(--color-ink-faint);">未指定位置</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="!selectedRoomId" class="empty-state">请先选择一个房间</div>

    <template v-else-if="loading">
      <div class="empty-state">加载中...</div>
    </template>

    <template v-else>
      <!-- 搜索高亮结果 -->
      <div v-if="searchKeyword && highlightedId" class="feature-card" style="margin-bottom: 16px; padding: 12px 20px; border-left: 3px solid var(--color-primary);">
        <div style="display: flex; align-items: center; gap: 8px;">
          <el-icon style="color: var(--color-primary);"><Location /></el-icon>
          <span>
            已定位到：
            <strong>{{ highlightInfo?.reagentName }}</strong>
            <span style="color: var(--color-ink-muted); margin-left: 8px;">→ {{ highlightInfo?.location }}</span>
          </span>
          <el-button text type="primary" size="small" @click="scrollToCabinet">查看位置</el-button>
        </div>
      </div>

      <!-- 房间布局示意 -->
      <div class="feature-card">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px;">
          {{ currentRoomName }} — 柜体布局
          <span style="font-size: 14px; font-weight: 400; color: var(--color-ink-muted); margin-left: 8px;">
            {{ cabinets.length }} 个柜子
          </span>
        </div>

        <div v-if="cabinets.length === 0" class="empty-state">该房间暂无柜子，请先在"位置管理"中添加</div>

        <div v-else class="cabinet-grid">
          <div
            v-for="cabinet in sortedCabinets"
            :key="cabinet.id"
            :ref="el => setCabinetRef(cabinet.id, el as HTMLElement)"
            class="cabinet-card"
            :class="{ 'is-highlighted': highlightedCabinetId === cabinet.id }"
            @click="toggleCabinet(cabinet)"
          >
            <div class="cabinet-header">
              <el-icon style="font-size: 18px;"><Folder /></el-icon>
              <span class="cabinet-name">{{ cabinet.label }}</span>
              <span class="cabinet-badge">{{ cabinet.reagentCount }} 瓶</span>
            </div>

            <div v-if="expandedCabinetId === cabinet.id" class="cabinet-detail">
              <div v-if="cabinet.shelves.length === 0" style="color: var(--color-ink-faint); font-size: 13px; padding: 8px 0;">
                空柜
              </div>
              <div v-for="shelf in cabinet.shelves" :key="shelf.id" class="shelf-row">
                <div class="shelf-header" @click.stop="toggleShelf(shelf)">
                  <el-icon><FolderOpened /></el-icon>
                  <span>{{ shelf.label }}</span>
                  <span class="cabinet-badge">{{ shelf.reagents?.length || 0 }} 瓶</span>
                  <el-icon v-if="expandedShelfId !== shelf.id" style="margin-left: auto;"><ArrowRight /></el-icon>
                  <el-icon v-else style="margin-left: auto;"><ArrowDown /></el-icon>
                </div>
                <div v-if="expandedShelfId === shelf.id && shelf.reagents?.length" class="reagent-list">
                  <div v-for="r in shelf.reagents" :key="r.id" class="reagent-item">
                    <el-tag :type="alertType(r.alertLevel)" size="small" style="margin-right: 6px;">{{ alertLabel(r.alertLevel) }}</el-tag>
                    <span class="reagent-name">{{ r.categoryName }}</span>
                    <span class="reagent-info">{{ r.remainingQuantity }}{{ r.unit }} | 效期 {{ r.expiryDate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getLocationTree, getInventoryList, searchReagentLocation } from '@/api/reagent'
import { getLabs, getFloors, getRooms } from '@/api/lab'
import { Search, Location, Folder, FolderOpened, ArrowRight, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const rooms = ref<any[]>([])
const selectedRoomId = ref('')
const currentRoomName = ref('')
const cabinets = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const highlightedCabinetId = ref('')
const highlightedId = ref('')
const highlightInfo = ref<any>(null)
const expandedCabinetId = ref('')
const expandedShelfId = ref('')

const cabinetRefs: Record<string, HTMLElement> = {}

function setCabinetRef(id: string, el: HTMLElement | null) {
  if (el) cabinetRefs[id] = el
}

function alertType(s: string) {
  return { normal: 'success', warning_days: 'warning', warning_urgent: 'danger', expired: 'danger' }[s] || 'info'
}
function alertLabel(s: string) {
  return { normal: '正常', warning_days: '临期', warning_urgent: '紧急', expired: '过期' }[s] || s
}

async function loadRooms() {
  try {
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
    if (allRooms.length > 0) {
      selectedRoomId.value = allRooms[0].id
      currentRoomName.value = allRooms[0].name
      loadCabinets()
    }
  } catch { /* ignore */ }
}

async function loadCabinets() {
  if (!selectedRoomId.value) return
  loading.value = true
  try {
    const res: any = await getLocationTree(selectedRoomId.value)
    const tree = res.data || []
    currentRoomName.value = rooms.value.find(r => r.id === selectedRoomId.value)?.name || ''
    cabinets.value = await buildCabinetList(tree)
  } finally {
    loading.value = false
  }
}

async function buildCabinetList(tree: any[]): Promise<any[]> {
  // 一次查询拉取该房间所有库存，避免 N+1 循环
  const allRes: any = await getInventoryList({ page: 0, size: 1000 })
  const allReagents = (allRes.data?.content || [])
    .filter((r: any) => r.status !== 'disposed' && r.status !== 'expired')

  // 按 locationId 分组
  const byLocation: Record<string, any[]> = {}
  for (const r of allReagents) {
    const locId = r.locationId
    if (locId) {
      if (!byLocation[locId]) byLocation[locId] = []
      byLocation[locId].push(r)
    }
  }

  const result: any[] = []
  for (const cabinet of tree) {
    const c: any = { id: cabinet.id, label: cabinet.label, shelves: [], reagentCount: 0 }
    let totalReagents = 0
    for (const shelf of (cabinet.children || [])) {
      const s: any = { id: shelf.id, label: shelf.label, reagents: [] }
      // 该层的试剂 = 直接挂在 shelf 上的 + 挂在 shelf 子格子上的
      const shelfReagents = byLocation[shelf.id] || []
      const gridReagents: any[] = []
      for (const grid of (shelf.children || [])) {
        gridReagents.push(...(byLocation[grid.id] || []))
      }
      s.reagents = [...shelfReagents, ...gridReagents]
      totalReagents += s.reagents.length
      c.shelves.push(s)
    }
    c.reagentCount = totalReagents
    result.push(c)
  }
  return result
}

const sortedCabinets = computed(() => {
  return [...cabinets.value].sort((a, b) => {
    const numA = parseInt(a.label.match(/\d+/)?.[0] || '0')
    const numB = parseInt(b.label.match(/\d+/)?.[0] || '0')
    return (numA || 9999) - (numB || 9999) || a.label.localeCompare(b.label)
  })
})

async function handleSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) { clearSearch(); return }

  // Load search results table
  try {
    const res: any = await searchReagentLocation(kw)
    searchResults.value = res.data || []
  } catch {
    searchResults.value = []
  }

  // Also highlight the cabinet on the map
  try {
    const res: any = await getInventoryList({ keyword: kw, size: 1 })
    const items = res.data?.content || []
    if (items.length === 0) { clearHighlight(); return }

    const item = items[0]
    if (item.locationPath) {
      const pathParts = item.locationPath.split('/')
      const cabinetCode = pathParts.length > 1 ? pathParts[1] : ''
      highlightedCabinetId.value = ''
      for (const c of cabinets.value) {
        if (c.label.includes(cabinetCode) || (cabinetCode && c.label.includes(cabinetCode))) {
          highlightedCabinetId.value = c.id
          break
        }
      }
      highlightedId.value = item.barcode || item.id
      highlightInfo.value = {
        reagentName: item.categoryName,
        location: item.locationPath,
      }
      if (highlightedCabinetId.value) {
        expandedCabinetId.value = highlightedCabinetId.value
        scrollToCabinet()
      }
    } else {
      clearHighlight()
    }
  } catch {
    clearHighlight()
  }
}

function clearHighlight() {
  highlightedCabinetId.value = ''
  highlightedId.value = ''
  highlightInfo.value = null
}

function clearSearch() {
  searchResults.value = []
  clearHighlight()
}

function scrollToCabinet() {
  if (highlightedCabinetId.value && cabinetRefs[highlightedCabinetId.value]) {
    cabinetRefs[highlightedCabinetId.value].scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

function toggleCabinet(cabinet: any) {
  expandedCabinetId.value = expandedCabinetId.value === cabinet.id ? '' : cabinet.id
  expandedShelfId.value = ''
}

function toggleShelf(shelf: any) {
  expandedShelfId.value = expandedShelfId.value === shelf.id ? '' : shelf.id
}

onMounted(loadRooms)
</script>

<style scoped lang="scss">
.cabinet-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 12px;
}

.cabinet-card {
  background: var(--color-surface);
  border: 1px solid var(--color-hairline);
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.15s ease;

  &:hover {
    border-color: #ccc;
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  }

  &.is-highlighted {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 2px var(--color-primary-light-3);
  }
}

.cabinet-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}

.cabinet-name {
  flex: 1;
}

.cabinet-badge {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-ink-muted);
  background: var(--color-canvas-soft);
  padding: 0 8px;
  border-radius: 9999px;
  height: 20px;
  line-height: 20px;
}

.cabinet-detail {
  margin-top: 8px;
  border-top: 1px solid var(--color-hairline);
  padding-top: 8px;
}

.shelf-row {
  margin-bottom: 4px;
}

.shelf-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-ink-secondary);
  padding: 6px 4px;
  border-radius: 4px;
  cursor: pointer;

  &:hover {
    background: var(--color-canvas-soft);
  }
}

.reagent-list {
  padding-left: 24px;
}

.reagent-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  font-size: 14px;
  border-radius: 4px;

  &:hover {
    background: var(--color-canvas-soft);
  }

  .reagent-name {
    font-weight: 500;
    color: var(--color-ink);
  }

  .reagent-info {
    font-size: 13px;
    color: var(--color-ink-faint);
    margin-left: auto;
  }
}
</style>
