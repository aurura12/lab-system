import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘' },
      },
      {
        path: 'labs',
        name: 'Labs',
        component: () => import('@/views/lab/FloorList.vue'),
        meta: { title: '实验室管理' },
      },
      {
        path: 'equipment',
        name: 'Equipment',
        component: () => import('@/views/equipment/EquipmentList.vue'),
        meta: { title: '设备管理' },
      },
      {
        path: 'equipment/:id',
        name: 'EquipmentDetail',
        component: () => import('@/views/equipment/EquipmentDetail.vue'),
        meta: { title: '设备详情' },
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目管理' },
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue'),
        meta: { title: '项目详情' },
      },
      {
        path: 'usage',
        name: 'UsageRecords',
        component: () => import('@/views/usage/UsageRecordList.vue'),
        meta: { title: '使用记录' },
      },
      {
        path: 'data',
        name: 'DataRecords',
        component: () => import('@/views/data/DataRecordList.vue'),
        meta: { title: '数据记录' },
      },
      // ─── 试剂管理 ───
      {
        path: 'reagents/categories',
        name: 'ReagentCategories',
        component: () => import('@/views/reagents/ReagentCategoryList.vue'),
        meta: { title: '试剂品类管理' },
      },
      {
        path: 'reagents/inventory',
        name: 'ReagentInventory',
        component: () => import('@/views/reagents/ReagentInventoryList.vue'),
        meta: { title: '库存总览' },
      },
      {
        path: 'reagents/transaction',
        name: 'ReagentTransaction',
        component: () => import('@/views/reagents/ReagentTransaction.vue'),
        meta: { title: '出入库操作' },
      },
      {
        path: 'reagents/expiring',
        name: 'ReagentExpiring',
        component: () => import('@/views/reagents/ReagentExpiring.vue'),
        meta: { title: '临期预警' },
      },
      {
        path: 'reagents/locations',
        name: 'ReagentLocations',
        component: () => import('@/views/reagents/ReagentLocationManage.vue'),
        meta: { title: '位置管理' },
      },
      {
        path: 'reagents/locations/search',
        name: 'ReagentLocationSearch',
        component: () => import('@/views/reagents/ReagentLocationSearch.vue'),
        meta: { title: '位置查询' },
      },
      {
        path: 'reagents/trace',
        name: 'ReagentTrace',
        component: () => import('@/views/reagents/ReagentTrace.vue'),
        meta: { title: '试剂追溯' },
      },
      {
        path: 'reagents/report',
        name: 'ReagentReport',
        component: () => import('@/views/reagents/ReagentReport.vue'),
        meta: { title: '使用报表' },
      },
      {
        path: 'reagents/incompatibility/rules',
        name: 'IncompatibilityRules',
        component: () => import('@/views/reagents/IncompatibilityRules.vue'),
        meta: { title: '禁忌规则' },
      },
      {
        path: 'reagents/incompatibility',
        name: 'IncompatibilityCheck',
        component: () => import('@/views/reagents/IncompatibilityCheck.vue'),
        meta: { title: '禁忌检查' },
      },
      {
        path: 'reagents/locations/map',
        name: 'ReagentLocationMap',
        component: () => import('@/views/reagents/ReagentLocationMap.vue'),
        meta: { title: '位置导航' },
      },
      // ─── 设备预约 ───
      {
        path: 'equipment/bookings',
        name: 'BookingList',
        component: () => import('@/views/equipment/BookingList.vue'),
        meta: { title: '设备预约' },
      },
      {
        path: 'equipment/bookings/new',
        name: 'BookingCreate',
        component: () => import('@/views/equipment/BookingCreate.vue'),
        meta: { title: '新建预约' },
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/user/UserManage.vue'),
        meta: { title: '用户管理', requiresAdmin: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
