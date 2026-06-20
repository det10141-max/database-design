<template>
  <div class="history-view">
    <div class="page-title">
      <span class="title-bar"></span>
      借阅历史
    </div>

    <el-table :data="list" stripe v-loading="loading" :row-class-name="rowClass">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bookTitle" label="图书名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="borrowDate" label="借出日期" width="140" />
      <el-table-column prop="dueDate" label="应还日期" width="140" />
      <el-table-column prop="returnDate" label="归还日期" width="140" />
      <el-table-column label="实际天数" width="100" align="center">
        <template #default="{ row }">
          {{ row.returnDate ? Math.ceil((new Date(row.returnDate).getTime() - new Date(row.borrowDate).getTime()) / 86400000) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status==='RETURNED'?'success':row.status==='LOST'?'danger':'warning'">
            {{ row.status==='RETURNED'?'已归还':row.status==='LOST'?'已丢失':'借阅中' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="10"
        @current-change="fetch"
        layout="prev, pager, next"
        background
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getHistory } from "../../api/reader/borrow"

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

function rowClass({ row }: any) {
  return row.returnDate && new Date(row.returnDate) > new Date(row.dueDate) ? 'overdue-row' : ''
}

async function fetch() {
  loading.value = true
  const r: any = await getHistory({ page: page.value, pageSize: 10 })
  list.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

onMounted(fetch)
</script>

<style scoped>
.history-view {
  max-width: 1000px;
  margin: 0 auto;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

:deep(.overdue-row) td {
  background: var(--danger-light) !important;
}
</style>
