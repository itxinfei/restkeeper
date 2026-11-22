import Vue from 'vue';

import 'normalize.css';
import ElementUI from 'element-ui';
import SvgIcon from 'vue-svgicon';
import VueAreaLinkage from 'vue-area-linkage';

import '@/styles/element-variables.scss';
import '@/styles/index.scss';
import 'vue-area-linkage/dist/index.css';

import App from '@/App.vue';
import store from '@/store';
import router from '@/router';
import '@/icons/components';
import '@/permission';

Vue.use(ElementUI);
Vue.use(VueAreaLinkage);
Vue.use(SvgIcon, {
    'tagName': 'svg-icon',
    'defaultWidth': '1em',
    'defaultHeight': '1em'
});

Vue.config.productionTip = false;

new Vue({
    router,
    store,
    'render': (h) => h(App)
}).$mount('#app');
