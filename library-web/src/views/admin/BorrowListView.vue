<template>
  <div class="borrow-list-page">
    <div class="page-title">
      <span class="title-bar"></span>
      借阅管理
    </div>

    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="fetch" class="borrow-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="借阅中" name="BORROWING" />
        <el-tab-pane label="已归还" name="RETURNED" />
        <el-tab-pane label="已丢失" name="LOST" />
      </el-tabs>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column prop="bookTitle" label="图书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="borrowDate" label="借出日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column label="剩余天数" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 'BORROWING' && daysLeft(row) < 0" class="days-overdue">
              逾期 {{ -daysLeft(row) }}天
            </span>
            <span v-else-if="row.status === 'BORROWING' && daysLeft(row) <= 3" class="days-warning">
              {{ daysLeft(row) }}天
            </span>
            <span v-else-if="row.status === 'BORROWING'" class="days-normal">
              {{ daysLeft(row) }}天
            </span>
            <span v-else class="days-none">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'RETURNED' ? 'success' : row.status === 'LOST' ? 'danger' : 'warning'"
              effect="light"
              round
            >
              {{ row.status === 'RETURNED' ? '已归还' : row.status === 'LOST' ? '已丢失' : '借阅中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'BORROWING'"
              size="small"
              type="success"
              text
              :loading="actingId === row.id && actingType === 'return'"
              :disabled="actingId !== null"
              @click="doReturn(row.id)"
            >
              归还
            </el-button>
            <el-button
              v-if="row.status === 'BORROWING'"
              size="small"
              type="warning"
              text
              :loading="actingId === row.id && actingType === 'lost'"
              :disabled="actingId !== null"
              @click="doLost(row.id)"
            >
              丢失
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
import { getBorrows, returnBook, markLost } from "../../api/admin/borrow"
import { ElMessage } from "element-plus"

const tab = ref("")
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
// 防重复提交：记录当前正在操作的记录ID和操作类型，操作期间禁用所有按钮
const actingId = ref<number | null>(null)
const actingType = ref<"return" | "lost" | null>(null)

function daysLeft(row: any) {
  return Math.ceil((new Date(row.dueDate).getTime() - Date.now()) / 86400000)
}

async function fetch() {
  loading.value = true
  const r: any = await getBorrows({ page: page.value, pageSize: 10, status: tab.value || undefined })
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function doReturn(id: number) {
  // 防重复提交：已有操作进行中则忽略
  if (actingId.value !== null) return
  actingId.value = id
  actingType.value = "return"
  try {
    await returnBook(id)
    ElMessage.success("归还成功")
    await fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "归还失败")
  } finally {
    actingId.value = null
    actingType.value = null
  }
}

async function doLost(id: number) {
  // 防重复提交：已有操作进行中则忽略
  if (actingId.value !== null) return
  actingId.value = id
  actingType.value = "lost"
  try {
    await markLost(id)
    ElMessage.success("已标记丢失")
    await fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "标记丢失失败")
  } finally {
    actingId.value = null
    actingType.value = null
  }
}

onMounted(fetch)
</script>

<style scoped>
.borrow-list-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.borrow-tabs {
  margin-bottom: 4px;
}

.days-overdue {
  color: var(--danger);
  font-weight: 600;
  font-size: 13px;
}

.days-warning {
  color: var(--warning);
  font-weight: 600;
  font-size: 13px;
}

.days-normal {
  color: var(--text-regular);
  font-size: 13px;
}

.days-none {
  color: var(--text-placeholder);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}
</style>
