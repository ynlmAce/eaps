import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'

// 样式
import './style.css'
import 'element-plus/dist/index.css'
import 'nprogress/nprogress.css'

// Element Plus
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 权限控制
import './permission'

// SVG 图标
import 'virtual:svg-icons-register'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus, {
  locale: zhCn,
})

app.use(router)
app.use(pinia)

app.mount('#app')
