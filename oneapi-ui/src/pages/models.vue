<template>
  <div class="models-container">
    <!-- Search and Filter -->
    <div class="filter-section">
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="8">
          <a-input
            v-model:value="searchKeyword"
            placeholder="搜索模型名称"
            allow-clear
          />
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="vendorFilter"
            placeholder="选择厂商"
            allow-clear
            style="width: 100%"
          >
            <a-select-option value="">全部厂商</a-select-option>
            <a-select-option value="OpenAI">OpenAI</a-select-option>
            <a-select-option value="Qwen">Qwen</a-select-option>
            <a-select-option value="Claude">Claude</a-select-option>
            <a-select-option value="Baidu">Baidu</a-select-option>
            <a-select-option value="DeepSeek">DeepSeek</a-select-option>
            <a-select-option value="Meta">Meta</a-select-option>
            <a-select-option value="Zhipu">Zhipu</a-select-option>
            <a-select-option value="Other">其他</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="typeFilter"
            placeholder="选择类型"
            allow-clear
            style="width: 100%"
          >
            <a-select-option value="">全部类型</a-select-option>
            <a-select-option value="llm">文本生成</a-select-option>
            <a-select-option value="embedding">向量</a-select-option>
            <a-select-option value="ocr">OCR</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="4" :push="0">
          <div style="display: flex; gap: 8px; justify-content: flex-end;">
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- Add Model Button -->
    <div class="action-bar" style="margin-bottom: 16px;">
      <a-button type="primary" @click="showAddModal">
        <template #icon>
          <PlusOutlined />
        </template>
        添加模型
      </a-button>
    </div>
    
    <a-table 
      :columns="columns" 
      :data-source="paginatedData" 
      :pagination="paginationConfig"
      :loading="loading"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'type'">
          <a-tag :color="getTypeColor(record.type)">
            {{ typeMapping[record.type] }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'enabled'">
          <a-switch 
            :checked="record.enable === 1 || record.enable === true"
            @change="(checked) => toggleModel(record, checked)"
          />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="editModel(record)">
              编辑
            </a-button>
            <a-popconfirm
              title="确定要删除这个模型吗？"
              @confirm="deleteModel(record.id)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" size="small" danger>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Add/Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑模型' : '添加模型'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
    >
      <a-form
        ref="formRef"
        :model="currentModel"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="模型名称" name="name">
          <a-input v-model:value="currentModel.name" placeholder="请输入模型名称" />
        </a-form-item>
        <a-form-item label="厂商" name="vendor">
          <a-select v-model:value="currentModel.vendor" placeholder="请选择厂商">
            <a-select-option value="OpenAI">OpenAI</a-select-option>
            <a-select-option value="Qwen">Qwen</a-select-option>
            <a-select-option value="Claude">Claude</a-select-option>
            <a-select-option value="Baidu">Baidu</a-select-option>
            <a-select-option value="DeepSeek">DeepSeek</a-select-option>
            <a-select-option value="Zhipu">Zhipu</a-select-option>
            <a-select-option value="Other">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模型类型" name="type">
          <a-select v-model:value="currentModel.type" placeholder="请选择模型类型">
            <a-select-option value="llm">文本生成</a-select-option>
            <a-select-option value="embedding">向量</a-select-option>
            <a-select-option value="ocr">OCR</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea 
            v-model:value="currentModel.description" 
            placeholder="请输入模型描述"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="状态" name="enabled">
          <a-radio-group v-model:value="currentModel.enabled">
            <a-radio :value="true">可用</a-radio>
            <a-radio :value="false">不可用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
import { 
  getModels, 
  addModel, 
  updateModel, 
  deleteModel as deleteModelApi,
  toggleModel as toggleModelApi 
} from '@/api/model';

const loading = ref(false);
const modelList = ref([]);
const filteredModelList = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref();

// Search and filter states
const searchKeyword = ref('');
const vendorFilter = ref('');
const typeFilter = ref('');

const currentModel = reactive({
  id: null,
  name: '',
  vendor: '',
  type: '',
  description: '',
  enabled: true
});

const typeMapping = {
  llm: '文本生成',
  embedding: '向量',
  ocr: 'OCR'
};

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '模型名称',
    dataIndex: 'name',
    width: 180,
  },
  {
    title: '厂商',
    dataIndex: 'vendor',
    width: 120,
  },
  {
    title: '类型',
    dataIndex: 'type',
    width: 100,
  },
  {
    title: '描述',
    dataIndex: 'description',
    ellipsis: true,
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    width: 80,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 150,
  }
];

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
});

const rules = {
  name: [
    { required: true, message: '请输入模型名称', trigger: 'blur' },
    { min: 1, max: 100, message: '模型名称长度应为1-100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择模型类型', trigger: 'change' }
  ]
};

const getTypeColor = (type) => {
  const colors = {
    llm: 'blue',
    embedding: 'green',
    ocr: 'orange'
  };
  return colors[type] || 'default';
};

// Filter and search logic
const applyFilters = () => {
  let filtered = [...modelList.value];
  
  // Apply search filter
  if (searchKeyword.value.trim()) {
    filtered = filtered.filter(model => 
      model.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    );
  }
  
  // Apply vendor filter
  if (vendorFilter.value) {
    filtered = filtered.filter(model => model.vendor === vendorFilter.value);
  }
  
  // Apply type filter
  if (typeFilter.value) {
    filtered = filtered.filter(model => model.type === typeFilter.value);
  }
  
  filteredModelList.value = filtered;
  pagination.value.total = filtered.length;
  pagination.value.current = 1; // Reset to first page
};

// Search and filter handlers
const handleSearch = () => {
  applyFilters();
};

const handleVendorFilter = () => {
  applyFilters();
};

const handleTypeFilter = () => {
  applyFilters();
};

const handleReset = () => {
  searchKeyword.value = '';
  vendorFilter.value = '';
  typeFilter.value = '';
  applyFilters();
};

// Computed properties for pagination
const paginatedData = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filteredModelList.value.slice(start, end);
});

const paginationConfig = computed(() => ({
  current: pagination.value.current,
  pageSize: pagination.value.pageSize,
  total: pagination.value.total,
  showSizeChanger: pagination.value.showSizeChanger,
  showQuickJumper: pagination.value.showQuickJumper,
  showTotal: pagination.value.showTotal,
  onChange: (page, pageSize) => {
    pagination.value.current = page;
    pagination.value.pageSize = pageSize;
  },
  onShowSizeChange: (current, size) => {
    pagination.value.current = 1;
    pagination.value.pageSize = size;
  }
}));

const loadModels = async () => {
  loading.value = true;
  try {
    const response = await getModels();
    modelList.value = response.data.data || [];
    applyFilters(); // Apply current filters after loading
  } catch (error) {
    message.error('获取模型列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

const showAddModal = () => {
  isEdit.value = false;
  modalVisible.value = true;
  resetForm();
};

const editModel = (model) => {
  isEdit.value = true;
  modalVisible.value = true;
  Object.assign(currentModel, model);
};

const resetForm = () => {
  Object.assign(currentModel, {
    id: null,
    name: '',
    vendor: '',
    type: '',
    description: '',
    enabled: true
  });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    
    if (isEdit.value) {
      await updateModel(currentModel);
      message.success('模型更新成功');
    } else {
      await addModel(currentModel);
      message.success('模型添加成功');
    }
    
    modalVisible.value = false;
    await loadModels();
  } catch (error) {
    if (error.errorFields) {
      // Form validation error
      return;
    }
    message.error('操作失败：' + error.message);
  } finally {
    submitLoading.value = false;
  }
};

const handleCancel = () => {
  modalVisible.value = false;
  resetForm();
};

const deleteModel = async (modelId) => {
  try {
    await deleteModelApi(modelId);
    message.success('模型删除成功');
    await loadModels();
  } catch (error) {
    message.error('删除失败：' + error.message);
  }
};

const toggleModel = async (model, checked) => {
  try {
    const newStatus = checked ? 1 : 0;
    await toggleModelApi(model.id, newStatus);
    message.success('状态更新成功');
    await loadModels();
  } catch (error) {
    message.error('状态更新失败：' + error.message);
  }
};

onMounted(() => {
  loadModels();
});
</script>

<style scoped>
.models-container {
  padding: 24px;
  background: white;
  margin: 16px;
  border-radius: 8px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h2 {
  margin: 0;
  color: #262626;
}
</style>
