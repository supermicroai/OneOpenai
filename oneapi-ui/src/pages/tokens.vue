<template>
  <div class="tokens-container">
    <!-- Add Token Button -->
    <div class="action-bar" style="margin-bottom: 16px;">
      <a-button type="primary" @click="showCreateModal">
        <template #icon>
          <PlusOutlined />
        </template>
        新建令牌
      </a-button>
    </div>
    
    <a-table 
      :columns="columns" 
      :dataSource="tokens" 
      :loading="loading"
      rowKey="id"
      :pagination="paginationConfig"
    >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'apiKey'">
            <span style="display: flex; align-items: center; gap: 8px;">
              <span>{{ maskApiKey(record.apiKey) }}</span>
              <a-button 
                type="text" 
                size="small" 
                @click="copyApiKey(record.apiKey)"
              >
                <CopyOutlined />
              </a-button>
            </span>
          </template>
          
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'expireTime'">
            {{ record.expireTime ? dayjs(record.expireTime).format('YYYY-MM-DD HH:mm:ss') : '永不过期' }}
          </template>
          
          <template v-if="column.key === 'maxUsage'">
            {{ record.maxUsage === -1 ? '不限制' : record.maxUsage }}
          </template>
          
          <template v-if="column.key === 'costLimit'">
            {{ record.maxCostLimit === -1 || record.maxCostLimit === null ? '不限制' : `$${record.maxCostLimit}` }}
          </template>
          
          <template v-if="column.key === 'usage'">
            {{ record.tokenUsage || 0 }} / {{ record.maxUsage === -1 ? '∞' : record.maxUsage }}
          </template>
          
          <template v-if="column.key === 'costUsage'">
            <span>
              ${{ record.currentCostUsage || 0 }} / {{ record.maxCostLimit === -1 || record.maxCostLimit === null ? '∞' : `$${record.maxCostLimit}` }}
            </span>
          </template>
          
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="editToken(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="toggleStatus(record)">
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-popconfirm
                title="确定要删除这个令牌吗？"
                @confirm="deleteTokenById(record.id)"
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

    <!-- 创建/编辑令牌对话框 -->
    <a-modal 
      v-model:open="modalVisible" 
      :title="editingToken ? '编辑令牌' : '新建令牌'"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form 
        :model="form" 
        :rules="rules" 
        ref="formRef"
        layout="vertical"
      >
        <a-form-item label="令牌名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入令牌名称" />
        </a-form-item>
        
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="form.description" placeholder="请输入令牌描述" />
        </a-form-item>
        
        <a-form-item label="过期时间" name="expireTime">
          <a-date-picker 
            v-model:value="form.expireTime" 
            show-time 
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择过期时间（不选则永不过期）"
            style="width: 100%"
          />
        </a-form-item>
        
        <a-form-item label="最大Token数" name="maxUsage">
          <a-input-number 
            v-model:value="form.maxUsage" 
            :min="-1"
            placeholder="最大Token数（-1表示不限制）"
            style="width: 100%"
          />
        </a-form-item>
        
        <a-form-item label="最大费用限制" name="maxCostLimit">
          <a-input-number 
            v-model:value="form.maxCostLimit" 
            :min="-1"
            placeholder="最大费用限制（-1表示不限制）"
            style="width: 100%"
          />
          <div style="margin-top: 4px; color: #666; font-size: 12px;">
            设置费用限制后，将优先检查费用限制而非Token数量限制
          </div>
        </a-form-item>
        
        <a-form-item v-if="editingToken && editingToken.apiKey" label="API密钥">
          <span style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: #f5f5f5; border: 1px solid #d9d9d9; border-radius: 6px;">
            <span>{{ maskApiKey(editingToken.apiKey) }}</span>
            <a-button 
              type="text" 
              size="small" 
              @click="copyApiKey(editingToken.apiKey)"
            >
              <CopyOutlined />
            </a-button>
          </span>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, CopyOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { getAllTokens, createToken, updateToken, deleteToken } from '@/api/token.js';

const tokens = ref([]);
const loading = ref(false);
const modalVisible = ref(false);
const editingToken = ref(null);
const formRef = ref();

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
});

const form = reactive({
  name: '',
  description: '',
  expireTime: null,
  maxUsage: -1,
  maxCostLimit: -1
});

const rules = {
  name: [
    { required: true, message: '请输入令牌名称', trigger: 'blur' }
  ]
};

const columns = [
  {
    title: '令牌名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'API密钥',
    dataIndex: 'apiKey',
    key: 'apiKey',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
  },
  {
    title: '过期时间',
    dataIndex: 'expireTime',
    key: 'expireTime',
  },
  {
    title: 'Token限制',
    dataIndex: 'maxUsage',
    key: 'maxUsage',
  },
  {
    title: '费用限制',
    key: 'costLimit',
  },
  {
    title: 'Token使用情况',
    key: 'usage',
  },
  {
    title: '费用使用情况',
    key: 'costUsage',
  },
  {
    title: '创建者',
    dataIndex: 'creator',
    key: 'creator',
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
  },
];

// Computed properties for pagination
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

const loadTokens = async () => {
  loading.value = true;
  try {
    const response = await getAllTokens();
    if (response.data.success) {
      tokens.value = response.data.data || [];
    } else {
      message.error(response.data.message);
    }
  } catch (error) {
    message.error('加载令牌列表失败');
  } finally {
    loading.value = false;
  }
};

const showCreateModal = () => {
  editingToken.value = null;
  resetForm();
  modalVisible.value = true;
};

const editToken = (token) => {
  editingToken.value = token;
  form.name = token.name;
  form.description = token.description;
  form.expireTime = token.expireTime ? dayjs(token.expireTime) : null;
  form.maxUsage = token.maxUsage;
  form.maxCostLimit = token.maxCostLimit === null ? -1 : token.maxCostLimit;
  modalVisible.value = true;
};

const resetForm = () => {
  form.name = '';
  form.description = '';
  form.expireTime = null;
  form.maxUsage = -1;
  form.maxCostLimit = -1;
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    
    const expireTimeStr = form.expireTime ? form.expireTime.format('YYYY-MM-DD HH:mm:ss') : null;

    let response;
    if (editingToken.value) {
      // 更新令牌
      const params = {
        id: editingToken.value.id,
        name: form.name,
        description: form.description,
        expireTime: expireTimeStr,
        maxUsage: form.maxUsage,
        maxCostLimit: form.maxCostLimit === -1 ? null : form.maxCostLimit,
        apiKey: editingToken.value.apiKey,
        tokenUsage: editingToken.value.tokenUsage,
        currentCostUsage: editingToken.value.currentCostUsage,
        status: editingToken.value.status,
        creator: editingToken.value.creator,
        lastUsedTime: editingToken.value.lastUsedTime
      };
      
      response = await updateToken(params);
    } else {
      // 创建令牌
      response = await createToken(form.name, form.description, expireTimeStr, form.maxUsage, form.maxCostLimit === -1 ? null : form.maxCostLimit, 'admin');
    }
    
    if (response.data.success) {
      message.success(editingToken.value ? '更新成功' : '创建成功');
      modalVisible.value = false;
      loadTokens();
    } else {
      message.error(response.data.message);
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const handleCancel = () => {
  modalVisible.value = false;
  resetForm();
};

const toggleStatus = async (token) => {
  try {
    const newStatus = token.status === 1 ? 0 : 1;
    const params = {
      ...token,
      status: newStatus
    };
    
    const response = await updateToken(params);
    if (response.data.success) {
      message.success(newStatus === 1 ? '启用成功' : '禁用成功');
      loadTokens();
    } else {
      message.error(response.data.message);
    }
  } catch (error) {
    message.error('操作失败');
  }
};

const deleteTokenById = async (id) => {
  try {
    const response = await deleteToken(id);
    if (response.data.success) {
      message.success('删除成功');
      loadTokens();
    } else {
      message.error(response.data.message);
    }
  } catch (error) {
    message.error('删除失败');
  }
};

const maskApiKey = (apiKey) => {
  if (!apiKey || apiKey.length < 10) {
    return apiKey;
  }
  const start = apiKey.substring(0, 6);
  const end = apiKey.substring(apiKey.length - 4);
  return `${start}****${end}`;
};

const copyApiKey = async (apiKey) => {
  try {
    await navigator.clipboard.writeText(apiKey);
    message.success('API密钥已复制到剪贴板');
  } catch (error) {
    message.error('复制失败');
  }
};

onMounted(() => {
  loadTokens();
});
</script>

<style scoped>
.tokens-container {
  padding: 24px;
  background: white;
  margin: 16px;
  border-radius: 8px;
}
</style>
