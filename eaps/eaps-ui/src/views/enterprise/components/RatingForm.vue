<template>
  <div class="rating-form">
    <el-form 
      ref="ratingFormRef" 
      :model="ratingForm" 
      :rules="ratingRules" 
      label-width="100px"
    >
      <el-form-item label="总体评分" prop="rating">
        <el-rate 
          v-model="ratingForm.rating" 
          :colors="rateColors"
          :max="5"
          :texts="rateTexts"
          show-text
        />
      </el-form-item>
      
      <el-form-item label="薪资福利" prop="salarySatisfaction">
        <el-rate 
          v-model="ratingForm.salarySatisfaction" 
          :colors="rateColors" 
          :max="5"
        />
      </el-form-item>
      
      <el-form-item label="工作环境" prop="workEnvironment">
        <el-rate 
          v-model="ratingForm.workEnvironment" 
          :colors="rateColors" 
          :max="5"
        />
      </el-form-item>
      
      <el-form-item label="晋升机会" prop="promotionOpportunity">
        <el-rate 
          v-model="ratingForm.promotionOpportunity" 
          :colors="rateColors" 
          :max="5"
        />
      </el-form-item>
      
      <el-form-item label="工作压力" prop="workPressure">
        <el-rate 
          v-model="ratingForm.workPressure" 
          :colors="rateColors" 
          :max="5"
        />
      </el-form-item>
      
      <el-form-item label="评价标签" prop="tags">
        <el-select
          v-model="ratingForm.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或添加标签"
        >
          <el-option
            v-for="tag in tagOptions"
            :key="tag.value"
            :label="tag.label"
            :value="tag.value"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="评价内容" prop="content">
        <el-input
          v-model="ratingForm.content"
          type="textarea"
          rows="5"
          placeholder="请输入您对该企业的评价"
        />
      </el-form-item>
      
      <el-form-item label="匿名评价" prop="isAnonymous">
        <el-switch v-model="ratingForm.isAnonymous" />
      </el-form-item>
      
      <el-form-item label="上传图片" prop="images">
        <el-upload
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          :on-change="handleImageChange"
          :on-remove="handleImageRemove"
          :file-list="imageList"
          :limit="5"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交评分</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, defineProps, defineEmits } from 'vue';
import { Plus } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { submitRating } from '@/api/rating';

const props = defineProps({
  enterpriseId: {
    type: [String, Number],
    required: true
  }
});

const emit = defineEmits(['success']);

// 评分表单引用
const ratingFormRef = ref(null);

// 评分颜色
const rateColors = ['#F56C6C', '#E6A23C', '#909399', '#67C23A', '#409EFF'];

// 评分文本
const rateTexts = ['很差', '一般', '满意', '良好', '优秀'];

// 标签选项
const tagOptions = [
  { value: '环境优美', label: '环境优美' },
  { value: '福利好', label: '福利好' },
  { value: '技术强', label: '技术强' },
  { value: '加班多', label: '加班多' },
  { value: '管理规范', label: '管理规范' },
  { value: '团队氛围好', label: '团队氛围好' },
  { value: '发展空间大', label: '发展空间大' },
  { value: '企业文化好', label: '企业文化好' },
  { value: '压力大', label: '压力大' }
];

// 图片列表
const imageList = ref([]);

// 评分表单
const ratingForm = reactive({
  rating: 0,
  salarySatisfaction: 0,
  workEnvironment: 0,
  promotionOpportunity: 0,
  workPressure: 0,
  content: '',
  tags: [],
  isAnonymous: false,
  images: []
});

// 表单验证规则
const ratingRules = {
  rating: [
    { required: true, message: '请给予总体评分', trigger: 'change' },
    { type: 'number', min: 1, message: '评分不能为0', trigger: 'change' }
  ],
  salarySatisfaction: [
    { required: true, message: '请评价薪资福利', trigger: 'change' }
  ],
  workEnvironment: [
    { required: true, message: '请评价工作环境', trigger: 'change' }
  ],
  promotionOpportunity: [
    { required: true, message: '请评价晋升机会', trigger: 'change' }
  ],
  workPressure: [
    { required: true, message: '请评价工作压力', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, max: 500, message: '评价内容长度在10-500字符之间', trigger: 'blur' }
  ]
};

// 处理图片上传
const handleImageChange = (file, fileList) => {
  imageList.value = fileList;
  // 将文件对象转换为可以上传的格式
  const reader = new FileReader();
  reader.readAsDataURL(file.raw);
  reader.onload = () => {
    // 更新表单中的图片列表
    ratingForm.images = imageList.value.map(item => {
      if (item.url) {
        return item.url;
      } else if (item.raw) {
        return reader.result;
      }
      return '';
    }).filter(url => url);
  };
};

// 处理图片移除
const handleImageRemove = (file, fileList) => {
  imageList.value = fileList;
  ratingForm.images = fileList.map(item => item.url || '').filter(url => url);
};

// 提交表单
const submitForm = () => {
  ratingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = {
          ...ratingForm,
          enterpriseId: props.enterpriseId
        };
        
        await submitRating(formData);
        ElMessage.success('评分提交成功，等待审核');
        emit('success');
        resetForm();
      } catch (error) {
        console.error('提交评分失败', error);
        ElMessage.error('提交评分失败，请重试');
      }
    } else {
      ElMessage.warning('表单验证未通过，请检查输入');
      return false;
    }
  });
};

// 重置表单
const resetForm = () => {
  ratingFormRef.value.resetFields();
  imageList.value = [];
  ratingForm.images = [];
};
</script>

<style scoped>
.rating-form {
  max-width: 700px;
  margin: 0 auto;
}

.el-rate {
  margin-top: 10px;
}

.el-upload {
  margin-top: 8px;
}
</style> 