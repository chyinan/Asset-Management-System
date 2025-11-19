<template>
  <PageContainer title="部门管理" description="维护组织结构，支撑审批链路" eyebrow="系统管理">
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索部门名称"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
      />
    </template>
    <div class="table-shell surface-card">
      <el-table v-if="filteredDepartments.length" :data="filteredDepartments" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="parentId" label="上级ID" width="120" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <el-empty v-else description="暂无部门" />
  </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listDepartments } from '@/api/modules/basic'
import type { Department } from '@/types/domain'
import PageContainer from '@/components/common/PageContainer.vue'
import { Search } from '@element-plus/icons-vue'

const departments = ref<Department[]>([])
const keyword = ref('')
const loading = ref(false)

const filteredDepartments = computed(() =>
  departments.value.filter((dept) => {
    if (!keyword.value) return true
    return dept.name.toLowerCase().includes(keyword.value.toLowerCase())
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listDepartments({ page: 0, size: 100 })
    departments.value = data.content
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.table-shell {
  padding: 24px;
  border-radius: var(--ams-radius-lg, 18px);
}
</style>
