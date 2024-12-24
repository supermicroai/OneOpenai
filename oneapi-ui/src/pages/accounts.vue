<template>
  <div class="account-form">
    <a-table :columns="columns" :dataSource="accounts" rowKey="id" bordered :pagination="false">
      <template #bodyCell="{ column, record, index }">
        <template v-if="record.isEditing">
          <template v-if="column.dataIndex === 'name'">
            <span class="display-field">{{ record.name }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'apiKey'">
            <a-input v-model:value="record.apiKey" />
          </template>
          <template v-else-if="column.dataIndex === 'ak'">
            <a-input v-model:value="record.ak" />
          </template>
          <template v-else-if="column.dataIndex === 'sk'">
            <a-input v-model:value="record.sk" />
          </template>
          <template v-else-if="column.dataIndex === 'note'">
            <a-input v-model:value="record.note" />
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-switch v-model:checked="record.status" />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a class="action-button" @click="saveAccount(index)">保存</a>
            <a class="action-button" @click="cancelEdit(index)">取消</a>
          </template>
        </template>
        <template v-else>
          <template v-if="column.dataIndex === 'name'">
            <span class="display-field">{{ record.name }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'apiKey'">
            <span class="display-field">{{ maskApiKey(record.apiKey) }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'ak'">
            <span class="display-field">{{ record.ak }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'sk'">
            <span class="display-field">{{ record.sk }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'note'">
            <span class="display-field">{{ record.note }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-switch v-model:checked="record.status" @change="enableChange(record)" />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a class="action-button" @click="editAccount(index)">编辑</a>
            <a class="action-button" v-if="false" @click="removeAccount(index)">删除</a>
          </template>
        </template>
      </template>
    </a-table>
    <a-button type="dashed" @click="addAccount" style="margin-top: 16px;">增加账户</a-button>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {enableAccount, getAccounts, updateAccount} from "@/api/provider.js";
import {useRoute} from "vue-router";
import {message} from "ant-design-vue";

const route = useRoute();
const providerId = route.params.providerId;

const providerName = ref('');
const accounts = ref([]);

const columns = [
  {title: '提供者', dataIndex: 'name', width: 150, className: 'center-header'},
  {title: 'key', dataIndex: 'apiKey', width: 150, className: 'center-header'},
  {title: 'AK', dataIndex: 'ak', width: 150, className: 'center-header'},
  {title: 'SK', dataIndex: 'sk', width: 150, className: 'center-header'},
  {title: '备注', dataIndex: 'note', width: 150, className: 'center-header'},
  {title: '是否启用', dataIndex: 'status', width: 100, className: 'center-header'},
  {title: '操作', dataIndex: 'action', width: 100, className: 'center-header'}
];

const addAccount = () => {
  accounts.value.push({
    name: providerName.value,
    apiKey: '',
    ak: '',
    sk: '',
    note: '',
    status: false,
    isEditing: true,
    isNew: true
  });
};

const editAccount = (index) => {
  accounts.value[index].isEditing = true;
  accounts.value[index].original = {...accounts.value[index]};
};

const enableChange = (record) => {
  enableAccount(record.id, record.status).then(() => {
    message.success('更新成功');
    loadAccount();
  }).catch((error) => {
    message.success('更新失败: ' + error);
  });
};

const saveAccount = (index) => {
  let account = accounts.value[index];
  delete account.isNew;
  delete account.original;
  updateAccount(account).then(res => {
    message.success('新增或保存成功');
    account = res.data.data;
    if (account) {
      accounts.value[index] = res.data.data;
      account.isEditing = false;
    }
  }).catch((error) => {
    message.success('更新失败: ' + error);
  });
};

const cancelEdit = (index) => {
  if (accounts.value[index].isNew) {
    accounts.value.splice(index, 1);
  } else {
    accounts.value[index] = {...accounts.value[index].original, isEditing: false};
    delete accounts.value[index].original;
  }
};

const removeAccount = (index) => {
  accounts.value.splice(index, 1);
};

const maskApiKey = (apiKey) => {
  if (apiKey.length <= 16) {
    return apiKey;
  }
  return `${apiKey.slice(0, 8)}***${apiKey.slice(-8)}`;
};

const loadAccount = async () => {
  try {
    const response = await getAccounts(providerId);
    accounts.value = response.data.data;
    providerName.value = response.data.params.name;
  } catch (error) {
    console.error('Error fetching accounts:', error);
  }
};

onMounted( () => {
  loadAccount();
});
</script>

<style scoped>
.account-form {
  display: flex;
  flex-direction: column;
  background-color: white;
  padding: 20px;
}

.action-button {
  margin-right: 10px;
}

.display-field {
  padding: 5px 12px;
}
</style>
