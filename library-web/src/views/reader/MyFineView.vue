<template>
  <div class="fine-view">
    <div class="page-title">
      <span class="title-bar"></span>
      我的罚款
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="amount" label="金额" width="100" align="center">
        <template #default="{ row }">
          <span class="fine-amount">¥{{ row.amount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status==='PAID'?'success':'danger'">
            {{ row.status==='PAID'?'已缴':'未缴' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column prop="paidAt" label="缴纳时间" width="170">
        <template #default="{ row }">
          {{ row.paidAt || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status==='UNPAID'"
            size="small"
            type="primary"
            @click="handlePay(row)"
          >
            缴纳
          </el-button>
          <span v-else class="paid-text">已完成</span>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && list.length === 0" description="暂无罚款记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import http from "../../api/request"
import { ElMessage, ElMessageBox } from "element-plus"

const list = ref([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  try {
    const r: any = await http.get("/reader/fines", { params: { page: 1, pageSize: 100 } })
    list.value = r.data.records
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "加载罚款列表失败")
  } finally {
    loading.value = false
  }
}

// 模拟缴纳：弹出确认框，确认后调用接口标记为已缴
async function handlePay(row: any) {
  try {
    await ElMessageBox.confirm(
      `确认缴纳罚款 ¥${row.amount} 元？`,
      "缴纳确认",
      { confirmButtonText: "确认缴纳", cancelButtonText: "取消", type: "warning" }
    )
  } catch {
    return // 用户取消
  }

  try {
    await http.put(`/reader/fines/${row.id}/pay`)
    ElMessage.success("缴纳成功")
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "缴纳失败")
  }
}

onMounted(fetch)
</script>

<style scoped>
.fine-view {
  max-width: 1000px;
  margin: 0 auto;
}

.fine-amount {
  font-weight: 600;
  color: var(--danger);
}

.paid-text {
  color: var(--text-placeholder);
  font-size: 13px;
}
</style>
