<template>
  <div class="page-container">
    <div class="page-header">
      <h2>试剂品类管理</h2>
      <el-button type="primary" @click="showAdd">添加品类</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="filters.keyword" placeholder="搜索试剂名称或 CAS 号..." clearable style="width: 280px" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="filters.hazardClass" placeholder="危险分类" clearable style="width: 140px" @change="loadData">
        <el-option label="易燃" value="flammable" />
        <el-option label="腐蚀" value="corrosive" />
        <el-option label="氧化剂" value="oxidizing" />
        <el-option label="有毒" value="toxic" />
        <el-option label="无" value="none" />
      </el-select>
      <el-button @click="loadData">搜索</el-button>
    </div>

    <el-table :data="tableData" stripe v-loading="loading">
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="casNo" label="CAS 号" width="120" />
      <el-table-column prop="molecularFormula" label="分子式" width="120" />
      <el-table-column prop="specification" label="规格" width="140" />
      <el-table-column prop="hazardClass" label="危险分类" width="100">
        <template #default="{ row }">
          <el-tag :type="hazardType(row.hazardClass)" size="small">{{ hazardLabel(row.hazardClass) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="60" />
      <el-table-column prop="defaultShelfLifeDays" label="有效期(天)" width="100" />
      <el-table-column prop="minStockThreshold" label="最低库存" width="90" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <div class="action-cell">
            <el-button text type="primary" @click="showEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row.id)">删除</el-button>
          </div>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑品类' : '添加品类'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="CAS 号">
          <el-input v-model="form.casNo" placeholder="如 64-17-5" />
        </el-form-item>
        <el-form-item label="分子式">
          <el-input v-model="form.molecularFormula" placeholder="如 C2H5OH" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="form.specification" placeholder="如 500mL 分析纯" />
        </el-form-item>
        <el-form-item label="危险分类">
          <el-select v-model="form.hazardClass" clearable>
            <el-option label="易燃" value="flammable" />
            <el-option label="腐蚀" value="corrosive" />
            <el-option label="氧化剂" value="oxidizing" />
            <el-option label="有毒" value="toxic" />
            <el-option label="无" value="none" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如 瓶、L、g" />
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="form.defaultShelfLifeDays" :min="1" :step="365" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最低库存">
          <el-input-number v-model="form.minStockThreshold" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="储存条件">
          <el-input v-model="form.storageConditions" type="textarea" :rows="2" />
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
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/reagent'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
let editId = ''

const filters = reactive({ keyword: '', hazardClass: '' })
const pagination = reactive({ page: 0, size: 10, total: 0 })
const form = reactive({
  name: '', casNo: '', molecularFormula: '', specification: '',
  hazardClass: 'none', unit: '瓶', defaultShelfLifeDays: 1825, minStockThreshold: 1,
  storageConditions: '',
})

function hazardType(s: string) {
  return { flammable: 'warning', corrosive: 'danger', oxidizing: 'warning', toxic: 'danger', none: 'info' }[s] || 'info'
}

function hazardLabel(s: string) {
  return { flammable: '易燃', corrosive: '腐蚀', oxidizing: '氧化剂', toxic: '有毒', none: '无' }[s] || s || '无'
}

async function loadData() {
  loading.value = true
  try {
    const res: any = await getCategoryList({
      keyword: filters.keyword || undefined,
      hazardClass: filters.hazardClass || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function showAdd() {
  isEdit.value = false
  Object.assign(form, { name: '', casNo: '', molecularFormula: '', specification: '', hazardClass: 'none', unit: '瓶', defaultShelfLifeDays: 1825, minStockThreshold: 1, storageConditions: '' })
  dialogVisible.value = true
}

function showEdit(row: any) {
  isEdit.value = true
  editId = row.id
  Object.assign(form, {
    name: row.name, casNo: row.casNo, molecularFormula: row.molecularFormula,
    specification: row.specification, hazardClass: row.hazardClass || 'none', unit: row.unit,
    defaultShelfLifeDays: row.defaultShelfLifeDays, minStockThreshold: row.minStockThreshold,
    storageConditions: row.storageConditions,
  })
  dialogVisible.value = true
}

async function handleSave() {
  if (isEdit.value) {
    await updateCategory(editId, form)
    ElMessage.success('品类已更新')
  } else {
    await createCategory(form)
    ElMessage.success('品类已创建')
  }
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id: string) {
  await ElMessageBox.confirm('确定要删除该品类吗？已有库存的品类删除后可能影响数据。', '警告', { type: 'warning' })
  await deleteCategory(id)
  ElMessage.success('品类已删除')
  loadData()
}

onMounted(loadData)
</script>
