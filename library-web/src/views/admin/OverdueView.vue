<template>
  <div class="overdue-page">
    <div class="page-title">
      <span class="title-bar"></span>
      逾期管理
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="userName" label="读者" width="100" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="borrowDate" label="借出日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column prop="overdueDays" label="逾期天数" width="110" align="center" sortable>
          <template #default="{ row }">
            <div class="overdue-badge">
              <span class="overdue-days">{{ row.overdueDays }}</span>
              <span class="overdue-unit">天</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="estimatedFine" label="预计罚款" width="110" align="center" sortable>
          <template #default="{ row }">
            <span class="fine-amount">¥{{ Number(row.estimatedFine).toFixed(2) }}</span>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getOverdue } from "../../api/admin/borrow"

const list = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

async function fetch() {
  loading.value = true
  const r: any = await getOverdue({ page: page.value, pageSize: 10 })
  list.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

onMounted(fetch)
</script>

<style scoped>
.overdue-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.overdue-badge {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 2px 10px;
  border-radius: var(--radius-sm);
  background: var(--danger-light);
}

.overdue-days {
  font-weight: 700;
  font-size: 15px;
  color: var(--danger);
}

.overdue-unit {
  font-size: 12px;
  color: var(--danger);
}

.fine-amount {
  font-weight: 600;
  color: var(--warning);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}
</style>
