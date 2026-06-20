<template>
  <div class="book-edit-page">
    <div class="page-title">
      <span class="title-bar"></span>
      {{ isEdit ? "编辑图书" : "新增图书" }}
    </div>

    <el-card shadow="never" class="form-card">
      <el-form
        :model="form"
        label-width="100px"
        label-position="right"
        class="book-form"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="ISBN">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名">
              <el-input v-model="form.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="出版年份">
              <el-input-number v-model="form.publishYear" placeholder="年份" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="定价">
              <el-input-number v-model="form.price" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总册数">
              <el-input-number v-model="form.totalCopies" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="馆藏位置">
          <el-input v-model="form.location" placeholder="请输入馆藏位置" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入图书简介"
          />
        </el-form-item>
        <el-form-item class="form-actions">
          <el-button type="primary" @click="submit">保存</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRoute, useRouter } from "vue-router"
import { createBook, updateBook, getBookDetail } from "../../api/admin/book"
import { ElMessage } from "element-plus"

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const form = ref<any>({
  isbn: "",
  title: "",
  author: "",
  publisher: "",
  publishYear: null,
  price: 0,
  totalCopies: 1,
  location: "",
  description: ""
})

onMounted(async () => {
  if (isEdit) {
    const r: any = await getBookDetail(Number(route.params.id))
    form.value = r.data
  }
})

async function submit() {
  if (isEdit) {
    await updateBook(Number(route.params.id), form.value)
  } else {
    await createBook(form.value)
  }
  ElMessage.success("保存成功")
  router.push("/admin/books")
}
</script>

<style scoped>
.book-edit-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.form-card {
  max-width: 900px;
}

.book-form {
  padding: 8px 0;
}

.form-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}
</style>
