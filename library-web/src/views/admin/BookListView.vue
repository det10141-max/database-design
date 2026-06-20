<template>
  <div class="book-list-page">
    <div class="page-title">
      <span class="title-bar"></span>
      图书管理
    </div>

    <div class="page-header">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名/作者"
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
      <el-button type="primary" @click="$router.push('/admin/books/add')">
        <el-icon><Plus /></el-icon>
        新增图书
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="isbn" label="ISBN" width="130" />
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="availableCopies" label="可借" width="70" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.availableCopies > 0 ? 'var(--success)' : 'var(--danger)', fontWeight: 600 }">
              {{ row.availableCopies }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCopies" label="总量" width="70" align="center" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="$router.push(`/admin/books/${row.id}`)">
              编辑
            </el-button>
            <el-button size="small" type="danger" text @click="del(row.id)">
              删除
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
import { getBooks, deleteBook } from "../../api/admin/book"
import { ElMessage, ElMessageBox } from "element-plus"
import { Search, Plus } from "@element-plus/icons-vue"

const tableData = ref([])
const loading = ref(false)
const keyword = ref("")
const page = ref(1)
const total = ref(0)

async function fetch() {
  loading.value = true
  const res: any = await getBooks({ keyword: keyword.value, page: page.value, pageSize: 10 })
  tableData.value = res.data.records
  total.value = res.data.total
  loading.value = false
}

async function del(id: number) {
  await ElMessageBox.confirm("确认删除该图书？", "提示", { type: "warning" })
  await deleteBook(id)
  ElMessage.success("删除成功")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.book-list-page {
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
