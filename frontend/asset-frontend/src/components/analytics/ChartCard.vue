<template>
  <section class="chart-card surface-card">
    <header class="chart-card__head">
      <div>
        <p class="chart-card__eyebrow">{{ eyebrow }}</p>
        <h3>{{ title }}</h3>
        <p v-if="description" class="chart-card__desc">{{ description }}</p>
      </div>
      <slot name="actions" />
    </header>
    <div ref="chartRef" class="chart-card__body" :style="{ height: `${height}px` }"></div>
  </section>
</template>

<script setup lang="ts">
import { watch, nextTick } from 'vue'
import type { EChartsOption } from 'echarts'
import { useEChart } from '@/composables/useEChart'

const props = withDefaults(
  defineProps<{
    title: string
    eyebrow?: string
    description?: string
    option: EChartsOption
    height?: number
  }>(),
  {
    eyebrow: '数据洞察',
    height: 320
  }
)

const { elRef: chartRef, setOption } = useEChart()

watch(
  () => props.option,
  (option) => {
    if (!option) return
    nextTick(() => setOption(option))
  },
  { deep: true, immediate: true }
)
</script>

<style scoped>
.chart-card {
  padding: 20px 24px;
  border-radius: var(--ams-radius-lg, 18px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.chart-card__eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ams-text-muted, #98a2b3);
}

.chart-card h3 {
  margin: 6px 0 4px;
  font-size: 18px;
  font-weight: 600;
}

.chart-card__desc {
  margin: 0;
  font-size: 14px;
  color: var(--ams-text-secondary, #475467);
}

.chart-card__body {
  width: 100%;
}
</style>

