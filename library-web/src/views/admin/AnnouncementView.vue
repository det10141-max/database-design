<template>
  <div class="announcement-page">
    <div class="page-title">
      <span class="title-bar"></span>
      公告管理
    </div>

    <div class="page-header">
      <div></div>
      <el-button type="primary" @click="dialogVisible = true">
        <el-icon><Plus /></el-icon>
        发布公告
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="{ row }">
            <span class="title-link" @click="viewDetail(row)">
              <el-icon v-if="row.isPinned" class="pin-icon"><Star /></el-icon>
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isPinned" label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="warning" effect="light" size="small" round>置顶</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" text @click="del(row.id)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      title="发布公告"
      width="520px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px" class="dialog-form">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isPinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="publish">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="公告详情"
      width="580px"
      destroy-on-close
    >
      <template v-if="current">
        <div class="detail-header">
          <h2 class="detail-title">{{ current.title }}</h2>
          <div class="detail-meta">
            <span>{{ current.createdAt }}</span>
            <el-tag v-if="current.isPinned" type="warning" effect="light" size="small" round>置顶</el-tag>
          </div>
        </div>
        <div class="detail-content">{{ current.content }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import http from "../../api/request"
import { ElMessage, ElMessageBox } from "element-plus"
import { Plus, Star } from "@element-plus/icons-vue"

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const current = ref<any>(null)
const page = ref(1)
const total = ref(0)
const form = ref({ title: "", content: "", isPinned: 0, status: 1 })

async function fetch() {
  loading.value = true
  const r: any = await http.get("/admin/announcements", { params: { page: page.value, pageSize: 10 } })
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function publish() {
  const data = {
    ...form.value,
    isPinned: Number(form.value.isPinned)
  }
  await http.post("/admin/announcements", data)
  ElMessage.success("发布成功")
  dialogVisible.value = false
  form.value = { title: "", content: "", isPinned: 0, status: 1 }
  fetch()
}

function viewDetail(row: any) {
  current.value = row
  detailVisible.value = true
}

async function del(id: number) {
  await ElMessageBox.confirm("确认删除该公告？", "提示", { type: "warning" })
  await http.delete(`/admin/announcements/${id}`)
  ElMessage.success("已删除")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.announcement-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.title-link {
  cursor: pointer;
  color: var(--primary);
  transition: color var(--transition-fast);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.title-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.pin-icon {
  color: var(--warning);
  font-size: 14px;
}

.text-muted {
  color: var(--text-placeholder);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.dialog-form {
  padding: 16px 8px 0;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}

.detail-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--text-secondary);
}

.detail-content {
  line-height: 1.8;
  white-space: pre-wrap;
  min-height: 120px;
  color: var(--text-regular);
  font-size: 14px;
}
</style>
