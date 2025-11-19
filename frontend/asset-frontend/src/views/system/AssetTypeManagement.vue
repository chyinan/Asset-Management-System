<template>
  <PageContainer title="资产类型库" description="规范资产分类，统一编码与配置信息" eyebrow="系统管理">
    <template #actions>
      <el-input
        v-model="keyword"
        placeholder="搜索名称或编码"
        clearable
        style="width: 240px"
        :prefix-icon="Search"
      />
      <el-tag type="info" effect="plain">共 {{ assetTypes.length }} 类</el-tag>
    </template>

    <div class="table-shell surface-card">
      <el-table v-if="filteredAssetTypes.length" :data="filteredAssetTypes" v-loading="loading" stripe>
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column label="编码" width="160">
          <template #default="{ row }">
            <el-tag type="primary" effect="dark">{{ row.code || 'N/A' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" />
      </el-table>
      <el-skeleton v-else-if="loading" :rows="5" animated />
      <el-empty v-else description="暂无资产类型" />
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import { listAssetTypes } from '@/api/modules/basic'
import type { AssetType } from '@/types/domain'
import { Search } from '@element-plus/icons-vue'

const assetTypes = ref<AssetType[]>([])
const keyword = ref('')
const loading = ref(false)

const filteredAssetTypes = computed(() =>
  assetTypes.value.filter((item) => {
    if (!keyword.value) return true
    const target = keyword.value.toLowerCase()
    const name = item.name?.toLowerCase?.() || ''
    const code = item.code?.toLowerCase?.() || ''
    const remark = item.remark?.toLowerCase?.() || ''
    return name.includes(target) || code.includes(target) || remark.includes(target)
  })
)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await listAssetTypes({ page: 0, size: 100 })
    assetTypes.value = data.content
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
