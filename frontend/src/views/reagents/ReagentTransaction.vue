<template>
  <div class="page-container">
    <div class="page-header">
      <h2>出入库操作</h2>
    </div>

    <div class="search-bar" style="gap: 8px; margin-bottom: 20px;">
      <el-radio-group v-model="activeTab">
        <el-radio-button value="inbound">入库</el-radio-button>
        <el-radio-button value="outbound">出库</el-radio-button>
        <el-radio-button value="batch">批次出库</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 入库 -->
    <div v-if="activeTab === 'inbound'" class="feature-card">
      <div style="font-size: 16px; font-weight: 600; margin-bottom: 20px;">试剂入库</div>
      <el-form :model="inboundForm" label-width="100px" style="max-width: 600px;">
        <el-form-item label="所属品类" required>
          <el-select v-model="inboundForm.categoryId" filterable placeholder="搜索或选择品类..." style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name + (c.specification ? ' (' + c.specification + ')' : '')" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="inboundForm.barcode" placeholder="扫码或手动输入" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="inboundForm.totalQuantity" :min="0.1" :step="1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="inboundForm.unit" placeholder="默认取品类单位" />
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker v-model="inboundForm.manufactureDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期至" required>
          <el-date-picker v-model="inboundForm.expiryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="inboundForm.supplier" />
        </el-form-item>
        <el-form-item label="储存条件">
          <el-input v-model="inboundForm.storageConditions" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleInbound" :loading="inboundLoading">确认入库</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 出库（单瓶） -->
    <div v-if="activeTab === 'outbound'" class="feature-card">
      <div style="font-size: 16px; font-weight: 600; margin-bottom: 20px;">单瓶出库</div>
      <div style="display: flex; gap: 12px; margin-bottom: 20px;">
        <el-input v-model="outboundBarcode" placeholder="扫描或输入条码..." style="width: 300px;" @keyup.enter="lookupBarcode" />
        <el-button type="primary" @click="lookupBarcode">查询</el-button>
      </div>

      <div v-if="outboundReagent" class="feature-card" style="margin-bottom: 16px; padding: 16px;">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="名称">{{ outboundReagent.categoryName }}</el-descriptions-item>
          <el-descriptions-item label="条码">{{ outboundReagent.barcode }}</el-descriptions-item>
          <el-descriptions-item label="余量">{{ outboundReagent.remainingQuantity }} {{ outboundReagent.unit }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ outboundReagent.expiryDate }}</el-descriptions-item>
          <el-descriptions-item label="预警">
            <el-tag :type="alertType(outboundReagent.alertLevel)" size="small">{{ alertLabel(outboundReagent.alertLevel) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="位置">{{ outboundReagent.locationPath || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-form :model="outboundForm" label-width="80px" style="margin-top: 16px; max-width: 400px;">
          <el-form-item label="取用量">
            <el-input-number v-model="outboundForm.quantity" :min="0.1" :max="outboundReagent.remainingQuantity" :step="0.5" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="用途">
            <el-input v-model="outboundForm.purpose" placeholder="如：色谱柱活化" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleOutbound" :loading="outboundLoading">确认出库</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 批次出库 -->
    <div v-if="activeTab === 'batch'" class="feature-card">
      <div style="font-size: 16px; font-weight: 600; margin-bottom: 20px;">批次出库（一次多瓶）</div>
      <div style="display: flex; gap: 12px; margin-bottom: 16px;">
        <el-input v-model="batchBarcodeInput" placeholder="扫描条码后回车添加..." style="width: 300px;" @keyup.enter="addBatchItem" />
        <el-button @click="addBatchItem">添加</el-button>
      </div>

      <el-table :data="batchItems" stripe style="margin-bottom: 16px;">
        <el-table-column label="#" width="50">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="barcode" label="条码" width="130" />
        <el-table-column prop="categoryName" label="名称" width="150" />
        <el-table-column prop="remainingQuantity" label="余量" width="80" />
        <el-table-column label="取用量" width="150">
          <template #default="{ row }">
            <el-input-number v-model="row.useQuantity" :min="0.1" :max="row.remainingQuantity" :step="0.5" :precision="2" size="small" style="width: 120px;" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="60">
          <template #default="{ $index }">
            <el-button text type="danger" size="small" @click="batchItems.splice($index, 1)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display: flex; gap: 12px; align-items: flex-end;">
        <div style="flex: 1; max-width: 400px;">
          <el-input v-model="batchPurpose" placeholder="批次出库用途（可选）" />
        </div>
        <el-button type="primary" @click="handleBatchUse" :disabled="batchItems.length === 0" :loading="batchLoading">批次出库</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCategoryList, getInventoryList, inboundReagent, useReagent, batchUseReagent } from '@/api/reagent'
import { ElMessage } from 'element-plus'

const activeTab = ref('inbound')

// 入库
const inboundLoading = ref(false)
const inboundForm = reactive({
  categoryId: '', barcode: '', totalQuantity: 1, unit: '',
  manufactureDate: '', expiryDate: '', supplier: '', storageConditions: '',
})
const categoryOptions = ref<any[]>([])

// 单瓶出库
const outboundBarcode = ref('')
const outboundReagent = ref<any>(null)
const outboundLoading = ref(false)
const outboundForm = reactive({ quantity: 1, purpose: '' })

// 批次出库
const batchBarcodeInput = ref('')
const batchItems = ref<any[]>([])
const batchPurpose = ref('')
const batchLoading = ref(false)

function alertType(s: string) {
  return { normal: 'success', warning_days: 'warning', warning_urgent: 'danger', expired: 'danger' }[s] || 'info'
}

function alertLabel(s: string) {
  return { normal: '正常', warning_days: '临期', warning_urgent: '紧急', expired: '过期' }[s] || s
}

async function loadCategories() {
  const res: any = await getCategoryList({ page: 0, size: 200 })
  categoryOptions.value = res.data.content || []
}

async function lookupBarcode() {
  if (!outboundBarcode.value.trim()) return
  const res: any = await getInventoryList({ keyword: outboundBarcode.value.trim(), size: 1 })
  const list = res.data.content || []
  if (list.length > 0) {
    outboundReagent.value = list[0]
    outboundForm.quantity = list[0].remainingQuantity > 1 ? 1 : list[0].remainingQuantity
    outboundForm.purpose = ''
  } else {
    ElMessage.warning('未找到该条码的试剂')
    outboundReagent.value = null
  }
}

async function addBatchItem() {
  const code = batchBarcodeInput.value.trim()
  if (!code) return
  const res: any = await getInventoryList({ keyword: code, size: 1 })
  const list = res.data.content || []
  if (list.length > 0 && list[0].status !== 'disposed' && list[0].status !== 'expired') {
    if (batchItems.value.some(i => i.barcode === code)) {
      ElMessage.warning('该条码已在列表中')
    } else {
      batchItems.value.push({ ...list[0], useQuantity: list[0].remainingQuantity > 1 ? 1 : list[0].remainingQuantity })
    }
    batchBarcodeInput.value = ''
  } else {
    ElMessage.warning('未找到有效试剂')
  }
}

async function handleInbound() {
  if (!inboundForm.categoryId || !inboundForm.expiryDate) {
    ElMessage.warning('请填写品类和有效期')
    return
  }
  inboundLoading.value = true
  try {
    await inboundReagent(inboundForm)
    ElMessage.success('入库成功')
    Object.assign(inboundForm, { categoryId: '', barcode: '', totalQuantity: 1, unit: '', manufactureDate: '', expiryDate: '', supplier: '', storageConditions: '' })
  } finally {
    inboundLoading.value = false
  }
}

async function handleOutbound() {
  if (!outboundReagent.value) return
  outboundLoading.value = true
  try {
    await useReagent(outboundReagent.value.id, {
      quantity: outboundForm.quantity,
      purpose: outboundForm.purpose || undefined,
    })
    ElMessage.success('出库成功')
    outboundReagent.value = null
    outboundBarcode.value = ''
  } finally {
    outboundLoading.value = false
  }
}

async function handleBatchUse() {
  batchLoading.value = true
  try {
    await batchUseReagent({
      items: batchItems.value.map(i => ({ inventoryId: i.id, quantity: i.useQuantity })),
      purpose: batchPurpose.value || undefined,
    })
    ElMessage.success('批次出库成功')
    batchItems.value = []
    batchPurpose.value = ''
  } finally {
    batchLoading.value = false
  }
}

onMounted(loadCategories)
</script>
