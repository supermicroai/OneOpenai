<template>
  <div class="providers-container">
    <!-- Search and Filter -->
    <div class="filter-section">
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="8">
          <a-input
            v-model:value="searchKeyword"
            placeholder="搜索提供商名称"
            allow-clear
          />
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
        <a-col :span="6">
          <a-select
            v-model:value="statusFilter"
            placeholder="选择状态"
            allow-clear
            style="width: 100%"
          >
            <a-select-option value="">全部状态</a-select-option>
            <a-select-option value="1">启用</a-select-option>
            <a-select-option value="0">禁用</a-select-option>
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

    <!-- Add Provider Button -->
    <div class="action-bar" style="margin-bottom: 16px;">
      <a-button type="primary" @click="changeRoute('/provider/new')">
        <template #icon>
          <PlusOutlined />
        </template>
        添加提供商
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
        <template v-if="column.dataIndex === 'url'">
          <a :href="record.url" target="_blank">{{ record.url }}</a>
        </template>
        <template v-else-if="column.dataIndex === 'models'">
          <div class="models">
            <span class="model" v-for="(value, key) in record.models" :key="key">
              <a-tag :color="getModelTypeColor(key)">{{ key }}</a-tag>
            </span>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'enable'">
          <a-switch 
            :checked="record.enable === 1 || record.enable === true"
            @change="(checked) => enableChange(record, checked)" 
          />
        </template>
        <template v-else-if="column.dataIndex === 'type'">
          <a-tag :color="getTypeColor(record.type)">
            {{ typeMapping[record.type] }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="showAccountModal(record)">
              账号管理
            </a-button>
            <a-button type="link" size="small" @click="showEditModal(record)">
              编辑
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- Edit Provider Modal -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑提供商"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
      :confirm-loading="editLoading"
      :width="1000"
      ok-text="保存"
      cancel-text="取消"
    >
      <a-form 
        ref="editFormRef"
        :model="currentProvider"
        :rules="editRules"
        labelAlign="left" 
        :colon="false" 
        :labelCol="{ style: { width: '100px' } }"
      >
        <a-form-item label="名称" name="name">
          <a-input v-model:value="currentProvider.name" />
        </a-form-item>
        <a-form-item label="网址" name="url">
          <a-input v-model:value="currentProvider.url" />
        </a-form-item>
        <a-form-item label="API地址" name="api">
          <a-input v-model:value="currentProvider.api" />
        </a-form-item>
        <a-form-item label="模型类型" name="type">
          <a-select v-model:value="currentProvider.type" @change="filterEditModels">
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
            <a-table 
              :columns="modelColumns" 
              :dataSource="paginatedEditModels" 
              :pagination="editModelPagination"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'name'">
                  <span>{{ record.name }}</span>
                </template>
                <template v-else-if="column.dataIndex === 'value'">
                  <a-input v-model:value="record.value" placeholder="模型别名" />
                </template>
                <template v-else-if="column.dataIndex === 'inputPrice'">
                  <a-input-number 
                    v-model:value="record.inputPrice" 
                    placeholder="输入价格"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                  />
                </template>
                <template v-else-if="column.dataIndex === 'outputPrice'">
                  <a-input-number 
                    v-model:value="record.outputPrice" 
                    placeholder="输出价格"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                  />
                </template>
                <template v-else-if="column.dataIndex === 'action'">
                  <a-button type="link" size="small" danger @click="removeEditModel(record.name)">
                    删除
                  </a-button>
                </template>
              </template>
            </a-table>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

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

    <!-- Account Management Modal -->
    <a-modal
      v-model:open="accountModalVisible"
      title="账号管理"
      :footer="null"
      @cancel="handleAccountCancel"
      :width="900"
    >
      <!-- Add Account Button -->
      <div style="margin-bottom: 16px;">
        <a-button type="primary" @click="addNewAccount">
          <template #icon>
            <PlusOutlined />
          </template>
          新增账号
        </a-button>
      </div>
      
      <div v-if="currentAccounts.length > 0">
        <a-table 
          :columns="accountColumns" 
          :data-source="currentAccounts" 
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'name'">
              <span v-if="!isEditing(record)">{{ record.name || '-' }}</span>
              <a-input 
                v-else
                v-model:value="record.name" 
                placeholder="请输入账号名称"
              />
            </template>
            <template v-else-if="column.dataIndex === 'apiKey'">
              <span v-if="!isEditing(record)">{{ maskApiKey(record.apiKey) || '-' }}</span>
              <a-input 
                v-else
                v-model:value="record.apiKey" 
                placeholder="请输入API Key"
              />
            </template>
            <template v-else-if="column.dataIndex === 'ak'">
              <span v-if="!isEditing(record)">{{ record.ak || '-' }}</span>
              <a-input 
                v-else
                v-model:value="record.ak" 
                placeholder="请输入AK"
              />
            </template>
            <template v-else-if="column.dataIndex === 'sk'">
              <span v-if="!isEditing(record)">{{ record.sk || '-' }}</span>
              <a-input 
                v-else
                v-model:value="record.sk" 
                placeholder="请输入SK"
              />
            </template>
            <template v-else-if="column.dataIndex === 'status'">
              <a-switch 
                :checked="record.status === 1 || record.status === true"
                @change="(checked) => { record.status = checked ? 1 : 0; }" 
              />
            </template>
            <template v-else-if="column.dataIndex === 'action'">
              <a-space>
                <template v-if="!isEditing(record)">
                  <a-button type="link" size="small" @click="startEdit(record)">
                    编辑
                  </a-button>
                  <a-button type="link" size="small" danger @click="removeAccount(record)">
                    删除
                  </a-button>
                </template>
                <template v-else>
                  <a-button type="link" size="small" @click="saveAccount(record)">
                    保存
                  </a-button>
                  <a-button type="link" size="small" @click="cancelEdit(record)">
                    取消
                  </a-button>
                </template>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
      <div v-else>
        <a-empty description="暂无账号配置">
          <template #extra>
            <a-button type="primary" @click="addNewAccount">
              <template #icon>
                <PlusOutlined />
              </template>
              新增账号
            </a-button>
          </template>
        </a-empty>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { getProviders, enableProvider, getProvider, updateProvider, getModels, getAccounts, updateAccount, deleteAccount } from "@/api/provider.js";
import { message, Modal } from "ant-design-vue";
import { PlusOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';

const router = useRouter();
const loading = ref(false);
const providerList = ref([]);
const filteredProviderList = ref([]);

// Search and filter states
const searchKeyword = ref('');
const typeFilter = ref('');
const statusFilter = ref('');

const changeRoute = (route) => {
  router.push(route);
};

const typeMapping = {
  llm: '文本生成',
  ocr: 'OCR',
  embedding: '向量'
};

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 160,
  },
  {
    title: '地址',
    dataIndex: 'url',
    width: 200,
  },
  {
    title: '类型',
    dataIndex: 'type',
    width: 100,
  },
  {
    title: '支持模型列表',
    dataIndex: 'models',
    width: 450,
  },
  {
    title: '是否启用',
    dataIndex: 'enable',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 150
  }
];

// Pagination
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
});

const getModelTypeColor = (modelName) => {
  const colors = ['blue', 'green', 'orange', 'purple', 'cyan', 'magenta'];
  const hash = modelName.split('').reduce((a, b) => {
    a = ((a << 5) - a) + b.charCodeAt(0);
    return a & a;
  }, 0);
  return colors[Math.abs(hash) % colors.length];
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
  let filtered = [...providerList.value];
  
  // Apply search filter
  if (searchKeyword.value.trim()) {
    filtered = filtered.filter(provider => 
      provider.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
    );
  }
  
  // Apply type filter
  if (typeFilter.value) {
    filtered = filtered.filter(provider => provider.type === typeFilter.value);
  }
  
  // Apply status filter
  if (statusFilter.value !== '') {
    const status = statusFilter.value === '1';
    filtered = filtered.filter(provider => 
      (provider.enable === 1 || provider.enable === true) === status
    );
  }
  
  filteredProviderList.value = filtered;
  pagination.value.total = filtered.length;
  pagination.value.current = 1; // Reset to first page
};

// Search and filter handlers
const handleSearch = () => {
  applyFilters();
};

const handleReset = () => {
  searchKeyword.value = '';
  typeFilter.value = '';
  statusFilter.value = '';
  applyFilters();
};

// Computed properties for pagination
const paginatedData = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filteredProviderList.value.slice(start, end);
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

const enableChange = async (record, checked) => {
  try {
    record.enable = checked;
    await enableProvider(record.id, checked);
    message.success('更新成功');
    await loadProviders();
  } catch (error) {
    message.error('更新失败: ' + error.message);
  }
};


const loadProviders = async () => {
  loading.value = true;
  try {
    const response = await getProviders();
    const result = response.data.data || [];
    
    // Parse models JSON
    result.forEach((item) => {
      if (item.models) {
        try {
          item.models = JSON.parse(item.models);
        } catch (e) {
          item.models = {};
        }
      } else {
        item.models = {};
      }
    });
    
    providerList.value = result;
    applyFilters(); // Apply current filters after loading
  } catch (error) {
    message.error('获取提供商列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// Edit Modal states
const editModalVisible = ref(false);
const editLoading = ref(false);
const editFormRef = ref();
const currentProvider = ref({
  id: null,
  name: '',
  url: '',
  api: '',
  type: '',
  models: {}
});
const modelList = ref([]);
const filteredEditModelList = ref([]);
const editModelCurrentPage = ref(1);
const editModelPageSize = ref(5);

// Add Model Modal states
const addModelModalVisible = ref(false);
const addModelLoading = ref(false);
const selectedModel = ref('');
const modelAlias = ref('');

// Account Modal states
const accountModalVisible = ref(false);
const accountLoading = ref(false);
const currentAccounts = ref([]);
const currentProviderId = ref(null);
const currentProviderCode = ref(null); // 当前提供商代码
const editingAccountIds = ref(new Set()); // 正在编辑的账号ID集合

// Edit form rules
const editRules = {
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

// Model columns for edit modal
const modelColumns = [
  {
    title: '模型名称',
    dataIndex: 'name',
    width: 120,
  },
  {
    title: '别名',
    dataIndex: 'value',
    width: 200,
  },
  {
    title: '输入价格(美元/1M)',
    dataIndex: 'inputPrice',
    width: 120,
  },
  {
    title: '输出价格(美元/1M)',
    dataIndex: 'outputPrice',
    width: 120,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 60,
  }
];

// Account columns
const accountColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    width: 120,
  },
  {
    title: 'API Key',
    dataIndex: 'apiKey',
    width: 200,
  },
  {
    title: 'AK',
    dataIndex: 'ak',
    width: 120,
  },
  {
    title: 'SK',
    dataIndex: 'sk',
    width: 120,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 120,
  }
];

// 掩码显示API Key，保留头尾
const maskApiKey = (apiKey) => {
  if (!apiKey || apiKey.length <= 8) {
    return apiKey;
  }
  const start = apiKey.substring(0, 4);
  const end = apiKey.substring(apiKey.length - 4);
  const middle = '*'.repeat(Math.min(apiKey.length - 8, 12)); // 最多显示12个星号
  return `${start}${middle}${end}`;
};

// Edit modal pagination
const editModelPagination = computed(() => ({
  current: editModelCurrentPage.value,
  pageSize: editModelPageSize.value,
  total: filteredEditModelList.value.length,
  onChange: (p, s) => {
    editModelCurrentPage.value = p;
    editModelPageSize.value = s;
  }
}));

// Paginated edit models
const paginatedEditModels = computed(() => {
  const start = (editModelCurrentPage.value - 1) * editModelPageSize.value;
  const end = start + editModelPageSize.value;
  return filteredEditModelList.value.slice(start, end);
});

// Available models for adding
const availableModelsForAdd = computed(() => {
  const currentModelNames = Object.keys(currentProvider.value.models || {});
  return modelList.value
    .filter(model => model.type === currentProvider.value.type && !currentModelNames.includes(model.name))
    .map(model => ({
      label: model.name,
      value: model.name
    }));
});

// Show edit modal
const showEditModal = async (record) => {
  try {
    editLoading.value = true;
    const response = await getProvider(record.id);
    const providerData = response.data.data;
    
    currentProvider.value = {
      ...providerData,
      models: providerData.models ? JSON.parse(providerData.models) : {}
    };
    
    // Load models
    const modelsResponse = await getModels();
    modelList.value = modelsResponse.data.data || [];
    
    filterEditModels();
    editModalVisible.value = true;
  } catch (error) {
    message.error('获取提供商信息失败: ' + error.message);
  } finally {
    editLoading.value = false;
  }
};

// Filter edit models
const filterEditModels = () => {
  const models = currentProvider.value.models || {};
  filteredEditModelList.value = Object.entries(models)
    .map(([modelName, modelConfig]) => {
      // 如果模型配置是字符串，转为对象
      if (typeof modelConfig === 'string') {
        return {
          name: modelName,
          value: modelConfig,
          inputPrice: null,
          outputPrice: null
        };
      }
      // 如果是对象，直接使用
      return {
        name: modelName,
        value: modelConfig.alias || modelConfig.value || modelName,
        inputPrice: modelConfig.inputPrice || null,
        outputPrice: modelConfig.outputPrice || null
      };
    })
    .filter(item => item.value && item.value.trim() !== '');
};

// Handle edit submit
const handleEditSubmit = async () => {
  try {
    await editFormRef.value.validate();
    editLoading.value = true;
    
    // Convert models back to the new format with price support
    const modelsObject = {};
    filteredEditModelList.value.forEach(model => {
      if (model.value && model.value.trim()) {
        modelsObject[model.name] = {
          alias: model.value,
          inputPrice: model.inputPrice || null,
          outputPrice: model.outputPrice || null
        };
      }
    });
    
    const providerToUpdate = {
      ...currentProvider.value,
      models: JSON.stringify(modelsObject)
    };
    
    await updateProvider(providerToUpdate);
    message.success('更新成功');
    editModalVisible.value = false;
    await loadProviders();
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error('更新失败: ' + error.message);
  } finally {
    editLoading.value = false;
  }
};

// Handle edit cancel
const handleEditCancel = () => {
  editModalVisible.value = false;
  currentProvider.value = {
    id: null,
    name: '',
    url: '',
    api: '',
    type: '',
    models: {}
  };
  filteredEditModelList.value = [];
};

// Show add model modal
const showAddModelModal = () => {
  if (!currentProvider.value.type) {
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
    // 使用新的对象结构支持价格配置
    const modelConfig = {
      alias: modelAlias.value.trim(),
      inputPrice: null,
      outputPrice: null
    };
    
    currentProvider.value.models[selectedModel.value] = modelConfig;
    
    // Update the filtered list
    filterEditModels();
    
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

// Remove edit model
const removeEditModel = (modelName) => {
  delete currentProvider.value.models[modelName];
  filterEditModels();
  message.success('模型映射已删除');
};

// Filter option for select
const filterOption = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

// Show account modal
const showAccountModal = async (record) => {
  try {
    accountLoading.value = true;
    currentProviderId.value = record.id; // 保存当前提供商ID
    currentProviderCode.value = record.code; // 保存当前提供商代码
    const response = await getAccounts(record.id);
    currentAccounts.value = response.data.data || [];
    accountModalVisible.value = true;
  } catch (error) {
    message.error('获取账号信息失败: ' + error.message);
  } finally {
    accountLoading.value = false;
  }
};

// Handle account cancel
const handleAccountCancel = () => {
  accountModalVisible.value = false;
  currentAccounts.value = [];
  currentProviderId.value = null;
  currentProviderCode.value = null;
};

// 生成账号的唯一标识（仅用于前端状态管理）
const getAccountId = (record) => {
  if (record.id) {
    return `account_${record.id}`;
  }
  // 对于新增账号，使用providerId和一个随机数作为临时ID（仅前端使用）
  if (!record._tempId) {
    record._tempId = Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }
  return `temp_${record.providerId}_${record._tempId}`;
};

// 判断账号是否处于编辑状态
const isEditing = (record) => {
  const accountId = getAccountId(record);
  return editingAccountIds.value.has(accountId);
};

// 开始编辑账号
const startEdit = (record) => {
  const accountId = getAccountId(record);
  editingAccountIds.value.add(accountId);
  // 保存原始数据用于取消编辑时恢复
  record._originalData = { ...record };
};

// 保存账号
const saveAccount = async (record) => {
  try {
    // 验证必填字段
    if (!record.name && !record.apiKey) {
      message.warning('请至少填写账号名称或API Key');
      return;
    }
    
    // 确保providerCode存在
    if (!record.providerCode) {
      record.providerCode = currentProviderCode.value;
    }
    
    // 创建要提交的账号数据副本
    const accountToSave = { ...record };
    
    // 新增账号：确保没有ID传递给后端（强制设为undefined）
    if (!record.id) {
      delete accountToSave.id; // 完全删除id字段，而不是设为null
    }
    
    // 清除所有临时字段，避免传递给服务端
    delete accountToSave._tempId;
    delete accountToSave._originalData;
    
    console.log('提交给服务端的账号数据:', accountToSave);
    
    await updateAccount(accountToSave);
    message.success('账号信息保存成功');
    
    // 退出编辑状态
    const accountId = getAccountId(record);
    editingAccountIds.value.delete(accountId);
    
    // 清除原始数据和临时ID
    delete record._originalData;
    delete record._tempId;
    
    // 重新加载账号列表以获取服务器分配的ID
    await refreshAccounts();
  } catch (error) {
    console.error('保存账号失败:', error);
    message.error('保存失败: ' + error.message);
  }
};

// 取消编辑
const cancelEdit = (record) => {
  // 恢复原始数据
  if (record._originalData) {
    Object.assign(record, record._originalData);
    delete record._originalData;
  }
  
  // 退出编辑状态
  const accountId = getAccountId(record);
  editingAccountIds.value.delete(accountId);
  
  // 如果是新增的账号(没有真实ID)，直接从列表中移除
  if (!record.id) {
    const index = currentAccounts.value.findIndex(account => account === record);
    if (index > -1) {
      currentAccounts.value.splice(index, 1);
    }
  }
};

// 重新加载账号列表
const refreshAccounts = async () => {
  if (!currentProviderId.value) return;
  
  try {
    const response = await getAccounts(currentProviderId.value);
    currentAccounts.value = response.data.data || [];
  } catch (error) {
    console.error('刷新账号列表失败:', error);
  }
};

// Add new account
const addNewAccount = () => {
  if (!currentProviderId.value || !currentProviderCode.value) {
    message.error('无法获取提供商信息');
    return;
  }
  
  const newAccount = {
    id: null, // 新增账号暂时没有ID
    providerCode: currentProviderCode.value, // 关联提供商代码
    name: '',
    apiKey: '',
    ak: '',
    sk: '',
    status: 1, // 默认启用
    cost: 0,
    balance: 0,
    _tempId: Date.now() + '_' + Math.random().toString(36).substr(2, 9) // 临时ID
  };
  currentAccounts.value.push(newAccount);
  
  // 新增的账号自动进入编辑状态
  nextTick(() => {
    startEdit(newAccount);
  });
  
  message.success('已添加新账号，请填写相关信息');
};

// Remove account
const removeAccount = (accountToRemove) => {
  const accountName = accountToRemove.name || accountToRemove.apiKey || '未命名账号';
  const isNewAccount = !accountToRemove.id;
  
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除账号 "${accountName}" 吗？${isNewAccount ? '' : '此操作不可恢复。'}`,
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        // 如果是已保存的账号(有ID)，需要调用API删除
        if (accountToRemove.id) {
          await deleteAccount(accountToRemove.id);
          message.success('账号已删除');
          // 重新加载账号列表
          await refreshAccounts();
          return;
        }
        
        // 如果是新增的账号(没有ID)，直接从列表中移除
        const index = currentAccounts.value.findIndex(account => account === accountToRemove);
        if (index > -1) {
          // 如果正在编辑，先退出编辑状态
          const accountId = getAccountId(accountToRemove);
          editingAccountIds.value.delete(accountId);
          
          currentAccounts.value.splice(index, 1);
          message.success('账号已删除');
        }
      } catch (error) {
        message.error('删除失败: ' + error.message);
      }
    }
  });
};

onMounted(() => {
  loadProviders();
});
</script>

<style scoped>
.providers-container {
  padding: 24px;
  background: white;
  margin: 16px;
  border-radius: 8px;
}

.filter-section {
  margin-bottom: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.models {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.model {
  cursor: pointer;
  margin-bottom: 5px;
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
