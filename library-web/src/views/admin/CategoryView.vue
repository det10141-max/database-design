<template>
  <div class="category-page">
    <div class="page-title">
      <span class="title-bar"></span>
      分类管理
    </div>

    <div class="page-header">
      <div></div>
      <el-button type="primary" @click="showDialog(null)">
        <el-icon><Plus /></el-icon>
        新增分类
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="list" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="parentId" label="父级ID" width="100" align="center" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" text @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑分类' : '新增分类'"
      width="440px"
      destroy-on-close
    >
      <el-form :model="dialogForm" label-width="80px" class="dialog-form">
        <el-form-item label="名称">
          <el-input v-model="dialogForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级ID">
          <el-input-number v-model="dialogForm.parentId" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialogForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { getCategories, createCategory, updateCategory, deleteCategory } from "../../api/admin/category"
import { ElMessage, ElMessageBox } from "element-plus"
import { Plus } from "@element-plus/icons-vue"

const list = ref([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const dialogForm = ref({ name: "", parentId: 0, sortOrder: 0 })

async function fetch() {
  const r: any = await getCategories()
  list.value = r.data
}

function showDialog(row: any) {
  editingId.value = row?.id || null
  dialogForm.value = row
    ? { name: row.name, parentId: row.parentId, sortOrder: row.sortOrder }
    : { name: "", parentId: 0, sortOrder: 0 }
  dialogVisible.value = true
}

async function save() {
  if (editingId.value) {
    await updateCategory(editingId.value, dialogForm.value)
  } else {
    await createCategory(dialogForm.value)
  }
  ElMessage.success("保存成功")
  dialogVisible.value = false
  fetch()
}

async function del(id: number) {
  await ElMessageBox.confirm("确认删除该分类？", "提示", { type: "warning" })
  await deleteCategory(id)
  ElMessage.success("删除成功")
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.category-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.dialog-form {
  padding: 16px 8px 0;
}
</style>
