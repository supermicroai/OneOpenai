<template>
  <div class="provider-form">
    <a-form 
      ref="formRef"
      :model="provider"
      :rules="formRules"
      labelAlign="left" 
      :colon="false" 
      :labelCol="{ style: { width: '150px' } }"
    >
      <a-form-item label="代码" name="code">
        <a-input 
          v-model:value="provider.code" 
          :disabled="providerId !== 'new'"
          :placeholder="providerId === 'new' ? '请输入提供商代码' : '系统自动生成'"
        />
      </a-form-item>
      <a-form-item label="名称" name="name">
        <a-input v-model:value="provider.name" />
      </a-form-item>
      <a-form-item label="网址" name="url">
        <a-input v-model:value="provider.url" />
      </a-form-item>
      <a-form-item label="API地址" name="api">
        <a-input v-model:value="provider.api" />
      </a-form-item>
      <a-form-item label="模型类型" name="type">
        <a-select v-model:value="provider.type" @change="filterModels">
          <a-select-option value="embedding">向量</a-select-option>
          <a-select-option value="llm">文本生成</a-select-option>
          <a-select-option value="ocr">OCR</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="模型列表">
        <div class="model-list-container">
          <div class="model-list-header">
            <a-button type="primary" @click="showAddModelModal" style="margin-bottom: 16px;">
              <template #icon>
                <PlusOutlined />
              </template>
              添加模型映射
            </a-button>
          </div>
          <a-table :columns="columns" :dataSource="paginatedData" :pagination="pagination">
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'name'">
                <span>{{ record.name }}</span>
              </template>
              <template v-else-if="column.dataIndex === 'value'">
                <a-input v-model:value="record.value" />
              </template>
              <template v-else-if="column.dataIndex === 'action'">
                <a-button type="link" danger @click="removeModel(record.name)">
                  删除
                </a-button>
              </template>
            </template>
          </a-table>
        </div>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保存</a-button>
      </a-form-item>
    </a-form>

    <!-- Add Model Modal -->
    <a-modal
      v-model:open="addModelModalVisible"
      title="添加模型映射"
      @ok="handleAddModel"
      @cancel="cancelAddModel"
      :confirm-loading="addModelLoading"
      ok-text="确定"
      cancel-text="取消"
    >
      <a-form layout="vertical">
        <a-form-item label="选择模型">
          <a-select 
            v-model:value="selectedModel" 
            placeholder="请选择要添加的模型"
            :options="availableModelsForAdd"
            show-search
            :filter-option="filterOption"
          />
        </a-form-item>
        <a-form-item label="模型别名">
          <a-input v-model:value="modelAlias" placeholder="请输入模型别名" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { getModels, getProvider, updateProvider } from '@/api/provider';

const route = useRoute();
const providerId = route.params.providerId;
const formRef = ref();

// 表单验证规则
const formRules = computed(() => {
  const rules = {
    name: [
      { required: true, message: '请输入提供商名称', trigger: 'blur' }
    ],
    api: [
      { required: true, message: '请输入API地址', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择模型类型', trigger: 'change' }
    ]
  };
  
  // 新增时code字段为必填
  if (providerId === 'new') {
    rules.code = [
      { required: true, message: '请输入提供商代码', trigger: 'blur' },
      { 
        pattern: /^[a-zA-Z0-9_-]+$/, 
        message: '代码只能包含字母、数字、下划线和短横线', 
        trigger: 'blur' 
      },
      {
        min: 3,
        max: 32,
        message: '代码长度必须在3-32个字符之间',
        trigger: 'blur'
      }
    ];
  }
  
  return rules;
});

const provider = ref({
  id: -1,
  code: '',
  type: '',
  name: '',
  url: '',
  api: '',
  models: {}
});

const modelList = ref([]);
const filteredModelList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

// Add model modal related variables
const addModelModalVisible = ref(false);
const addModelLoading = ref(false);
const selectedModel = ref('');
const modelAlias = ref('');

const columns = [
  {
    title: '模型名称',
    dataIndex: 'name',
    width: 150,
  },
  {
    title: '别名',
    dataIndex: 'value',
    width: 200,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 100,
  }
];

const pagination = computed(() => {return {
  current: currentPage.value,
  pageSize: pageSize.value,
  total: filteredModelList.value.length,
  onChange: (p, s) => {
    currentPage.value = p;
    pageSize.value = s;
  }
}});

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredModelList.value.slice(start, end);
});

const loadProvider = async () => {
  try {
    const res = await getProvider(providerId);
    const item = provider.value = res.data.data;
    if (item && item.models) {
      try {
        provider.value.models = item.models ? JSON.parse(item.models) : {};
      } catch (error) {
        provider.value.models = {};
        console.error('Error parsing models:', error);
      }
    }
    // Use the original getModels API from provider.js
    const response = await getModels();
    const list = response.data.data;
    modelList.value = list ? list.sort((a, b) => a.name.localeCompare(b.name)) : [];
    filterModels();
  } catch (error) {
    console.error('Error fetching models:', error);
  }
};

onMounted(() => {
  loadProvider();
});

const filterModels = () => {
  // Only show models that have been configured (have values)
  filteredModelList.value = Object.entries(provider.value.models || {})
      .map(([modelName, modelAlias]) => ({
        name: modelName,
        value: modelAlias
      }))
      .filter(item => item.value && item.value.trim() !== '');
};

// Computed property for available models to add
const availableModelsForAdd = computed(() => {
  const currentModelNames = Object.keys(provider.value.models || {});
  return modelList.value
    .filter(model => model.type === provider.value.type && !currentModelNames.includes(model.name))
    .map(model => ({
      label: model.name,
      value: model.name
    }));
});

// Show add model modal
const showAddModelModal = () => {
  if (!provider.value.type) {
    message.warning('请先选择模型类型');
    return;
  }
  addModelModalVisible.value = true;
  selectedModel.value = '';
  modelAlias.value = '';
};

// Cancel add model
const cancelAddModel = () => {
  addModelModalVisible.value = false;
  selectedModel.value = '';
  modelAlias.value = '';
};

// Handle add model
const handleAddModel = async () => {
  if (!selectedModel.value) {
    message.warning('请选择模型');
    return;
  }
  if (!modelAlias.value.trim()) {
    message.warning('请输入模型别名');
    return;
  }

  addModelLoading.value = true;
  try {
    // Add the model to the provider's models
    provider.value.models[selectedModel.value] = modelAlias.value.trim();
    
    // Update the filtered list
    filterModels();
    
    // Close modal
    addModelModalVisible.value = false;
    selectedModel.value = '';
    modelAlias.value = '';
    
    message.success('模型映射添加成功');
  } catch (error) {
    message.error('添加失败: ' + error.message);
  } finally {
    addModelLoading.value = false;
  }
};

// Remove model
const removeModel = (modelName) => {
  delete provider.value.models[modelName];
  filterModels();
  message.success('模型映射已删除');
};

// Filter option for select
const filterOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

const handleSubmit = async (e) => {
  e.preventDefault();
  try {
    // 验证表单
    await formRef.value.validate();
    
    const list = filteredModelList.value
        .filter(item => item.value.trim() !== '')
        .map(item => [item.name, item.value]);
    provider.value.models = Object.fromEntries(list);
    
    await updateProvider(provider.value);
    message.success('更新成功');
    loadProvider();
  } catch (error) {
    if (error.errorFields) {
      // 表单验证错误，不显示错误消息
      return;
    }
    message.error('更新失败:' + error.message);
  }
};
</script>

<style scoped>
.provider-form {
  display: flex;
  flex-direction: column;
  width: calc(100%);
  height: calc(100vh - 20px);
  padding: 20px;
  background-color: white;
  overflow-y: auto;
}
</style>
