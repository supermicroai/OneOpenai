<template>
  <div class="app-container">
    <a-layout style="min-height: 100vh">
      <a-layout-sider v-model:collapsed="collapsed" :trigger="null" collapsible>
        <div class="logo">
          <h3 v-if="!collapsed" style="color: white; text-align: center; margin: 16px 0;">OneOpenAI</h3>
          <h3 v-else style="color: white; text-align: center; margin: 16px 0;">OOA</h3>
        </div>
        <a-menu
          theme="dark"
          mode="inline"
          :selectedKeys="selectedKeys"
          @click="handleMenuClick"
        >
          <a-menu-item key="/">
            <template #icon>
              <CloudServerOutlined />
            </template>
            <span>服务提供商</span>
          </a-menu-item>
          <a-menu-item key="/models">
            <template #icon>
              <AppstoreOutlined />
            </template>
            <span>模型管理</span>
          </a-menu-item>
          <a-menu-item key="/tokens">
            <template #icon>
              <KeyOutlined />
            </template>
            <span>令牌管理</span>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>
      <a-layout>
        <a-layout-header style="background: #fff; padding: 0">
          <MenuUnfoldOutlined
            v-if="collapsed"
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
          <MenuFoldOutlined
            v-else
            class="trigger"
            @click="() => (collapsed = !collapsed)"
          />
        </a-layout-header>
        <a-layout-content style="margin: 0; background: #f0f2f5">
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  CloudServerOutlined,
  AppstoreOutlined,
  KeyOutlined,
} from '@ant-design/icons-vue';

const router = useRouter();
const route = useRoute();

const collapsed = ref(false);
const selectedKeys = ref(['/']);

const handleMenuClick = ({ key }) => {
  router.push(key);
};

// Watch route changes to update selected menu item
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/models') {
      selectedKeys.value = ['/models'];
    } else if (newPath === '/tokens') {
      selectedKeys.value = ['/tokens'];
    } else {
      selectedKeys.value = ['/'];
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
}

.trigger {
  font-size: 18px;
  line-height: 64px;
  padding: 0 24px;
  cursor: pointer;
  transition: color 0.3s;
}

.trigger:hover {
  color: #1890ff;
}
</style>
