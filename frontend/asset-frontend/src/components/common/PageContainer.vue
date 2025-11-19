<template>
  <section class="page-container">
    <header v-if="showHeader" class="page-container__head">
      <div class="page-container__titles">
        <p v-if="eyebrow" class="page-container__eyebrow">{{ eyebrow }}</p>
        <div class="page-container__title-row">
          <h2 v-if="title" class="page-container__title">{{ title }}</h2>
          <slot name="tags" />
        </div>
        <p v-if="description" class="page-container__description">{{ description }}</p>
        <slot name="meta" />
      </div>
      <div class="page-container__actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="page-container__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, useSlots } from 'vue'

const props = withDefaults(
  defineProps<{
    title?: string
    description?: string
    eyebrow?: string
  }>(),
  {}
)

const slots = useSlots()
const showHeader = computed(
  () => Boolean(props.title || props.description || props.eyebrow || slots.actions || slots.tags || slots.meta)
)
</script>

<style scoped>
.page-container {
  width: 100%;
  margin: 0 auto var(--ams-spacing-xl, 32px);
  padding: var(--ams-spacing-xl, 32px);
  background: var(--ams-surface-card, #ffffff);
  border-radius: var(--ams-radius-lg, 18px);
  border: 1px solid var(--ams-border-subtle, rgba(15, 23, 42, 0.08));
  box-shadow: var(--ams-shadow-soft, 0 6px 24px rgba(15, 23, 42, 0.08));
}

.page-container__head {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: var(--ams-spacing-md, 16px);
  margin-bottom: var(--ams-spacing-lg, 24px);
}

.page-container__titles {
  max-width: 640px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-container__title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-container__eyebrow {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ams-text-muted, #98a2b3);
}

.page-container__title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--ams-text-primary, #0f172a);
}

.page-container__description {
  margin: 0;
  font-size: 15px;
  color: var(--ams-text-secondary, #475467);
}

.page-container__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.page-container__body {
  display: flex;
  flex-direction: column;
  gap: var(--ams-spacing-lg, 24px);
}

@media (max-width: 768px) {
  .page-container {
    padding: var(--ams-spacing-lg, 24px);
  }

  .page-container__actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>

