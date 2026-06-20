<template>
  <div class="user-list-page">
    <div class="page-title">
      <span class="title-bar"></span>
      读者管理
    </div>

    <div class="page-header">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/姓名"
          clearable
          style="width: 280px"
          @clear="fetch"
          @keyup.enter="fetch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'danger'"
              effect="light"
              round
            >
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              text
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
import { getUsers, updateUserStatus } from "../../api/admin/user"
import { ElMessage } from "element-plus"
import { Search } from "@element-plus/icons-vue"

const tableData = ref([])
const loading = ref(false)
const keyword = ref("")
const page = ref(1)
const total = ref(0)

async function fetch() {
  loading.value = true
  const r: any = await getUsers({ page: page.value, pageSize: 10, keyword: keyword.value })
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function toggleStatus(row: any) {
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success("操作成功")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.user-list-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}
</style>
