<template>
  <div class="table-container">
    <a-table :columns="columns" :data-source="data" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a :href="record.url" target="_blank">{{ record.url }}</a>
        </template>
        <template v-else-if="column.dataIndex === 'models'">
          <div class="models">
            <span class="model" v-for="(value, key) in record.models" :key="key">
              <a-tag>{{ key }}</a-tag>
            </span>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'enable'">
          <a-switch v-model:checked="record.enable" @change="enableChange(record)" />
        </template>
        <template v-else-if="column.dataIndex === 'type'">
          <span>{{ typeMapping[record.type] }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'action'">
          <span>
            <a class="op" @click="changeRoute(`/accounts/${record.id}`)">账号</a>
            <a class="op" @click="changeRoute(`/provider/${record.id}`)">编辑</a>
          </span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getProviders, enableProvider } from "@/api/provider.js";
import { message } from "ant-design-vue";

const router = useRouter();

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

const data = ref([]);

const enableChange = (record) => {
  enableProvider(record.id, record.enable).then(() => {
    message.success('更新成功');
  }).catch((error) => {
    message.success('更新失败: ' + error);
  });
};

onMounted(async () => {
  try {
    const response = await getProviders();
    const result = data.value = response.data.data;
    if (result && result.length > 0) {
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
    }
  } catch (error) {
    console.error('Error fetching providers:', error);
  }
});
</script>

<style scoped>
.table-container {
  padding: 20px;
}

.op {
  padding-right: 10px;
}

.models {
  display: flex;
  flex-wrap: wrap;
}

.model {
  cursor: pointer;
  margin-bottom: 5px;
}
</style>
