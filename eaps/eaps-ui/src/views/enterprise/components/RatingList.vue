<template>
  <div class="rating-list">
    <!-- 评分统计 -->
    <el-card v-if="stats" class="rating-stats">
      <div class="stats-header">
        <div class="rating-overview">
          <div class="rating-score">
            <span class="score">{{ stats.averageRating }}</span>
            <el-rate
              v-model="stats.averageRating"
              disabled
              :colors="rateColors"
              text-color="#909399"
            />
          </div>
          <div class="rating-count">
            <span>{{ stats.totalCount }}条评价</span>
          </div>
        </div>
        <div class="rating-details">
          <div class="rating-item">
            <span class="label">薪资福利</span>
            <el-progress
              :percentage="stats.salarySatisfaction * 20"
              :format="formatPercentage"
              :color="progressColor"
              :stroke-width="10"
            />
          </div>
          <div class="rating-item">
            <span class="label">工作环境</span>
            <el-progress
              :percentage="stats.workEnvironment * 20"
              :format="formatPercentage"
              :color="progressColor"
              :stroke-width="10"
            />
          </div>
          <div class="rating-item">
            <span class="label">晋升机会</span>
            <el-progress
              :percentage="stats.promotionOpportunity * 20"
              :format="formatPercentage"
              :color="progressColor"
              :stroke-width="10"
            />
          </div>
          <div class="rating-item">
            <span class="label">工作压力</span>
            <el-progress
              :percentage="stats.workPressure * 20"
              :format="formatPercentage"
              :color="progressColor"
              :stroke-width="10"
            />
          </div>
        </div>
      </div>
      <div class="stats-tags">
        <el-tag
          v-for="tag in stats.tags"
          :key="tag.name"
          :type="getTagType(tag.count)"
          effect="light"
          class="tag-item"
        >
          {{ tag.name }} ({{ tag.count }})
        </el-tag>
      </div>
    </el-card>

    <!-- 筛选器 -->
    <div class="filter-options">
      <el-select v-model="filter.sort" placeholder="排序方式" @change="fetchRatings">
        <el-option label="最新" value="latest" />
        <el-option label="最热" value="popular" />
        <el-option label="最高评分" value="highest" />
        <el-option label="最低评分" value="lowest" />
      </el-select>
      <el-select v-model="filter.rating" placeholder="评分筛选" @change="fetchRatings" clearable>
        <el-option label="5星" :value="5" />
        <el-option label="4星" :value="4" />
        <el-option label="3星" :value="3" />
        <el-option label="2星" :value="2" />
        <el-option label="1星" :value="1" />
      </el-select>
      <el-select 
        v-model="filter.tag" 
        placeholder="标签筛选" 
        @change="fetchRatings" 
        clearable
      >
        <el-option 
          v-for="tag in stats?.tags" 
          :key="tag.name" 
          :label="tag.name" 
          :value="tag.name" 
        />
      </el-select>
    </div>

    <!-- 评分列表 -->
    <div v-loading="loading" class="ratings-container">
      <el-empty v-if="!loading && ratingList.length === 0" description="暂无评价" />
      
      <template v-else>
        <div v-for="rating in ratingList" :key="rating.id" class="rating-item">
          <el-card>
            <div class="rating-header">
              <div class="user-info">
                <el-avatar :size="40" :src="rating.userAvatar || '/avatar-placeholder.png'" />
                <div class="user-meta">
                  <div class="user-name">{{ rating.isAnonymous ? '匿名用户' : rating.userName }}</div>
                  <div class="rating-time">{{ formatDate(rating.createTime) }}</div>
                </div>
              </div>
              <div class="rating-score">
                <el-rate
                  v-model="rating.rating"
                  disabled
                  :colors="rateColors"
                  text-color="#909399"
                />
              </div>
            </div>

            <div class="rating-tags">
              <el-tag
                v-for="(tag, index) in rating.tags"
                :key="index"
                size="small"
                effect="plain"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div class="rating-content">
              {{ rating.content }}
            </div>

            <div v-if="rating.images && rating.images.length > 0" class="rating-images">
              <el-image 
                v-for="(image, index) in rating.images"
                :key="index"
                :src="image"
                :preview-src-list="rating.images"
                :initial-index="index"
                fit="cover"
                class="image-item"
              />
            </div>

            <div v-if="rating.reply" class="rating-reply">
              <div class="reply-header">
                <el-tag type="info" size="small">企业回复</el-tag>
                <span class="reply-time">{{ formatDate(rating.replyTime) }}</span>
              </div>
              <div class="reply-content">{{ rating.reply }}</div>
            </div>

            <div class="rating-actions">
              <span 
                class="action-item" 
                :class="{ active: rating.isLiked }"
                @click="toggleLike(rating)"
              >
                <el-icon><Thumb /></el-icon>
                <span>{{ rating.likeCount || 0 }}</span>
              </span>
              <span class="action-item" @click="showComments(rating)">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ rating.commentCount || 0 }}</span>
              </span>
            </div>
          </el-card>
        </div>
      </template>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineProps, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Thumb, ChatDotRound } from '@element-plus/icons-vue';
import { getRatingList, getEnterpriseRatingStats, toggleLikeRating } from '@/api/rating';

const props = defineProps({
  enterpriseId: {
    type: [String, Number],
    required: true
  }
});

// 评分数据
const ratingList = ref([]);
const stats = ref(null);
const loading = ref(false);

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 筛选条件
const filter = reactive({
  sort: 'latest',
  rating: null,
  tag: null
});

// 评分颜色
const rateColors = ['#F56C6C', '#E6A23C', '#909399', '#67C23A', '#409EFF'];

// 进度条颜色
const progressColor = computed(() => {
  return (percentage) => {
    if (percentage < 30) return '#F56C6C';
    if (percentage < 70) return '#E6A23C';
    return '#67C23A';
  };
});

// 格式化百分比为评分
const formatPercentage = (percentage) => {
  return `${(percentage / 20).toFixed(1)}`;
};

// 获取标签类型
const getTagType = (count) => {
  const types = ['', 'success', 'warning', 'danger', 'info'];
  return types[count % types.length];
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 获取评分列表
const fetchRatings = async () => {
  loading.value = true;
  try {
    const params = {
      enterpriseId: props.enterpriseId,
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      sort: filter.sort,
      ...filter.rating !== null ? { rating: filter.rating } : {},
      ...filter.tag ? { tag: filter.tag } : {}
    };
    
    const { data } = await getRatingList(params);
    ratingList.value = data.list;
    pagination.total = data.total;
  } catch (error) {
    console.error('获取评分列表失败', error);
    ElMessage.error('获取评分列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取评分统计
const fetchRatingStats = async () => {
  try {
    const { data } = await getEnterpriseRatingStats(props.enterpriseId);
    stats.value = data;
  } catch (error) {
    console.error('获取评分统计失败', error);
    ElMessage.error('获取评分统计失败');
  }
};

// 点赞/取消点赞
const toggleLike = async (rating) => {
  try {
    const like = !rating.isLiked;
    await toggleLikeRating(rating.id, like);
    rating.isLiked = like;
    rating.likeCount = like 
      ? (rating.likeCount || 0) + 1 
      : Math.max(0, (rating.likeCount || 0) - 1);
    
    ElMessage.success(like ? '点赞成功' : '已取消点赞');
  } catch (error) {
    console.error('操作失败', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 显示评论
const showComments = (rating) => {
  // 后续实现评论功能
  ElMessage.info('评论功能将在后续实现');
};

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchRatings();
};

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchRatings();
};

// 初始化
onMounted(() => {
  fetchRatingStats();
  fetchRatings();
});

</script>

<style scoped>
.rating-list {
  margin-bottom: 20px;
}

.rating-stats {
  margin-bottom: 20px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
}

.rating-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 30px;
}

.rating-score {
  display: flex;
  align-items: center;
}

.score {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 10px;
}

.rating-count {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}

.rating-details {
  flex: 1;
}

.rating-item {
  margin-bottom: 15px;
}

.label {
  display: inline-block;
  width: 80px;
  margin-right: 10px;
  text-align: right;
}

.stats-tags {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
}

.tag-item {
  margin-right: 10px;
  margin-bottom: 10px;
}

.filter-options {
  display: flex;
  margin-bottom: 20px;
}

.filter-options .el-select {
  margin-right: 15px;
  width: 150px;
}

.ratings-container {
  min-height: 200px;
}

.rating-item {
  margin-bottom: 20px;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-meta {
  margin-left: 10px;
}

.user-name {
  font-weight: bold;
}

.rating-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.rating-tags {
  margin-bottom: 15px;
}

.rating-content {
  margin-bottom: 15px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.rating-images {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.image-item {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  margin-bottom: 10px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.rating-reply {
  background-color: #f7f7f7;
  padding: 10px 15px;
  margin-bottom: 15px;
  border-radius: 4px;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.reply-time {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.reply-content {
  line-height: 1.5;
}

.rating-actions {
  display: flex;
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
}

.action-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  cursor: pointer;
  color: #909399;
}

.action-item.active {
  color: #409EFF;
}

.action-item .el-icon {
  margin-right: 5px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 