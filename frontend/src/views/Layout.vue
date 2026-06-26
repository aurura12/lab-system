<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">L</div>
        <span v-if="!isCollapse" class="logo-text">实验室管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/labs">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>实验室管理</template>
        </el-menu-item>
        <el-menu-item index="/equipment">
          <el-icon><Monitor /></el-icon>
          <template #title>设备管理</template>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <template #title>项目管理</template>
        </el-menu-item>
        <el-menu-item index="/usage">
          <el-icon><Timer /></el-icon>
          <template #title>使用记录</template>
        </el-menu-item>
        <el-menu-item index="/data">
          <el-icon><Document /></el-icon>
          <template #title>数据记录</template>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>

        <!-- 试剂管理 -->
        <el-sub-menu index="/reagents">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>试剂管理</span>
          </template>
          <el-menu-item index="/reagents/categories">品类管理</el-menu-item>
          <el-menu-item index="/reagents/inventory">库存总览</el-menu-item>
          <el-menu-item index="/reagents/transaction">出入库操作</el-menu-item>
          <el-menu-item index="/reagents/expiring">临期预警</el-menu-item>
          <el-menu-item index="/reagents/locations">位置管理</el-menu-item>
          <el-menu-item index="/reagents/locations/search">位置查询</el-menu-item>
          <el-menu-item index="/reagents/trace">试剂追溯</el-menu-item>
          <el-menu-item index="/reagents/report">使用报表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ authStore.user?.realName || authStore.user?.username || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  DataAnalysis, OfficeBuilding, Monitor, Folder, Timer, Document,
  User, Fold, Expand, ArrowDown, Collection
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta?.title as string) || '仪表盘')
const isAdmin = computed(() => authStore.user?.role === 'admin')

function handleCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.sidebar {
  background: var(--color-surface);
  border-right: 1px solid var(--color-hairline);
  transition: width 0.25s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    gap: 10px;
    border-bottom: 1px solid var(--color-hairline);
    flex-shrink: 0;
  }

  .logo-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: var(--color-primary);
    color: var(--color-on-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 16px;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-ink);
    letter-spacing: -0.125px;
    white-space: nowrap;
  }

  .sidebar-menu {
    background: transparent;
    padding: 8px 0;
    flex: 1;
    overflow-y: auto;

    .el-menu-item {
      margin: 2px 10px;
      font-size: 15px;
    }
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-hairline);
  padding: 0 24px;
  height: 60px !important;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-btn {
      font-size: 18px;
      cursor: pointer;
      color: var(--color-ink-muted);
      &:hover { color: var(--color-primary); }
    }

    .page-title {
      font-size: 15px;
      font-weight: 500;
      color: var(--color-ink-secondary);
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      color: var(--color-ink-secondary);
      font-size: 14px;
      &:hover { color: var(--color-primary); }
    }
  }
}

.main-content {
  background: var(--color-canvas-soft);
  overflow-y: auto;
}
</style>
