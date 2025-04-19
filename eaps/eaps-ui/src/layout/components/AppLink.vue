<template>
  <!-- 外部链接 -->
  <a
    v-if="isExternal(to)"
    :href="to"
    target="_blank"
    rel="noopener"
  >
    <slot />
  </a>
  
  <!-- 内部链接 -->
  <router-link
    v-else
    :to="to"
    custom
    v-slot="{ navigate }"
  >
    <a @click="navigate" role="link">
      <slot />
    </a>
  </router-link>
</template>

<script setup>
import { isExternal } from '@/utils/validate'

defineProps({
  to: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
a {
  text-decoration: none;
  color: inherit;
  display: block;
  width: 100%;
}
</style> 