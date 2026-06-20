<template>
  <div class="search-view">
    <div class="page-title">
      <span class="title-bar"></span>
      图书搜索
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索书名或作者..."
        clearable
        @clear="fetch"
        @keyup.enter="fetch"
        size="large"
        style="width: 480px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="fetch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-table
      :data="books"
      stripe
      v-loading="loading"
      @row-click="(row: any) => $router.push(`/reader/books/${row.id}`)"
      style="cursor: pointer"
    >
      <el-table-column prop="title" label="书名" min-width="200">
        <template #default="{ row }">
          <span class="book-title-link">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="140" />
      <el-table-column prop="publisher" label="出版社" width="160" />
      <el-table-column prop="availableCopies" label="可借" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.availableCopies > 0 ? 'success' : 'danger'" size="small">
            {{ row.availableCopies }}
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
import { getBooks } from "../../api/reader/book"
import { Search } from "@element-plus/icons-vue"

const books = ref([])
const loading = ref(false)
const keyword = ref("")
const page = ref(1)
const total = ref(0)

async function fetch() {
  loading.value = true
  const r: any = await getBooks({ keyword: keyword.value, page: page.value, pageSize: 10 })
  books.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

onMounted(fetch)
</script>

<style scoped>
.search-view {
  max-width: 1000px;
  margin: 0 auto;
}

.book-title-link {
  color: var(--primary);
  font-weight: 500;
}

.book-title-link:hover {
  text-decoration: underline;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
