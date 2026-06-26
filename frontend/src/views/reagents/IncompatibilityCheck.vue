<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂禁忌检查</h2>
    </div>

    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
      <div class="feature-card">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 4px;">实验方案检查</div>
        <div style="font-size: 14px; color: var(--color-ink-muted); margin-bottom: 16px;">选择实验中会用到的试剂，系统自动检查是否存在禁忌组合</div>
        <el-select v-model="checkCategories" multiple filterable placeholder="搜索并选择试剂..." style="width: 100%; margin-bottom: 12px;">
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name + (c.specification ? ' (' + c.specification + ')' : '')" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="handleCheck" :disabled="checkCategories.length < 2" :loading="usageChecking">检查禁忌</el-button>
      </div>

      <div class="feature-card">
        <div style="font-size: 16px; font-weight: 600; margin-bottom: 4px;">废液倾倒检查</div>
        <div style="font-size: 14px; color: var(--color-ink-muted); margin-bottom: 16px;">选择要混合的废液成分，检查混合后是否存在禁忌风险</div>
        <el-select v-model="wasteCategories" multiple filterable placeholder="搜索并选择废液成分..." style="width: 100%; margin-bottom: 12px;">
          <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name + (c.specification ? ' (' + c.specification + ')' : '')" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="handleWasteCheck" :disabled="wasteCategories.length < 2" :loading="wasteChecking">检查禁忌</el-button>
      </div>
    </div>

    <!-- 检查结果 -->
    <div v-if="result" class="feature-card" style="margin-bottom: 20px;">
      <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 16px;">
        <el-icon v-if="!result.hasConflict" style="font-size: 20px; color: var(--color-success);"><SuccessFilled /></el-icon>
        <el-icon v-else style="font-size: 20px; color: var(--color-danger);"><WarningFilled /></el-icon>
        <span style="font-size: 16px; font-weight: 600;">
          {{ result.hasConflict ? '发现禁忌冲突' : '未发现禁忌冲突' }}
        </span>
      </div>

      <!-- 直接冲突 -->
      <div v-if="result.directConflicts?.length" style="margin-bottom: 16px;">
        <div style="font-size: 14px; font-weight: 600; color: var(--color-danger); margin-bottom: 8px;">
          直接冲突（{{ result.directConflicts.length }} 项）
        </div>
        <el-table :data="result.directConflicts" stripe size="small">
          <el-table-column label="试剂 A" prop="categoryAName" min-width="140" />
          <el-table-column label="试剂 B" prop="categoryBName" min-width="140" />
          <el-table-column label="危害" width="80">
            <template #default="{ row }">
              <el-tag :type="hazardType(row.hazardType)" size="small">{{ hazardLabel(row.hazardType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="严重程度" width="80">
            <template #default="{ row }">
              <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
          <el-table-column prop="actionRequired" label="处理建议" min-width="180" show-overflow-tooltip />
        </el-table>
      </div>

      <!-- 间接冲突 -->
      <div v-if="result.indirectConflicts?.length">
        <div style="font-size: 14px; font-weight: 600; color: var(--color-warning); margin-bottom: 8px;">
          间接关联（{{ result.indirectConflicts.length }} 项）
        </div>
        <el-table :data="result.indirectConflicts" stripe size="small">
          <el-table-column label="试剂 A" prop="categoryAName" min-width="140" />
          <el-table-column label="试剂 B" prop="categoryBName" min-width="140" />
          <el-table-column label="关联路径" prop="chain" min-width="300" />
          <el-table-column prop="actionRequired" label="建议" min-width="220" show-overflow-tooltip />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { checkIncompatibility, getCategoryList } from '@/api/reagent'
import { SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const categoryOptions = ref<any[]>([])
const checkCategories = ref<string[]>([])
const wasteCategories = ref<string[]>([])
const usageChecking = ref(false)
const wasteChecking = ref(false)
const result = ref<any>(null)

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

async function loadCategories() {
  try {
    const res: any = await getCategoryList({ page: 0, size: 200 })
    categoryOptions.value = res.data?.content || []
  } catch { /* ignore */ }
}

async function handleCheck() {
  if (checkCategories.value.length < 2) return
  usageChecking.value = true
  try {
    const res: any = await checkIncompatibility({ categoryIds: checkCategories.value, scenario: 'usage' })
    result.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '检查失败')
  } finally {
    usageChecking.value = false
  }
}

async function handleWasteCheck() {
  if (wasteCategories.value.length < 2) return
  wasteChecking.value = true
  try {
    const res: any = await checkIncompatibility({ categoryIds: wasteCategories.value, scenario: 'disposal' })
    result.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '检查失败')
  } finally {
    wasteChecking.value = false
  }
}

onMounted(loadCategories)
</script>
