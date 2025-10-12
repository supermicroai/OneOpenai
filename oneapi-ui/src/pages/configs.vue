<template>
  <div class="models-container">
    <!-- Search and Filter -->
    <div class="filter-section">
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="8">
          <a-input
            v-model:value="searchKeyword"
            placeholder="搜索配置项"
            allow-clear
          />
        </a-col>
        <a-col :span="6">
          <a-select
            v-model:value="groupFilter"
            placeholder="选择分组"
            allow-clear
            style="width: 100%"
          >
            <a-select-option value="">全部分组</a-select-option>
            <a-select-option v-for="group in configGroups" :key="group" :value="group">{{ group }}</a-select-option>
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

    <!-- Add Config Button -->
    <div class="action-bar" style="margin-bottom: 16px;">
      <a-button type="primary" @click="showAddModal">
        <template #icon>
          <PlusOutlined />
        </template>
        添加配置
      </a-button>
    </div>

    <AutoHeightTable
      :columns="columns"
      :data-source="paginatedData"
      :pagination="paginationConfig"
      :loading="loading"
      row-key="id"
      :scroll="{ x: false, y: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'configGroup'">
          <a-tag :color="getGroupColor(record.configGroup)">
            {{ record.configGroup || '默认分组' }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'configValue'">
          <div style="max-width: 200px; word-break: break-all;">
            {{ record.configValue }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'note'">
          <div style="max-width: 250px; word-wrap: break-word; white-space: normal;">
            {{ record.note }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'status'">
          <a-switch 
            :checked="record.status === 1"
            @change="(checked) => toggleConfig(record, checked)"
          />
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="editConfig(record)">
              编辑
            </a-button>
          </a-space>
        </template>
      </template>
    </AutoHeightTable>

    <!-- Add/Edit Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑配置' : '添加配置'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      :width="600"
    >
      <a-form
        ref="formRef"
        :model="currentConfig"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="配置分组" name="configGroup">
          <a-input v-model:value="currentConfig.configGroup" placeholder="请输入配置分组" />
        </a-form-item>
        <a-form-item label="配置键" name="configKey">
          <a-input v-model:value="currentConfig.configKey" placeholder="请输入配置键" />
        </a-form-item>
        <a-form-item label="配置值" name="configValue">
          <a-textarea 
            v-model:value="currentConfig.configValue" 
            placeholder="请输入配置值"
            :rows="4"
          />
        </a-form-item>
        <a-form-item label="描述" name="note">
          <a-textarea 
            v-model:value="currentConfig.note" 
            placeholder="请输入配置描述"
            :rows="2"
          />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="currentConfig.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
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
  getConfigs, 
  updateConfig
} from '@/api/config';
import AutoHeightTable from '@/components/AutoHeightTable.vue';

const loading = ref(false);
const configList = ref([]);
const filteredConfigList = ref([]);
const modalVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref();

// Search and filter states
const searchKeyword = ref('');
const groupFilter = ref('');
const statusFilter = ref('');

const currentConfig = reactive({
  id: null,
  configGroup: '',
  configKey: '',
  configValue: '',
  note: '',
  status: 1
});

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 60,
  },
  {
    title: '配置分组',
    dataIndex: 'configGroup',
    width: 120,
  },
  {
    title: '配置键',
    dataIndex: 'configKey',
    width: 200,
  },
  {
    title: '配置值',
    dataIndex: 'configValue',
    width: 200,
  },
  {
    title: '描述',
    dataIndex: 'note',
    width: 250,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 150,
    fixed: 'right',
  }
];

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
  locale: {
    items_per_page: '条/页',
    jump_to: '跳至',
    jump_to_confirm: '确定',
    page: '页',
    prev_page: '上一页',
    next_page: '下一页',
    prev_5: '向前 5 页',
    next_5: '向后 5 页',
    prev_3: '向前 3 页',
    next_3: '向后 3 页'
  }
});

const rules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { min: 1, max: 100, message: '配置键长度应为1-100个字符', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ]
};

// 获取所有配置分组
const configGroups = computed(() => {
  const groups = [...new Set(configList.value.map(item => item.configGroup).filter(Boolean))];
  return groups;
});

const getGroupColor = (group) => {
  const colors = ['blue', 'green', 'orange', 'purple', 'cyan', 'magenta'];
  if (!group) return 'default';
  const hash = group.split('').reduce((a, b) => {
    a = ((a << 5) - a) + b.charCodeAt(0);
    return a & a;
  }, 0);
  return colors[Math.abs(hash) % colors.length];
};

// Filter and search logic
const applyFilters = () => {
  let filtered = [...configList.value];
  
  // Apply search filter
  if (searchKeyword.value.trim()) {
    filtered = filtered.filter(config => 
      config.configKey.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      config.configValue.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      (config.note && config.note.toLowerCase().includes(searchKeyword.value.toLowerCase()))
    );
  }
  
  // Apply group filter
  if (groupFilter.value) {
    filtered = filtered.filter(config => config.configGroup === groupFilter.value);
  }
  
  // Apply status filter
  if (statusFilter.value !== '') {
    filtered = filtered.filter(config => config.status == statusFilter.value);
  }
  
  // Sort by group and key
  filtered.sort((a, b) => {
    if (a.configGroup !== b.configGroup) {
      return (a.configGroup || '').localeCompare(b.configGroup || '');
    }
    return (a.configKey || '').localeCompare(b.configKey || '');
  });
  
  filteredConfigList.value = filtered;
  pagination.value.total = filtered.length;
  pagination.value.current = 1; // Reset to first page
};

// Search and filter handlers
const handleSearch = () => {
  applyFilters();
};

const handleReset = () => {
  searchKeyword.value = '';
  groupFilter.value = '';
  statusFilter.value = '';
  applyFilters();
};

// Computed properties for pagination
const paginatedData = computed(() => {
  const start = (pagination.value.current - 1) * pagination.value.pageSize;
  const end = start + pagination.value.pageSize;
  return filteredConfigList.value.slice(start, end);
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

const loadConfigs = async () => {
  loading.value = true;
  try {
    const response = await getConfigs();
    configList.value = response.data.data || [];
    applyFilters(); // Apply current filters after loading
  } catch (error) {
    message.error('获取配置列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

const showAddModal = () => {
  isEdit.value = false;
  modalVisible.value = true;
  resetForm();
};

const editConfig = (config) => {
  isEdit.value = true;
  modalVisible.value = true;
  Object.assign(currentConfig, config);
};

const resetForm = () => {
  Object.assign(currentConfig, {
    id: null,
    configGroup: '',
    configKey: '',
    configValue: '',
    note: '',
    status: 1
  });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    
    await updateConfig(currentConfig);
    message.success(isEdit.value ? '配置更新成功' : '配置添加成功');
    
    modalVisible.value = false;
    await loadConfigs();
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

const toggleConfig = async (config, checked) => {
  try {
    const newStatus = checked ? 1 : 0;
    const configToUpdate = { ...config, status: newStatus };
    await updateConfig(configToUpdate);
    message.success('状态更新成功');
    await loadConfigs();
  } catch (error) {
    message.error('状态更新失败：' + error.message);
  }
};

onMounted(() => {
  loadConfigs();
});
</script>

<style scoped>
.models-container {
  padding: 24px;
  background: white;
  margin: 16px;
  border-radius: 8px;
  height: calc(100vh - 96px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>