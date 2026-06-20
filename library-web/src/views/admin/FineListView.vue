<template>
  <div class="fine-list-page">
    <div class="page-title">
      <span class="title-bar"></span>
      罚款管理
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column prop="amount" label="金额" width="100" align="center">
          <template #default="{ row }">
            <span class="fine-amount">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'PAID' ? 'success' : 'danger'"
              effect="light"
              round
            >
              {{ row.status === 'PAID' ? '已缴' : '未缴' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'UNPAID'"
              size="small"
              type="primary"
              text
              @click="pay(row.id)"
            >
              标记已缴
            </el-button>
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
import { getFines, payFine } from "../../api/admin/fine"
import { ElMessage } from "element-plus"

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

async function fetch() {
  loading.value = true
  const r: any = await getFines({ page: page.value, pageSize: 10 })
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function pay(id: number) {
  await payFine(id)
  ElMessage.success("标记成功")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.fine-list-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
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
