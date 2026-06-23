<template>
  <div class="reservation-view">
    <div class="page-title">
      <span class="title-bar"></span>
      我的预约
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图书名" min-width="180" show-overflow-tooltip>
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
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            :type="statusTagType(row.status)"
            size="small"
          >
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="截止时间" width="170">
        <template #default="{ row }">
          <span v-if="row.status==='WAITING'">失效: {{ formatDate(row.expireDate) }}</span>
          <span v-else-if="row.status==='FULFILLED'" class="deadline-warning">取书截止: {{ formatDate(row.pickupDeadline) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status==='FULFILLED'"
            size="small"
            type="primary"
            @click="pickup(row.id)"
          >
            取书借阅
          </el-button>
          <el-button
            v-if="row.status==='WAITING'"
            size="small"
            type="danger"
            @click="cancel(row.id)"
          >
            取消
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无预约记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getReservations, cancelReservation, pickupReservation } from "../../api/reader/reserve"
import { ElMessage } from "element-plus"

const list = ref([])
const loading = ref(false)

// 状态文案映射
function statusText(status: string) {
  const map: Record<string, string> = {
    WAITING: "等待中",
    FULFILLED: "已到书",
    CANCELLED: "已取消",
    EXPIRED: "已过期",
    BORROWED: "已借阅"
  }
  return map[status] || status
}

// 状态标签颜色映射
function statusTagType(status: string) {
  const map: Record<string, string> = {
    WAITING: "warning",
    FULFILLED: "success",
    CANCELLED: "info",
    EXPIRED: "info",
    BORROWED: ""
  }
  return map[status] || "info"
}

// 格式化日期时间
function formatDate(dt: string) {
  if (!dt) return "-"
  return new Date(dt).toLocaleString("zh-CN", { hour12: false })
}

async function fetch() {
  loading.value = true
  try {
    const r: any = await getReservations()
    const now = Date.now()
    // 前端实时修正：已过取书截止时间的 FULFILLED 记录视为 EXPIRED
    // （后端定时任务每天凌晨才批量处理，这里避免在过期后仍显示"取书借阅"按钮误导用户）
    list.value = (r.data || []).map((item: any) => {
      if (item.status === 'FULFILLED' && item.pickupDeadline && new Date(item.pickupDeadline).getTime() < now) {
        return { ...item, status: 'EXPIRED' }
      }
      return item
    })
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "加载预约列表失败")
  } finally {
    loading.value = false
  }
}

async function cancel(id: number) {
  try {
    await cancelReservation(id)
    ElMessage.success("取消成功")
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "取消失败")
  }
}

async function pickup(id: number) {
  try {
    await pickupReservation(id)
    ElMessage.success("取书借阅成功，请前往「当前借阅」查看")
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "取书失败")
  }
}

onMounted(fetch)
</script>

<style scoped>
.reservation-view {
  max-width: 1000px;
  margin: 0 auto;
}

.deadline-warning {
  color: var(--warning);
  font-weight: 600;
  font-size: 13px;
}
</style>
