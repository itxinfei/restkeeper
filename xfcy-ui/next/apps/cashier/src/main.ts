import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { router } from './router'
import '@xfcy/ui/styles/tokens.css'
import '@xfcy/ui/styles/app.css'
import '@xfcy/ui/styles/layout.css'
import './styles/layout.css'

createApp(App).use(createPinia()).use(router).mount('#app')
