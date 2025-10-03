<template>
  <div class="access-logs-container">
    <!-- 查询条件 -->
    <div class="filter-section">
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="4">
          <a-select 
            v-model:value="searchForm.provider" 
            placeholder="选择服务提供商"
            style="width: 100%"
            allowClear
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="openai">OpenAI</a-select-option>
            <a-select-option value="aliyun">阿里云</a-select-option>
            <a-select-option value="deepseek">DeepSeek</a-select-option>
            <a-select-option value="silicon">Silicon</a-select-option>
            <a-select-option value="openrouter">OpenRouter</a-select-option>
          </a-select>
        </a-col>
        
        <a-col :span="4">
          <a-input 
            v-model:value="searchForm.model" 
            placeholder="模型名称"
            allowClear
          />
        </a-col>
        
        <a-col :span="3">
          <a-select 
            v-model:value="searchForm.status" 
            placeholder="状态"
            style="width: 100%"
            allowClear
          >
            <a-select-option :value="null">全部</a-select-option>
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">失败</a-select-option>
          </a-select>
        </a-col>
        
        <a-col :span="9">
          <a-range-picker 
            v-model:value="searchForm.timeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            :placeholder="['开始时间', '结束时间']"
            style="width: 100%"
          />
        </a-col>
        
        <a-col :span="4">
          <div style="display: flex; gap: 8px; justify-content: flex-end;">
            <a-button @click="handleReset">
              <template #icon>
                <ReloadOutlined />
              </template>
              重置
            </a-button>
            <a-button type="primary" :loading="loading" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
            <a-button 
              type="default" 
              :loading="calculating" 
              @click="handleRecalculateCosts"
              style="background-color: #52c41a; border-color: #52c41a; color: white;"
            >
              <template #icon>
                <CalculatorOutlined />
              </template>
              计算开销
            </a-button>
          </div>
        </a-col>
      </a-row>
    </div>
    
    <!-- 数据表格 -->
    <a-table 
      :columns="columns" 
      :dataSource="accessLogs" 
      :loading="loading"
      rowKey="id"
      :pagination="paginationConfig"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '成功' : '失败' }}
          </a-tag>
        </template>
        
        <template v-if="column.key === 'cost'">
          {{ formatCost(record.cost) }}
        </template>
        
        <template v-if="column.key === 'totalTokens'">
          {{ (record.requestTokens || 0) + (record.responseTokens || 0) }}
        </template>
        
        <template v-if="column.key === 'gmtCreate'">
          {{ formatDateTime(record.gmtCreate) }}
        </template>
        
        <template v-if="column.key === 'errorMsg'">
          <a-tooltip v-if="record.errorMsg" :title="record.errorMsg">
            <span class="error-msg">{{ truncateErrorMsg(record.errorMsg) }}</span>
          </a-tooltip>
          <span v-else>-</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { message } from 'ant-design-vue';
import { SearchOutlined, ReloadOutlined, CalculatorOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import { queryUsageRecords, recalculateAllTokenCosts } from '@/api/token.js';

const accessLogs = ref([]);
const loading = ref(false);
const calculating = ref(false);

const searchForm = reactive({
  provider: undefined,
  model: undefined,
  status: null,
  timeRange: undefined,
});

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条记录`,
});

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    fixed: 'left',
  },
  {
    title: '服务提供商',
    dataIndex: 'provider',
    key: 'provider',
    width: 120,
  },
  {
    title: '模型',
    dataIndex: 'model',
    key: 'model',
    width: 150,
  },
  {
    title: '请求Token',
    dataIndex: 'requestTokens',
    key: 'requestTokens',
    width: 100,
  },
  {
    title: '响应Token',
    dataIndex: 'responseTokens',
    key: 'responseTokens',
    width: 100,
  },
  {
    title: '总Token',
    key: 'totalTokens',
    width: 100,
  },
  {
    title: '成本',
    dataIndex: 'cost',
    key: 'cost',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 130,
  },
  {
    title: '错误信息',
    dataIndex: 'errorMsg',
    key: 'errorMsg',
    width: 200,
  },
  {
    title: '创建时间',
    dataIndex: 'gmtCreate',
    key: 'gmtCreate',
    width: 180,
    fixed: 'right',
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
    loadAccessLogs();
  },
  onShowSizeChange: (current, size) => {
    pagination.value.current = 1;
    pagination.value.pageSize = size;
    loadAccessLogs();
  }
}));

const loadAccessLogs = async () => {
  loading.value = true;
  try {
    // 构建查询参数
    const provider = searchForm.provider || null;
    const model = searchForm.model || null;
    const status = searchForm.status;
    
    let startTime = null;
    let endTime = null;
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      startTime = searchForm.timeRange[0].format('YYYY-MM-DD HH:mm:ss');
      endTime = searchForm.timeRange[1].format('YYYY-MM-DD HH:mm:ss');
    }
    
    const response = await queryUsageRecords(
      provider, 
      model, 
      status, 
      startTime, 
      endTime, 
      pagination.value.current, 
      pagination.value.pageSize
    );
    
    if (response.data.success) {
      accessLogs.value = response.data.data || [];
      pagination.value.total = response.data.total || 0;
    } else {
      message.error(response.data.message || '查询访问日志失败');
    }
  } catch (error) {
    message.error('查询访问日志失败');
    console.error('查询访问日志异常:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.current = 1; // 重置到第一页
  loadAccessLogs();
};

const handleReset = () => {
  searchForm.provider = undefined;
  searchForm.model = undefined;
  searchForm.status = null;
  searchForm.timeRange = undefined;
  pagination.value.current = 1;
  loadAccessLogs();
};

const formatCost = (cost) => {
  if (cost === null || cost === undefined) {
    return '-';
  }
  const numCost = Number(cost);
  
  // 如果成本为0，显示0.0000
  if (numCost === 0) {
    return '$0';
  }
  
  // 如果成本大于0但小于0.0001，显示<0.0001
  if (numCost > 0 && numCost < 0.00001) {
    return '小于 $0.00001';
  }
  
  // 其他情况显示四位小数
  return `$${numCost.toFixed(5)}`;
};

const formatDateTime = (dateTime) => {
  if (!dateTime) {
    return '-';
  }
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss');
};

const truncateErrorMsg = (errorMsg) => {
  if (!errorMsg) {
    return '-';
  }
  return errorMsg.length > 30 ? errorMsg.substring(0, 30) + '...' : errorMsg;
};

const handleRecalculateCosts = async () => {
  calculating.value = true;
  try {
    const response = await recalculateAllTokenCosts();
    if (response.data.success) {
      message.success(response.data.data || '计算完成！');
      // 重新加载数据
      loadAccessLogs();
    } else {
      message.error(response.data.message || '计算开销失败');
    }
  } catch (error) {
    message.error('计算开销失败');
    console.error('计算开销异常:', error);
  } finally {
    calculating.value = false;
  }
};

onMounted(() => {
  loadAccessLogs();
});
</script>

<style scoped>
.access-logs-container {
  padding: 24px;
  background: white;
  margin: 16px;
  border-radius: 8px;
}

.filter-section {
  margin-bottom: 16px;
}

.error-msg {
  color: #ff4d4f;
  cursor: pointer;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
}
</style>