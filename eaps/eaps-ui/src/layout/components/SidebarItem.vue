<template>
  <div v-if="!item.hidden">
    <!-- 没有子菜单的情况 -->
    <template v-if="hasOneShowingChild(item.children, item) && (!onlyOneChild.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown': !isNest}">
          <el-icon v-if="onlyOneChild.meta && onlyOneChild.meta.icon">
            <component :is="onlyOneChild.meta.icon" />
          </el-icon>
          <template #title>
            <span>{{ onlyOneChild.meta.title }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <!-- 有子菜单的情况 -->
    <el-sub-menu v-else :index="resolvePath(item.path)" popper-append-to-body>
      <template #title>
        <el-icon v-if="item.meta && item.meta.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta.title }}</span>
      </template>
      
      <sidebar-item
        v-for="child in item.children"
        :key="child.path"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-sub-menu>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import path from 'path-browserify'
import AppLink from './AppLink.vue'

const props = defineProps({
  // 路由对象
  item: {
    type: Object,
    required: true
  },
  // 基础路径
  basePath: {
    type: String,
    default: ''
  },
  // 是否为嵌套菜单
  isNest: {
    type: Boolean,
    default: false
  }
})

// 唯一子路由
const onlyOneChild = ref(null)

/**
 * 判断是否有一个显示的子菜单
 * @param {Array} children - 子菜单数组
 * @param {Object} parent - 父菜单对象
 * @returns {Boolean} - 是否只有一个显示的子菜单
 */
const hasOneShowingChild = (children = [], parent) => {
  const showingChildren = children.filter(item => {
    if (item.hidden) {
      return false
    } else {
      // 临时设置
      onlyOneChild.value = item
      return true
    }
  })

  // 当只有一个子路由，该子路由默认展示
  if (showingChildren.length === 1) {
    return true
  }

  // 没有子路由则显示父路由
  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true }
    return true
  }

  return false
}

/**
 * 解析路径
 * @param {String} routePath - 路由路径
 * @returns {String} - 完整路径
 */
const resolvePath = (routePath) => {
  if (routePath.startsWith('/')) {
    return routePath
  }
  return path.resolve(props.basePath, routePath)
}
</script>

<style scoped>
.el-menu-item, .el-sub-menu {
  font-size: 14px;
}

.el-menu-item [class^="el-icon-"], .el-sub-menu [class^="el-icon-"] {
  margin-right: 5px;
  width: 24px;
  text-align: center;
  font-size: 18px;
  vertical-align: middle;
}

.el-menu-item .el-icon, .el-sub-menu .el-icon {
  margin-right: 5px;
  width: 24px;
  text-align: center;
  font-size: 18px;
  vertical-align: middle;
}
</style> 