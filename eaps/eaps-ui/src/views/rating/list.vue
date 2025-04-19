<template>
  <div class="rating-list-container">
    <page-header title="评价管理"></page-header>
    <rating-management />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import PageHeader from '@/components/PageHeader/index.vue';
import RatingManagement from './components/RatingManagement.vue';
</script>

<style scoped>
.rating-list-container {
  padding: 20px;
}
</style> 