<template>
  <div class="reservation-list-page">
    <div class="page-title">
      <span class="title-bar"></span>
      预约管理
    </div>

    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="onTabChange" class="reservation-tabs">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="等待中" name="WAITING" />
        <el-tab-pane label="已到书" name="FULFILLED" />
        <el-tab-pane label="已借阅" name="BORROWED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
        <el-tab-pane label="已过期" name="EXPIRED" />
      </el-tabs>

      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="读者" width="120" show-overflow-tooltip />
        <el-table-column label="图书名" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.bookTitle || '图书#' + row.bookId }}
          </template>
        </el-table-column>
        <el-table-column prop="reserveDate" label="预约时间" width="170" />
        <el-table-column label="排队位置" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status==='WAITING'" size="small" type="warning">第 {{ row.queuePosition }} 位</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">
            <span v-if="row.status==='WAITING'">失效: {{ formatDate(row.expireDate) }}</span>
            <span v-else-if="row.status==='FULFILLED'" class="deadline-warning" :class="{ 'deadline-overdue': isOverdue(row) }">
              取书截止: {{ formatDate(row.pickupDeadline) }}
              <span v-if="isOverdue(row)" class="overdue-tag">已逾期</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status==='WAITING' || row.status==='FULFILLED'"
              size="small"
              type="danger"
              text
              @click="doCancel(row.id)"
            >
              取消
            </el-button>
            <span v-else class="op-none">-</span>
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
import http from "../../api/request"
import { ElMessage, ElMessageBox } from "element-plus"

const tab = ref("")
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)

// 状态文案与颜色映射
function statusText(status: string) {
  const map: Record<string, string> = {
    WAITING: "等待中", FULFILLED: "已到书", BORROWED: "已借阅",
    CANCELLED: "已取消", EXPIRED: "已过期"
  }
  return map[status] || status
}
function statusTagType(status: string) {
  const map: Record<string, string> = {
    WAITING: "warning", FULFILLED: "success", BORROWED: "",
    CANCELLED: "info", EXPIRED: "info"
  }
  return map[status] || "info"
}
function formatDate(dt: string) {
  if (!dt) return "-"
  return new Date(dt).toLocaleString("zh-CN", { hour12: false })
}

// 判断已到书（FULFILLED）的取书截止时间是否已过
function isOverdue(row: any) {
  return row.status === 'FULFILLED' && row.pickupDeadline && new Date(row.pickupDeadline).getTime() < Date.now()
}

function onTabChange() {
  page.value = 1
  fetch()
}

async function fetch() {
  loading.value = true
  try {
    const r: any = await http.get("/admin/reservations", {
      params: { page: page.value, pageSize: 10, status: tab.value || undefined }
    })
    tableData.value = r.data.records
    total.value = r.data.total
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "加载预约列表失败")
  } finally {
    loading.value = false
  }
}

async function doCancel(id: number) {
  try {
    await ElMessageBox.confirm("确认取消该预约？取消后排队位置将自动调整。", "取消预约", {
      confirmButtonText: "确认取消", cancelButtonText: "返回", type: "warning"
    })
  } catch {
    return
  }
  try {
    await http.delete(`/admin/reservations/${id}`)
    ElMessage.success("已取消该预约")
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "取消失败")
  }
}

onMounted(fetch)
</script>

<style scoped>
.reservation-list-page {
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.reservation-tabs { margin-bottom: 4px; }
.deadline-warning {
  color: var(--warning);
  font-weight: 600;
  font-size: 13px;
}
/* 已逾期：取书截止时间已过 */
.deadline-overdue {
  color: var(--danger);
}
.overdue-tag {
  margin-left: 4px;
  padding: 0 6px;
  border-radius: 4px;
  background: var(--danger-light);
  color: var(--danger);
  font-size: 12px;
}
.op-none { color: var(--text-placeholder); }
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

/* 响应式：窄窗口下表格横向滚动，避免列内容重叠 */
:deep(.el-table) {
  width: 100%;
}
:deep(.el-table .el-table__inner-wrapper) {
  overflow-x: auto;
}

/* 窄窗口下分页居中显示 */
@media (max-width: 768px) {
  .pagination-wrap {
    justify-content: center;
  }
}
</style>
