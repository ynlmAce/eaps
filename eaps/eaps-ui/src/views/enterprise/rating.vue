<template>
  <div class="enterprise-rating-container">
    <page-header title="企业评价"></page-header>
    
    <!-- 搜索和筛选卡片 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="queryParams" inline>
        <el-form-item label="企业名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入企业名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="queryParams.industry" placeholder="请选择行业" clearable>
            <el-option
              v-for="item in industryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryParams.rating" placeholder="请选择评分" clearable>
            <el-option label="5星" :value="5" />
            <el-option label="4星以上" :value="4" />
            <el-option label="3星以上" :value="3" />
            <el-option label="2星以上" :value="2" />
            <el-option label="1星以上" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 企业评分列表 -->
    <el-card class="enterprise-list-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>企业列表</span>
        </div>
      </template>
      
      <el-empty v-if="!loading && enterpriseList.length === 0" description="暂无数据"></el-empty>
      
      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="enterprise in enterpriseList" :key="enterprise.id">
          <el-card class="enterprise-card" shadow="hover" @click="viewEnterpriseDetail(enterprise.id)">
            <div class="enterprise-logo">
              <el-image
                :src="enterprise.logo || '/enterprise-logo-placeholder.png'"
                fit="contain"
              ></el-image>
            </div>
            <div class="enterprise-info">
              <div class="enterprise-name">{{ enterprise.name }}</div>
              <div class="enterprise-industry">{{ enterprise.industry }}</div>
              <div class="enterprise-rating">
                <span class="rating-value">{{ enterprise.avgRating }}</span>
                <el-rate
                  v-model="enterprise.avgRating"
                  disabled
                  :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                  text-color="#909399"
                ></el-rate>
                <span class="rating-count">({{ enterprise.ratingCount }}条评价)</span>
              </div>
              <div class="enterprise-tags">
                <el-tag
                  v-for="(tag, index) in enterprise.tags"
                  :key="index"
                  size="small"
                  effect="plain"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[8, 16, 24, 32]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 企业详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      direction="rtl"
      size="80%"
      :title="selectedEnterprise ? selectedEnterprise.name : '企业详情'"
    >
      <template v-if="selectedEnterprise">
        <div class="enterprise-detail">
          <div class="enterprise-header">
            <div class="enterprise-logo">
              <el-image
                :src="selectedEnterprise.logo || '/enterprise-logo-placeholder.png'"
                fit="contain"
                style="width: 100px; height: 100px;"
              ></el-image>
            </div>
            <div class="enterprise-info">
              <h2>{{ selectedEnterprise.name }}</h2>
              <p>{{ selectedEnterprise.industry }} | {{ selectedEnterprise.scale }} | {{ selectedEnterprise.location }}</p>
              <div class="enterprise-rating">
                <span class="rating-value">{{ selectedEnterprise.avgRating }}</span>
                <el-rate
                  v-model="selectedEnterprise.avgRating"
                  disabled
                  :colors="['#F56C6C', '#E6A23C', '#67C23A']"
                  text-color="#909399"
                ></el-rate>
                <span class="rating-count">({{ selectedEnterprise.ratingCount }}条评价)</span>
              </div>
            </div>
            <div class="enterprise-actions">
              <el-button type="primary" @click="openRatingForm">评价企业</el-button>
              <el-button @click="viewEnterpriseFullDetail">查看详情</el-button>
            </div>
          </div>
          
          <el-divider />
          
          <el-tabs v-model="activeTab">
            <el-tab-pane label="企业评价" name="ratings">
              <rating-list :enterprise-id="selectedEnterprise.id" />
            </el-tab-pane>
            <el-tab-pane label="企业简介" name="intro">
              <div class="enterprise-intro">
                <h3>企业简介</h3>
                <p>{{ selectedEnterprise.description || '暂无简介' }}</p>
                
                <h3>企业地址</h3>
                <p>{{ selectedEnterprise.address || '暂无地址信息' }}</p>
                
                <h3>联系方式</h3>
                <p v-if="selectedEnterprise.contactEmail">邮箱：{{ selectedEnterprise.contactEmail }}</p>
                <p v-if="selectedEnterprise.contactPhone">电话：{{ selectedEnterprise.contactPhone }}</p>
                <p v-if="selectedEnterprise.website">网站：<a :href="selectedEnterprise.website" target="_blank">{{ selectedEnterprise.website }}</a></p>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>
    </el-drawer>
    
    <!-- 评分表单对话框 -->
    <el-dialog
      v-model="ratingFormVisible"
      title="企业评价"
      width="50%"
    >
      <rating-form
        v-if="selectedEnterprise"
        :enterprise-id="selectedEnterprise.id"
        @success="handleRatingSuccess"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import PageHeader from '@/components/PageHeader/index.vue';
import RatingList from './components/RatingList.vue';
import RatingForm from './components/RatingForm.vue';
import { getRatingList, getEnterpriseRatingStats } from '@/api/rating';

const router = useRouter();

// 企业列表数据
const enterpriseList = ref([]);
const loading = ref(false);
const selectedEnterprise = ref(null);
const drawerVisible = ref(false);
const ratingFormVisible = ref(false);
const activeTab = ref('ratings');

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 8,
  total: 0
});

// 查询参数
const queryParams = reactive({
  keyword: '',
  industry: '',
  rating: null
});

// 行业选项
const industryOptions = [
  { value: '互联网', label: '互联网' },
  { value: '金融', label: '金融' },
  { value: '教育', label: '教育' },
  { value: '制造业', label: '制造业' },
  { value: '医疗', label: '医疗' },
  { value: '服务业', label: '服务业' },
  { value: '房地产', label: '房地产' },
  { value: '其他', label: '其他' }
];

// 获取企业列表
const fetchEnterpriseList = async () => {
  loading.value = true;
  try {
    // 这里应该调用实际的企业列表API
    // 以下使用评分API模拟获取企业列表
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: queryParams.keyword,
      industry: queryParams.industry,
      minRating: queryParams.rating
    };
    
    // 模拟数据
    const mockData = {
      total: 50,
      list: Array.from({ length: pagination.pageSize }, (_, index) => ({
        id: index + 1,
        name: `企业${index + 1}`,
        logo: `/img/enterprise-logo-${(index % 5) + 1}.png`,
        industry: industryOptions[index % industryOptions.length].value,
        avgRating: (3 + Math.random() * 2).toFixed(1),
        ratingCount: Math.floor(Math.random() * 200) + 10,
        scale: ['小型', '中型', '大型'][index % 3],
        location: ['北京', '上海', '广州', '深圳', '杭州'][index % 5],
        tags: [
          '环境好',
          '福利好',
          '技术强',
          '发展空间大',
          '工作压力大'
        ].sort(() => Math.random() - 0.5).slice(0, Math.floor(Math.random() * 3) + 1),
        description: '这是一家专注于互联网技术研发的企业，致力于为客户提供高质量的产品和服务。',
        address: '北京市海淀区中关村软件园',
        contactEmail: 'contact@company.com',
        contactPhone: '010-12345678',
        website: 'https://www.example.com'
      }))
    };
    
    enterpriseList.value = mockData.list;
    pagination.total = mockData.total;
  } catch (error) {
    console.error('获取企业列表失败', error);
    ElMessage.error('获取企业列表失败');
  } finally {
    loading.value = false;
  }
};

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  // 在实际应用中，应该根据ID查询企业详情
  // 这里使用模拟数据
  selectedEnterprise.value = enterpriseList.value.find(item => item.id === id);
  if (selectedEnterprise.value) {
    drawerVisible.value = true;
    activeTab.value = 'ratings';
  }
};

// 查看完整企业详情页
const viewEnterpriseFullDetail = () => {
  if (selectedEnterprise.value) {
    router.push(`/enterprise/detail/${selectedEnterprise.value.id}`);
  }
};

// 打开评分表单
const openRatingForm = () => {
  ratingFormVisible.value = true;
};

// 评分成功处理
const handleRatingSuccess = () => {
  ratingFormVisible.value = false;
  activeTab.value = 'ratings';
  // 刷新评分列表
  if (selectedEnterprise.value) {
    selectedEnterprise.value.ratingCount++;
  }
};

// 搜索处理
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchEnterpriseList();
};

// 重置查询
const resetQuery = () => {
  queryParams.keyword = '';
  queryParams.industry = '';
  queryParams.rating = null;
  handleSearch();
};

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchEnterpriseList();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchEnterpriseList();
};

// 初始化
onMounted(() => {
  fetchEnterpriseList();
});
</script>

<style scoped>
.enterprise-rating-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.enterprise-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.enterprise-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  height: 100%;
}

.enterprise-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.enterprise-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.enterprise-logo .el-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
}

.enterprise-info {
  text-align: center;
}

.enterprise-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.enterprise-industry {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.enterprise-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}

.rating-value {
  font-weight: bold;
  color: #E6A23C;
  margin-right: 5px;
}

.rating-count {
  font-size: 12px;
  color: #909399;
  margin-left: 5px;
}

.enterprise-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.tag-item {
  margin: 3px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.enterprise-detail {
  padding: 20px;
}

.enterprise-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.enterprise-header .enterprise-logo {
  margin-right: 20px;
  flex-shrink: 0;
}

.enterprise-header .enterprise-info {
  flex: 1;
  text-align: left;
}

.enterprise-header .enterprise-info h2 {
  margin-top: 0;
  margin-bottom: 10px;
}

.enterprise-header .enterprise-info p {
  color: #909399;
  margin-bottom: 10px;
}

.enterprise-header .enterprise-rating {
  justify-content: flex-start;
}

.enterprise-actions {
  margin-left: 20px;
}

.enterprise-intro {
  padding: 20px;
}

.enterprise-intro h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
}

.enterprise-intro p {
  line-height: 1.6;
  margin-bottom: 15px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .enterprise-header {
    flex-direction: column;
  }
  
  .enterprise-header .enterprise-logo {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .enterprise-actions {
    margin-left: 0;
    margin-top: 15px;
  }
}
</style> 