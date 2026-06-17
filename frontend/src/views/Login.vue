<template>
  <div class="login-container">
    <div class="login-overlay"></div>
    <div class="login-card">
      <h1>实验室管理系统</h1>
      <p class="subtitle">设备与数据管控平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" style="width: 100%">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('/login-bg.png');
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.login-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1;
}

.login-card {
  position: relative;
  z-index: 2;
  width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  border: none;
  box-shadow:
    0 0.175px 1.041px rgba(0,0,0,0.02),
    0 0.8px 2.925px rgba(0,0,0,0.03),
    0 2.025px 7.847px rgba(0,0,0,0.04),
    0 4px 18px rgba(0,0,0,0.05),
    0 23px 52px rgba(0,0,0,0.05);

  h1 {
    text-align: center;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: -0.625px;
    color: rgba(0, 0, 0, 0.95);
    margin-bottom: 4px;
    line-height: 1.23;
  }

  .subtitle {
    text-align: center;
    font-size: 15px;
    font-weight: 400;
    color: #615d59;
    margin-bottom: 32px;
    line-height: 1.33;
  }

  // ── Override input styles inside the login card ──
  :deep(.el-input__wrapper) {
    border-radius: 4px !important;
    border: 1px solid rgb(221, 221, 221) !important;
    box-shadow: none !important;
    padding: 2px 6px !important;
    background: #ffffff !important;
  }

  :deep(.el-input__wrapper.is-focus) {
    border-color: #0075de !important;
    box-shadow: 0 0 0 1px #0075de !important;
  }

  :deep(.el-input__inner) {
    height: 30px !important;
    font-size: 15px !important;
    color: #000000 !important;
  }

  :deep(.el-input__inner::placeholder) {
    color: #a39e98 !important;
  }

  // ── Override button inside the login card ──
  :deep(.el-button.el-button--primary) {
    border-radius: 9999px !important;
    padding: 10px 28px !important;
    font-size: 16px !important;
    font-weight: 500 !important;
    letter-spacing: 0 !important;
    border: none !important;
    background: #0075de !important;
    color: #ffffff !important;
    transition: all 0.15s ease;
  }

  :deep(.el-button.el-button--primary:active) {
    background: #005bab !important;
    transform: scale(0.97);
  }

  :deep(.el-button.el-button--primary.is-disabled) {
    opacity: 0.5;
  }

  // ── Override form item spacing ──
  :deep(.el-form-item) {
    margin-bottom: 18px !important;
  }

  :deep(.el-input__prefix) {
    color: #a39e98 !important;
  }
}
</style>
