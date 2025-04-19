<template>
  <div class="ratings-management-container">
    <div class="page-header">
      <h2 class="page-title">企业评价管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待审核评价" name="pending_ratings">
        <RatingsTable 
          v-if="activeTab === 'pending_ratings'" 
          type="pending_ratings" 
          title="待审核评价"
        />
      </el-tab-pane>
      <el-tab-pane label="待处理申诉" name="pending_appeals">
        <RatingsTable 
          v-if="activeTab === 'pending_appeals'" 
          type="pending_appeals" 
          title="待处理申诉"
        />
      </el-tab-pane>
      <el-tab-pane label="已处理评价" name="processed_ratings">
        <RatingsTable 
          v-if="activeTab === 'processed_ratings'" 
          type="processed_ratings" 
          title="已处理评价"
        />
      </el-tab-pane>
      <el-tab-pane label="已处理申诉" name="processed_appeals">
        <RatingsTable 
          v-if="activeTab === 'processed_appeals'" 
          type="processed_appeals" 
          title="已处理申诉"
        />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import RatingsTable from './components/RatingsTable.vue'
import { getRatingStatistics } from '@/api/rating'
import { ElMessage } from 'element-plus'

// 当前激活的标签页
const activeTab = ref('pending_ratings')

// 统计数据
const statistics = ref({
  pendingRatingsCount: 0,
  pendingAppealsCount: 0,
  processedRatingsCount: 0,
  processedAppealsCount: 0
})

// 处理标签页切换
const handleTabChange = (tab) => {
  activeTab.value = tab
}

// 获取评价统计数据
const fetchStatistics = async () => {
  try {
    const response = await getRatingStatistics()
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取评价统计数据失败：', error)
    ElMessage.error('获取评价统计数据失败')
  }
}

// 组件挂载时获取统计数据
fetchStatistics()
</script>

<style scoped>
.ratings-management-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
</style> 