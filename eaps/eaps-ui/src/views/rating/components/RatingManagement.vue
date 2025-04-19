<template>
  <div class="rating-management">
    <!-- 筛选面板 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="queryParams" inline @keyup.enter="handleSearch">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待回复" value="pending" />
            <el-option label="已回复" value="replied" />
            <el-option label="已申诉" value="appealed" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryParams.rating" placeholder="选择评分" clearable>
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索评价内容" 
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评分列表 -->
    <el-card class="rating-list-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>评价列表</span>
          <div class="statistics">
            <span class="stat-item">总计: {{ pagination.total }}条</span>
            <span class="stat-item">待回复: {{ statistics.pendingCount || 0 }}条</span>
            <span class="stat-item">已申诉: {{ statistics.appealedCount || 0 }}条</span>
          </div>
        </div>
      </template>

      <el-table :data="ratingList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" width="150">
          <template #default="scope">
            <div class="user-info">
              <el-avatar 
                :size="32" 
                :src="scope.row.userAvatar || '/avatar-placeholder.png'"
              />
              <span>{{ scope.row.isAnonymous ? '匿名用户' : scope.row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="scope">
            <el-rate 
              v-model="scope.row.rating" 
              disabled 
              :colors="rateColors"
              text-color="#909399"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)"
              effect="plain"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              link 
              type="primary" 
              @click="viewRatingDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-if="!scope.row.reply && scope.row.status !== 'appealed'"
              link 
              type="success" 
              @click="handleReply(scope.row)"
            >
              回复
            </el-button>
            <el-button 
              v-if="scope.row.status !== 'appealed' && scope.row.rating <= 3"
              link 
              type="warning" 
              @click="handleAppeal(scope.row)"
            >
              申诉
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评分详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="评价详情"
      direction="rtl"
      size="50%"
    >
      <div v-if="selectedRating" class="rating-detail">
        <div class="detail-header">
          <div class="user-info">
            <el-avatar 
              :size="50" 
              :src="selectedRating.userAvatar || '/avatar-placeholder.png'"
            />
            <div class="user-meta">
              <div class="user-name">{{ selectedRating.isAnonymous ? '匿名用户' : selectedRating.userName }}</div>
              <div class="rating-time">{{ formatDate(selectedRating.createTime) }}</div>
            </div>
          </div>
          <div class="rating-score">
            <el-rate
              v-model="selectedRating.rating"
              disabled
              :colors="rateColors"
              text-color="#909399"
            />
          </div>
        </div>

        <el-divider />

        <div class="detail-content">
          <h3>评价内容</h3>
          <p>{{ selectedRating.content }}</p>

          <div v-if="selectedRating.images && selectedRating.images.length > 0" class="rating-images">
            <h4>评价图片</h4>
            <div class="image-list">
              <el-image 
                v-for="(image, index) in selectedRating.images"
                :key="index"
                :src="image"
                :preview-src-list="selectedRating.images"
                :initial-index="index"
                fit="cover"
                class="image-item"
              />
            </div>
          </div>

          <div v-if="selectedRating.tags && selectedRating.tags.length > 0" class="rating-tags">
            <h4>评价标签</h4>
            <div>
              <el-tag
                v-for="(tag, index) in selectedRating.tags"
                :key="index"
                size="small"
                effect="plain"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div v-if="selectedRating.reply" class="rating-reply">
            <h4>企业回复</h4>
            <div class="reply-content">
              {{ selectedRating.reply }}
              <div class="reply-time">{{ formatDate(selectedRating.replyTime) }}</div>
            </div>
          </div>

          <div v-if="selectedRating.appeal" class="rating-appeal">
            <h4>申诉信息</h4>
            <div class="appeal-reason">
              <strong>申诉理由：</strong>
              <span>{{ selectedRating.appeal.reason }}</span>
            </div>
            <div class="appeal-content">
              <strong>申诉详情：</strong>
              <p>{{ selectedRating.appeal.content }}</p>
            </div>
            <div v-if="selectedRating.appeal.evidence && selectedRating.appeal.evidence.length > 0" class="appeal-evidence">
              <strong>申诉证据：</strong>
              <div class="evidence-list">
                <el-image 
                  v-for="(evidence, index) in selectedRating.appeal.evidence"
                  :key="index"
                  :src="evidence"
                  :preview-src-list="selectedRating.appeal.evidence"
                  :initial-index="index"
                  fit="cover"
                  class="evidence-item"
                />
              </div>
            </div>
            <div class="appeal-status">
              <strong>申诉状态：</strong>
              <el-tag :type="getAppealStatusType(selectedRating.appeal.status)">
                {{ getAppealStatusText(selectedRating.appeal.status) }}
              </el-tag>
            </div>
            <div v-if="selectedRating.appeal.result" class="appeal-result">
              <strong>处理结果：</strong>
              <p>{{ selectedRating.appeal.result }}</p>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <el-button @click="drawerVisible = false">关闭</el-button>
          <el-button 
            v-if="!selectedRating.reply && selectedRating.status !== 'appealed'"
            type="primary" 
            @click="handleReply(selectedRating)"
          >
            回复评价
          </el-button>
          <el-button 
            v-if="selectedRating.status !== 'appealed' && selectedRating.rating <= 3"
            type="warning" 
            @click="handleAppeal(selectedRating)"
          >
            申诉评价
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 回复评价对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评价"
      width="500px"
    >
      <el-form 
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyRules"
        label-width="100px"
      >
        <el-form-item label="回复内容" prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReply" :loading="submitting">提交回复</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 申诉评价对话框 -->
    <el-dialog
      v-model="appealDialogVisible"
      title="申诉评价"
      width="600px"
    >
      <el-form 
        ref="appealFormRef"
        :model="appealForm"
        :rules="appealRules"
        label-width="100px"
      >
        <el-form-item label="申诉理由" prop="reason">
          <el-select v-model="appealForm.reason" placeholder="请选择申诉理由" style="width: 100%">
            <el-option label="评价与事实不符" value="不符事实" />
            <el-option label="评价内容不实/虚假" value="信息虚假" />
            <el-option label="评价内容侮辱/诽谤" value="侮辱诽谤" />
            <el-option label="评价内容违法/违规" value="违法违规" />
            <el-option label="非本人体验" value="非真实体验" />
            <el-option label="其他原因" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉详情" prop="content">
          <el-input
            v-model="appealForm.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述申诉原因和详情"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传证据">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-change="handleEvidenceChange"
            :on-remove="handleEvidenceRemove"
            :file-list="evidenceFileList"
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">
            支持JPG、PNG、PDF格式，单个文件不超过5MB，最多上传5个文件
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="appealDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAppeal" :loading="submitting">提交申诉</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { getRatingList, replyRating, appealRating } from '@/api/rating';

// 数据加载状态
const loading = ref(false);
const submitting = ref(false);

// 评分列表数据
const ratingList = ref([]);
const selectedRating = ref(null);

// 统计数据
const statistics = reactive({
  pendingCount: 0,
  appealedCount: 0
});

// 分页参数
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 查询参数
const queryParams = reactive({
  status: '',
  rating: null,
  keyword: ''
});

// 评分颜色
const rateColors = ['#F56C6C', '#E6A23C', '#909399', '#67C23A', '#409EFF'];

// 抽屉和对话框可见性
const drawerVisible = ref(false);
const replyDialogVisible = ref(false);
const appealDialogVisible = ref(false);

// 回复表单
const replyFormRef = ref(null);
const replyForm = reactive({
  ratingId: null,
  content: ''
});

// 回复表单验证规则
const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 5, max: 500, message: '回复内容长度在5-500个字符之间', trigger: 'blur' }
  ]
};

// 申诉表单
const appealFormRef = ref(null);
const appealForm = reactive({
  ratingId: null,
  reason: '',
  content: '',
  evidence: []
});

// 申诉表单验证规则
const appealRules = {
  reason: [
    { required: true, message: '请选择申诉理由', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入申诉详情', trigger: 'blur' },
    { min: 10, max: 1000, message: '申诉详情长度在10-1000个字符之间', trigger: 'blur' }
  ]
};

// 证据文件列表
const evidenceFileList = ref([]);

// 获取评分列表
const fetchRatingList = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      status: queryParams.status,
      rating: queryParams.rating,
      keyword: queryParams.keyword,
      enterpriseId: 'current' // 当前企业
    };
    
    const { data } = await getRatingList(params);
    
    ratingList.value = data.list;
    pagination.total = data.total;
    
    // 更新统计数据
    statistics.pendingCount = data.statistics?.pendingCount || 0;
    statistics.appealedCount = data.statistics?.appealedCount || 0;
  } catch (error) {
    console.error('获取评分列表失败', error);
    ElMessage.error('获取评分列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    pending: 'info',
    replied: 'success',
    appealed: 'warning'
  };
  return map[status] || 'info';
};

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending: '待回复',
    replied: '已回复',
    appealed: '已申诉'
  };
  return map[status] || '未知';
};

// 获取申诉状态类型
const getAppealStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  };
  return map[status] || 'info';
};

// 获取申诉状态文本
const getAppealStatusText = (status) => {
  const map = {
    pending: '待处理',
    approved: '已通过',
    rejected: '已驳回'
  };
  return map[status] || '未知';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

// 查看评分详情
const viewRatingDetail = (rating) => {
  selectedRating.value = rating;
  drawerVisible.value = true;
};

// 处理回复评价
const handleReply = (rating) => {
  selectedRating.value = rating;
  replyForm.ratingId = rating.id;
  replyForm.content = '';
  replyDialogVisible.value = true;
};

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return;
  
  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      await replyRating(replyForm.ratingId, { content: replyForm.content });
      ElMessage.success('回复成功');
      replyDialogVisible.value = false;
      
      // 更新列表和详情
      fetchRatingList();
      if (selectedRating.value && selectedRating.value.id === replyForm.ratingId) {
        selectedRating.value.reply = replyForm.content;
        selectedRating.value.replyTime = new Date().toISOString();
        selectedRating.value.status = 'replied';
      }
    } catch (error) {
      console.error('回复失败', error);
      ElMessage.error('回复失败，请重试');
    } finally {
      submitting.value = false;
    }
  });
};

// 处理申诉评价
const handleAppeal = (rating) => {
  selectedRating.value = rating;
  appealForm.ratingId = rating.id;
  appealForm.reason = '';
  appealForm.content = '';
  appealForm.evidence = [];
  evidenceFileList.value = [];
  appealDialogVisible.value = true;
};

// 处理证据图片变化
const handleEvidenceChange = (file, fileList) => {
  evidenceFileList.value = fileList;
  
  if (file.raw) {
    const reader = new FileReader();
    reader.readAsDataURL(file.raw);
    reader.onload = () => {
      // 将文件转为Base64
      const base64Data = reader.result;
      // 更新表单中的证据列表
      appealForm.evidence = evidenceFileList.value
        .map(item => {
          if (item.url) return item.url;
          if (item.raw && item._id === file._id) return base64Data;
          return null;
        })
        .filter(Boolean);
    };
  }
};

// 处理证据图片移除
const handleEvidenceRemove = (file, fileList) => {
  evidenceFileList.value = fileList;
  appealForm.evidence = fileList
    .map(item => item.url || null)
    .filter(Boolean);
};

// 提交申诉
const submitAppeal = async () => {
  if (!appealFormRef.value) return;
  
  await appealFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    if (selectedRating.value.rating > 3) {
      ElMessage.warning('只能对3星及以下的评价进行申诉');
      return;
    }
    
    if (appealForm.evidence.length === 0) {
      ElMessageBox.confirm('您未提供任何申诉证据，确定继续提交吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        submitAppealRequest();
      }).catch(() => {});
    } else {
      submitAppealRequest();
    }
  });
};

// 提交申诉请求
const submitAppealRequest = async () => {
  submitting.value = true;
  try {
    await appealRating(appealForm.ratingId, {
      reason: appealForm.reason,
      content: appealForm.content,
      evidence: appealForm.evidence
    });
    
    ElMessage.success('申诉提交成功，等待管理员审核');
    appealDialogVisible.value = false;
    
    // 更新列表和详情
    fetchRatingList();
    if (selectedRating.value && selectedRating.value.id === appealForm.ratingId) {
      selectedRating.value.status = 'appealed';
      selectedRating.value.appeal = {
        reason: appealForm.reason,
        content: appealForm.content,
        evidence: appealForm.evidence,
        status: 'pending'
      };
    }
  } catch (error) {
    console.error('申诉提交失败', error);
    ElMessage.error('申诉提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

// 搜索处理
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchRatingList();
};

// 重置查询
const resetQuery = () => {
  queryParams.status = '';
  queryParams.rating = null;
  queryParams.keyword = '';
  handleSearch();
};

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val;
  pagination.currentPage = 1;
  fetchRatingList();
};

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val;
  fetchRatingList();
};

// 初始化
onMounted(() => {
  fetchRatingList();
});
</script>

<style scoped>
.rating-management {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.rating-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics {
  display: flex;
}

.stat-item {
  margin-left: 15px;
  color: #606266;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info span {
  margin-left: 10px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.rating-detail {
  padding: 0 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.user-meta {
  margin-left: 15px;
}

.user-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.rating-time {
  font-size: 12px;
  color: #909399;
}

.detail-content {
  margin-top: 20px;
}

.detail-content h3 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.detail-content h4 {
  font-size: 14px;
  font-weight: bold;
  margin-top: 20px;
  margin-bottom: 10px;
}

.detail-content p {
  line-height: 1.6;
  margin-bottom: 15px;
}

.rating-images,
.rating-tags,
.rating-reply,
.rating-appeal {
  margin-top: 20px;
}

.image-list,
.evidence-list {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
}

.image-item,
.evidence-item {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.rating-reply {
  background-color: #f7f7f7;
  padding: 15px;
  border-radius: 4px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  text-align: right;
}

.rating-appeal {
  border: 1px solid #f0f0f0;
  padding: 15px;
  border-radius: 4px;
}

.appeal-reason,
.appeal-content,
.appeal-evidence,
.appeal-status,
.appeal-result {
  margin-bottom: 15px;
}

.appeal-content p,
.appeal-result p {
  margin-top: 5px;
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 10px;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 