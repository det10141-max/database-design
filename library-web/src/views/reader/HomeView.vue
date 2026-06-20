<template>
  <div class="home-view">
    <div class="page-header">
      <div class="page-title">
        <span class="title-bar"></span>
        图书推荐
      </div>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索书名..."
          clearable
          @clear="fetch"
          @keyup.enter="fetch"
          style="width: 280px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <div class="book-grid">
      <div
        v-for="book in books"
        :key="book.id"
        class="book-card"
        @click="openDetail(book.id)"
      >
        <div class="book-cover">
          <el-icon :size="48" color="var(--primary-lighter)"><Reading /></el-icon>
        </div>
        <div class="book-info">
          <div class="book-title">{{ book.title }}</div>
          <div class="book-author">{{ book.author }}</div>
          <div class="book-publisher">{{ book.publisher }}</div>
          <div class="book-availability">
            <span class="availability-dot" :class="safeAvailable(book) > 0 ? 'available' : 'unavailable'"></span>
            <span :class="safeAvailable(book) > 0 ? 'text-available' : 'text-unavailable'">
              可借 {{ safeAvailable(book) }} / {{ book.totalCopies }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="8"
        @current-change="fetch"
        layout="prev, pager, next"
        background
      />
    </div>

    <div class="popular-section">
      <div class="page-title" style="margin-top: 32px">
        <span class="title-bar"></span>
        热门借阅 TOP 10
      </div>
      <div class="popular-list">
        <div
          v-for="(item, index) in popular"
          :key="item.bookId"
          class="popular-item"
          @click="openDetail(item.bookId)"
        >
          <div class="popular-rank" :class="{ 'top-1': index === 0, 'top-2': index === 1, 'top-3': index === 2 }">
            {{ index + 1 }}
          </div>
          <div class="popular-info">
            <div class="popular-title">{{ item.title }}</div>
            <div class="popular-author">{{ item.author }}</div>
          </div>
          <div class="popular-count">
            <el-tag size="small" :type="index < 3 ? 'danger' : 'info'">{{ item.borrowCount }} 次</el-tag>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="detail?.title || '图书详情'" width="640px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作者">{{ detail.author }}</el-descriptions-item>
          <el-descriptions-item label="出版社">{{ detail.publisher }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ detail.isbn }}</el-descriptions-item>
          <el-descriptions-item label="出版年份">{{ detail.publishYear }}</el-descriptions-item>
          <el-descriptions-item label="定价">{{ detail.price }}</el-descriptions-item>
          <el-descriptions-item label="馆藏位置">{{ detail.location }}</el-descriptions-item>
          <el-descriptions-item label="库存状态">
            <el-tag :type="detail.stockStatus==='AVAILABLE'?'success':detail.stockStatus==='RESERVED'?'warning':'danger'">
              {{ detail.stockStatus==='AVAILABLE'?'可借':detail.stockStatus==='RESERVED'?'已预约':'无库存' }}
            </el-tag>
            ({{ safeAvailable(detail) }}/{{ detail.totalCopies }})
          </el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px">
          <el-button type="primary" v-if="safeAvailable(detail)>0" @click="doBorrow">借阅</el-button>
          <el-button type="warning" v-else @click="doReserve">预约</el-button>
        </div>
        <div style="margin-top: 24px">
          <h4 style="color: var(--text-primary); margin-bottom: 12px">评论 ({{ detail.reviewCount }}) 均分: {{ detail.avgRating?.toFixed(1) || '-' }}</h4>
          <div v-for="r in detail.reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span>
              <span class="review-user">{{ r.username }}</span>
            </div>
            <div class="review-content">{{ r.content }}</div>
          </div>
          <el-button style="margin-top: 12px" @click="openReviewDialog">写评论</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="发表评论" width="400px">
      <el-rate v-model="reviewForm.rating" />
      <el-input v-model="reviewForm.content" type="textarea" placeholder="评论内容" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="reviewVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getBooks, getBookDetail, getPopularBooks } from "../../api/reader/book"
import { borrowBook } from "../../api/reader/borrow"
import { reserve } from "../../api/reader/reserve"
import { createReview } from "../../api/reader/review"
import { ElMessage } from "element-plus"
import { Search, Reading } from "@element-plus/icons-vue"

const books = ref([])
const keyword = ref("")
const page = ref(1)
const total = ref(0)
const popular = ref<any[]>([])
const dialogVisible = ref(false)
const detail = ref<any>(null)
const reviewVisible = ref(false)
const reviewForm = ref({ rating: 5, content: "", bookId: 0 })

// 前端数据校验：确保可借数量不超过总数量，防止后端脏数据导致"可借 2 / 1"等异常显示
function safeAvailable(book: any): number {
  const available = Number(book.availableCopies) || 0
  const total = Number(book.totalCopies) || 0
  return Math.max(0, Math.min(available, total))
}

async function fetch() {
  const r: any = await getBooks({ keyword: keyword.value, page: page.value, pageSize: 8 })
  books.value = r.data.records
  total.value = r.data.total
}

async function fetchPopular() {
  const r: any = await getPopularBooks(10)
  popular.value = r.data
}

async function openDetail(bookId: number) {
  const r: any = await getBookDetail(bookId)
  detail.value = r.data
  reviewForm.value.bookId = r.data.id
  dialogVisible.value = true
}

async function doBorrow() {
  try {
    await borrowBook(detail.value.id)
    ElMessage.success("借阅成功")
    dialogVisible.value = false
    // 同时刷新图书列表和热门借阅榜单（借阅次数已变化）
    fetch()
    fetchPopular()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "借阅失败")
  }
}

async function doReserve() {
  try {
    await reserve(detail.value.id)
    ElMessage.success("预约成功")
    dialogVisible.value = false
    fetch()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "预约失败")
  }
}

// 打开评论窗口：前置校验借阅权限，并清空表单避免历史残留
function openReviewDialog() {
  if (!detail.value.hasBorrowed) {
    ElMessage.warning("只有借阅过该书的用户才能发表评论")
    return
  }
  // 每次打开前重置表单，确保无历史内容残留
  reviewForm.value = { rating: 5, content: "", bookId: detail.value.id }
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning("请输入评论内容")
    return
  }
  try {
    await createReview(reviewForm.value)
    ElMessage.success("评论发表成功")
    reviewVisible.value = false
    const r: any = await getBookDetail(reviewForm.value.bookId)
    detail.value = r.data
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "评论发表失败")
  }
}

onMounted(() => { fetch(); fetchPopular() })
</script>

<style scoped>
.home-view {
  max-width: 1200px;
  margin: 0 auto;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1100px) {
  .book-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 800px) {
  .book-grid { grid-template-columns: repeat(2, 1fr); }
}

.book-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-lighter);
}

.book-cover {
  height: 140px;
  background: var(--primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--transition-normal);
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-publisher {
  font-size: 12px;
  color: var(--text-placeholder);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-availability {
  display: flex;
  align-items: center;
  gap: 6px;
}

.availability-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.availability-dot.available {
  background: var(--success);
}

.availability-dot.unavailable {
  background: var(--danger);
}

.text-available {
  font-size: 13px;
  color: var(--success);
  font-weight: 500;
}

.text-unavailable {
  font-size: 13px;
  color: var(--danger);
  font-weight: 500;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.popular-section {
  margin-top: 8px;
}

.popular-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

@media (max-width: 800px) {
  .popular-list { grid-template-columns: 1fr; }
}

.popular-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.popular-item:hover {
  background: var(--bg-hover);
  border-color: var(--primary-lighter);
  transform: translateX(4px);
}

.popular-rank {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  background: var(--bg-page);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.popular-rank.top-1 {
  background: #FEE2E2;
  color: #DC2626;
}

.popular-rank.top-2 {
  background: #FEF3C7;
  color: #D97706;
}

.popular-rank.top-3 {
  background: #D1FAE5;
  color: #059669;
}

html.dark .popular-rank.top-1 {
  background: #7F1D1D;
  color: #FCA5A5;
}

html.dark .popular-rank.top-2 {
  background: #78350F;
  color: #FDE68A;
}

html.dark .popular-rank.top-3 {
  background: #064E3B;
  color: #6EE7B7;
}

.popular-info {
  flex: 1;
  min-width: 0;
}

.popular-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.popular-author {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.popular-count {
  flex-shrink: 0;
}

.review-item {
  border-bottom: 1px solid var(--border-light);
  padding: 10px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.review-stars {
  color: #F59E0B;
  font-size: 14px;
}

.review-user {
  color: var(--text-secondary);
  font-size: 13px;
}

.review-content {
  color: var(--text-regular);
  font-size: 14px;
  line-height: 1.6;
}
</style>
