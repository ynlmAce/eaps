<template>
  <div class="audit-container">
    <div class="audit-header">
      <h2>审核管理中心</h2>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
      <el-tab-pane name="rating" label="评价审核管理">
        <rating-index v-if="activeTab === 'rating'" />
      </el-tab-pane>
      <el-tab-pane name="appeal" label="申诉处理管理">
        <appeal-index v-if="activeTab === 'appeal'" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import RatingIndex from './rating/index.vue'
import AppealIndex from './appeal/index.vue'

const route = useRoute()
const router = useRouter()

// 默认激活标签页为评价审核管理
const activeTab = ref('rating')

// 监听路由变化，更新激活的标签页
watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab && ['rating', 'appeal'].includes(newTab)) {
      activeTab.value = newTab
    }
  },
  { immediate: true }
)

// 处理标签页变化
const handleTabChange = (tab) => {
  router.push({
    path: route.path,
    query: { 
      ...route.query,
      tab 
    }
  })
}
</script>

<style scoped>
.audit-container {
  padding: 20px;
  height: 100%;
}

.audit-header {
  margin-bottom: 20px;
}

.audit-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
</style> 