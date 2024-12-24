<template>
  <div class="provider-form">
    <a-form labelAlign="left" :colon="false">
      <a-form-item label="名称" :labelCol="{ span: 2 }">
        <a-input v-model:value="provider.name" />
      </a-form-item>
      <a-form-item label="网址" :labelCol="{ span: 2 }">
        <a-input v-model:value="provider.url" />
      </a-form-item>
      <a-form-item label="API地址" :labelCol="{ span: 2 }">
        <a-input v-model:value="provider.api" />
      </a-form-item>
      <a-form-item label="模型类型" :labelCol="{ span: 2 }">
        <a-select v-model:value="provider.type" @change="filterModels">
          <a-select-option value="embedding">向量</a-select-option>
          <a-select-option value="llm">文本生成</a-select-option>
          <a-select-option value="ocr">OCR</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="模型列表" :labelCol="{ span: 2 }">
        <a-table :columns="columns" :dataSource="paginatedData" :pagination="pagination">
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'name'">
              <span>{{ record.name }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'value'">
              <a-input v-model:value="record.value" />
            </template>
          </template>
        </a-table>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSubmit">保存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { getModels, getProvider, updateProvider } from '@/api/provider';

const route = useRoute();
const providerId = route.params.providerId;

const provider = ref({
  id: -1,
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

const columns = [
  {
    title: '模型名称',
    dataIndex: 'name',
    width: 100,
  },
  {
    title: '别名',
    dataIndex: 'value',
    width: 200,
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
    const response = await getModels();
    const list = response.data.data;
    modelList.value = list.sort((a, b) => a.name.localeCompare(b.name));
    filterModels();
  } catch (error) {
    console.error('Error fetching models:', error);
  }
};

onMounted(() => {
  loadProvider();
});

const filterModels = () => {
  filteredModelList.value = modelList.value
      .filter(model => model.type === provider.value.type)
      .map(model => ({
        name: model.name,
        value: provider.value.models[model.name] || ''
      }));
};

const handleSubmit = (e) => {
  e.preventDefault();
  try {
    const list = filteredModelList.value
        .filter(item => item.value.trim() !== '')
        .map(item => [item.name, item.value]);
    provider.value.models = Object.fromEntries(list);
    updateProvider(provider.value).then(() => {
      message.success('更新成功');
      loadProvider();
    });
  } catch (error) {
    message.error('更新失败:' + error);
  }
};
</script>

<style scoped>
.provider-form {
  display: flex;
  flex-direction: column;
  width: calc(100% - 40px);
  height: calc(100vh - 20px);
  padding: 20px;
  background-color: white;
  overflow-y: auto;
}
</style>
