import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ArcoVue from '@arco-design/web-vue';
import '@arco-design/web-vue/dist/arco.css';
import App from './App.vue';
import router from './router';
import { setupDirectives } from './directives';
import './styles/index.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ArcoVue);

setupDirectives(app);

app.mount('#app');
