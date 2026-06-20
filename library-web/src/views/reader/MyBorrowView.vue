<template>
  <div class="borrow-view">
    <div class="page-title">
      <span class="title-bar"></span>
      当前借阅
    </div>

    <el-table :data="list" stripe v-loading="loading" :row-class-name="rowClass">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookTitle" label="图书名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="borrowDate" label="借出日期" width="140" />
      <el-table-column prop="dueDate" label="应还日期" width="140" />
      <el-table-column label="剩余天数" width="120" align="center">
        <template #default="{ row }">
          <span v-if="daysLeft(row) < 0" class="days-overdue">已逾期 {{ -daysLeft(row) }} 天</span>
          <span v-else-if="daysLeft(row) <= 3" class="days-warning">{{ daysLeft(row) }} 天</span>
          <span v-else class="days-normal">{{ daysLeft(row) }} 天</span>
        </template>
      </el-table-column>
      <el-table-column prop="renewCount" label="续借次数" width="90" align="center" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="doRenew(row.id)">续借</el-button>
          <el-button size="small" type="success" @click="doReturn(row.id)">归还</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getCurrent, renew, returnBook } from "../../api/reader/borrow"
import { ElMessage } from "element-plus"

const list = ref([])
const loading = ref(false)

function daysLeft(row: any) {
  return Math.ceil((new Date(row.dueDate).getTime() - Date.now()) / 86400000)
}

function rowClass({ row }: any) {
  return daysLeft(row) < 0 ? 'overdue-row' : ''
}

async function fetch() {
  loading.value = true
  const r: any = await getCurrent({ page: 1, pageSize: 100 })
  list.value = r.data.records
  loading.value = false
}

async function doRenew(id: number) {
  try {
    await renew(id)
    ElMessage.success("续借成功")
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "续借失败")
  }
}

async function doReturn(id: number) {
  await returnBook(id)
  ElMessage.success("归还成功")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.borrow-view {
  max-width: 1000px;
  margin: 0 auto;
}

.days-overdue {
  color: var(--danger);
  font-weight: 700;
  font-size: 13px;
}

.days-warning {
  color: var(--warning);
  font-weight: 600;
  font-size: 13px;
}

.days-normal {
  color: var(--success);
  font-weight: 500;
  font-size: 13px;
}

:deep(.overdue-row) td {
  background: var(--danger-light) !important;
}
</style>
