<template>
  <div class="book-detail-view" v-if="book">
    <div class="detail-hero">
      <div class="hero-cover">
        <el-icon :size="80" color="var(--primary-lighter)"><Reading /></el-icon>
      </div>
      <div class="hero-info">
        <h1 class="hero-title">{{ book.title }}</h1>
        <div class="hero-meta">
          <span class="meta-item">{{ book.author }}</span>
          <span class="meta-divider">|</span>
          <span class="meta-item">{{ book.publisher }}</span>
          <span class="meta-divider">|</span>
          <span class="meta-item">{{ book.publishYear }}</span>
        </div>
        <div class="hero-status">
          <el-tag
            :type="book.stockStatus==='AVAILABLE'?'success':book.stockStatus==='RESERVED'?'warning':'danger'"
            size="large"
          >
            {{ book.stockStatus==='AVAILABLE'?'可借':book.stockStatus==='RESERVED'?'已预约':'无库存' }}
          </el-tag>
          <span class="stock-text">库存 {{ book.availableCopies }} / {{ book.totalCopies }}</span>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="large" v-if="book.availableCopies>0" @click="borrow">
            <el-icon><CircleCheck /></el-icon>借阅
          </el-button>
          <el-button type="warning" size="large" v-else @click="reserve">
            <el-icon><Clock /></el-icon>预约
          </el-button>
        </div>
      </div>
    </div>

    <div class="detail-body">
      <div class="detail-section">
        <div class="page-title">
          <span class="title-bar"></span>
          图书信息
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
          <el-descriptions-item label="出版社">{{ book.publisher }}</el-descriptions-item>
          <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
          <el-descriptions-item label="出版年份">{{ book.publishYear }}</el-descriptions-item>
          <el-descriptions-item label="定价">{{ book.price }}</el-descriptions-item>
          <el-descriptions-item label="馆藏位置">{{ book.location }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="detail-section">
        <div class="page-header">
          <div class="page-title">
            <span class="title-bar"></span>
            读者评论
          </div>
          <div class="review-summary">
            <span class="review-count">{{ book.reviewCount }} 条评论</span>
            <span class="review-avg">均分 {{ book.avgRating?.toFixed(1) || '-' }}</span>
          </div>
        </div>

        <div v-if="book.reviews && book.reviews.length > 0" class="review-list">
          <div v-for="r in book.reviews" :key="r.id" class="review-card">
            <div class="review-card-header">
              <div class="review-card-user">
                <div class="review-avatar">{{ r.username?.charAt(0) || '?' }}</div>
                <span class="review-username">{{ r.username }}</span>
              </div>
              <div class="review-card-rating">
                <span class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5-r.rating) }}</span>
              </div>
            </div>
            <div class="review-card-content">{{ r.content }}</div>
          </div>
        </div>
        <div v-else class="review-empty">
          暂无评论，快来发表第一条评论吧
        </div>

        <el-button type="primary" plain style="margin-top: 16px" @click="openReviewDialog">
          <el-icon><EditPen /></el-icon>写评论
        </el-button>
      </div>
    </div>

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
import { useRoute } from "vue-router"
import { getBookDetail } from "../../api/reader/book"
import { borrowBook } from "../../api/reader/borrow"
import { reserve } from "../../api/reader/reserve"
import { createReview } from "../../api/reader/review"
import { ElMessage } from "element-plus"
import { Reading, CircleCheck, Clock, EditPen } from "@element-plus/icons-vue"

const route = useRoute()
const book = ref<any>(null)
const reviewVisible = ref(false)
const reviewForm = ref({ rating: 5, content: "", bookId: 0 })

async function load() {
  const r: any = await getBookDetail(Number(route.params.id))
  book.value = r.data
  reviewForm.value.bookId = r.data.id
}

async function borrow() {
  try {
    await borrowBook(book.value.id)
    ElMessage.success("借阅成功")
    load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "借阅失败")
  }
}

async function reserve() {
  try {
    await reserve(book.value.id)
    ElMessage.success("预约成功")
    load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "预约失败")
  }
}

// 打开评论窗口：前置校验借阅权限，并清空表单避免历史残留
function openReviewDialog() {
  if (!book.value.hasBorrowed) {
    ElMessage.warning("只有借阅过该书的用户才能发表评论")
    return
  }
  // 每次打开前重置表单，确保无历史内容残留
  reviewForm.value = { rating: 5, content: "", bookId: book.value.id }
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
    load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg || "评论发表失败")
  }
}

onMounted(load)
</script>

<style scoped>
.book-detail-view {
  max-width: 900px;
  margin: 0 auto;
}

.detail-hero {
  display: flex;
  gap: 32px;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  margin-bottom: 24px;
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.hero-cover {
  width: 180px;
  height: 240px;
  background: var(--primary-bg);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background-color var(--transition-normal);
}

.hero-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  line-height: 1.3;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 14px;
  color: var(--text-secondary);
}

.meta-divider {
  color: var(--text-placeholder);
  font-size: 12px;
}

.hero-status {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.stock-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 24px;
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.review-summary {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-count {
  font-size: 14px;
  color: var(--text-secondary);
}

.review-avg {
  font-size: 14px;
  color: var(--warning);
  font-weight: 600;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: background-color var(--transition-normal), border-color var(--transition-normal);
}

.review-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.review-card-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-bg);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.review-username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.review-stars {
  color: #F59E0B;
  font-size: 14px;
}

.review-card-content {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.7;
}

.review-empty {
  text-align: center;
  padding: 32px 0;
  color: var(--text-placeholder);
  font-size: 14px;
}

@media (max-width: 700px) {
  .detail-hero {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  .hero-meta { justify-content: center; }
  .hero-status { justify-content: center; }
  .hero-actions { justify-content: center; }
}
</style>
