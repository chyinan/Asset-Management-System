<template>
  <PageContainer title="角色管理" description="查看并维护角色与权限矩阵" eyebrow="系统管理">
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索角色编码/名称"
        clearable
        style="width: 260px"
        :prefix-icon="Search"
      />
    </template>
    <div class="table-shell surface-card">
      <el-table v-if="filteredRoles.length" :data="filteredRoles" v-loading="loading" stripe>
        <el-table-column prop="code" label="编码" width="160" />
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column label="权限">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="perm in row.permissions" :key="perm" size="small">{{ perm }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <el-skeleton v-else-if="loading" :rows="4" animated />
      <el-empty v-else description="暂无角色" />
  </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { listRoles } from '@/api/modules/system'
import PageContainer from '@/components/common/PageContainer.vue'
import { Search } from '@element-plus/icons-vue'

interface RoleRow {
  code: string
  name: string
  permissions: string[]
}

const roles = ref<RoleRow[]>([])
const keyword = ref('')
const loading = ref(false)

const filteredRoles = computed(() =>
  roles.value.filter((role) => {
    if (!keyword.value) return true
    const target = keyword.value.toLowerCase()
    return role.code.toLowerCase().includes(target) || role.name.toLowerCase().includes(target)
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listRoles({ page: 0, size: 50 })
    roles.value = data.content
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
