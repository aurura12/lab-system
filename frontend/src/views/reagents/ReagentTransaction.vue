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
        <el-form-item label="存放位置">
          <el-select v-model="inboundForm.locationId" placeholder="选择存放位置（可选）" clearable filterable style="width: 100%" @change="onLocationChange">
            <el-option v-for="loc in allLocations" :key="loc.id" :label="loc.path || loc.code" :value="loc.id" />
          </el-select>
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

    <!-- 条码打印对话框 -->
    <el-dialog v-model="barcodeDialogVisible" title="条码打印" width="400px" :close-on-click-modal="false">
      <div style="text-align: center;">
        <div style="font-size: 14px; color: var(--color-ink-muted); margin-bottom: 12px;">
          试剂：<strong>{{ lastInboundResult?.categoryName }}</strong>
        </div>
        <div style="margin-bottom: 8px;">
          <svg ref="barcodeSvgRef"></svg>
        </div>
        <div style="font-size: 20px; font-weight: 700; letter-spacing: 2px; margin-bottom: 8px;">
          {{ lastInboundResult?.barcode }}
        </div>
        <div style="font-size: 13px; color: var(--color-ink-faint);">
          有效期：{{ lastInboundResult?.expiryDate }} &nbsp;|&nbsp; 批次：{{ lastInboundResult?.batchNo }}
        </div>
      </div>
      <template #footer>
        <el-button @click="barcodeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="printBarcode">打印条码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getCategoryList, getInventoryList, inboundReagent, useReagent, batchUseReagent, checkLocationIncompatibility, getAllLocations } from '@/api/reagent'
import { ElMessage, ElMessageBox } from 'element-plus'
import JsBarcode from 'jsbarcode'

const activeTab = ref('inbound')

// 入库
const inboundLoading = ref(false)
const inboundForm = reactive({
  categoryId: '', barcode: '', totalQuantity: 1, unit: '',
  manufactureDate: '', expiryDate: '', supplier: '', storageConditions: '',
  locationId: '',
})
const allLocations = ref<any[]>([])
const barcodeDialogVisible = ref(false)
const lastInboundResult = ref<any>(null)
const barcodeSvgRef = ref<SVGSVGElement | null>(null)
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
  try {
    const res: any = await getCategoryList({ page: 0, size: 200 })
    categoryOptions.value = res.data.content || []
  } catch {
    // 品类列表加载失败不影响出入库操作
  }
}

async function lookupBarcode() {
  if (!outboundBarcode.value.trim()) return
  try {
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
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '查询失败')
  }
}

async function addBatchItem() {
  const code = batchBarcodeInput.value.trim()
  if (!code) return
  try {
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
  } catch {
    ElMessage.warning('查询失败，请检查条码是否正确')
  }
}

async function onLocationChange(val: string) {
  if (!val || !inboundForm.categoryId) return
  try {
    const res: any = await checkLocationIncompatibility(val, inboundForm.categoryId)
    if (res.data?.hasConflict) {
      const conflicts = res.data.directConflicts || []
      const msg = conflicts.map((c: any) =>
        `<b>${c.categoryAName}</b> 与 <b>${c.categoryBName}</b>：${c.description}<br/>建议：${c.actionRequired || '请确认安全性'}`
      ).join('<br/><br/>')
      await ElMessageBox.confirm(
        `<div style="line-height:1.6;">检测到配伍禁忌冲突：<br/><br/>${msg}</div>`,
        '入库位置禁忌预警',
        { confirmButtonText: '仍然入库', cancelButtonText: '换个位置', type: 'warning', dangerouslyUseHTMLString: true }
      )
    }
  } catch {
    // 用户选择换个位置，清空选择
    inboundForm.locationId = ''
  }
}

async function loadLocations() {
  try {
    const res: any = await getAllLocations()
    allLocations.value = res.data || []
  } catch { /* ignore */ }
}

async function handleInbound() {
  if (!inboundForm.categoryId || !inboundForm.expiryDate) {
    ElMessage.warning('请填写品类和有效期')
    return
  }
  inboundLoading.value = true
  try {
    const res: any = await inboundReagent(inboundForm)
    lastInboundResult.value = res.data
    Object.assign(inboundForm, { categoryId: '', barcode: '', totalQuantity: 1, unit: '', manufactureDate: '', expiryDate: '', supplier: '', storageConditions: '', locationId: '' })
    await nextTick()
    if (barcodeSvgRef.value && lastInboundResult.value?.barcode) {
      JsBarcode(barcodeSvgRef.value, lastInboundResult.value.barcode, {
        format: 'CODE128',
        width: 2,
        height: 60,
        displayValue: false,
        margin: 10,
      })
    }
    barcodeDialogVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '入库失败')
  } finally {
    inboundLoading.value = false
  }
}

function printBarcode() {
  const svg = barcodeSvgRef.value
  if (!svg) return
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`
    <html><head><title>打印条码</title>
    <style>body{text-align:center;padding:40px;font-family:sans-serif;}
    .barcode{font-size:20px;font-weight:700;letter-spacing:2px;margin-top:10px;}
    .info{font-size:13px;color:#666;margin-top:6px;}
    @media print{body{padding:20px;}}</style>
    </head><body>
    ${svg.outerHTML}
    <div class="barcode">${lastInboundResult.value?.barcode || ''}</div>
    <div class="info">${lastInboundResult.value?.categoryName || ''}</div>
    <div class="info">有效期: ${lastInboundResult.value?.expiryDate || ''}</div>
    </body></html>
  `)
  win.document.close()
  setTimeout(() => { win.print(); win.close() }, 500)
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
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '出库失败')
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
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '批次出库失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(() => { loadCategories(); loadLocations() })
</script>
