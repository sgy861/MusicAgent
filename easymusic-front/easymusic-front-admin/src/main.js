import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import "@/assets/icon/iconfont.css"
import '@/assets/base.scss';

import Table from "@/component/Table.vue"
import Dialog from "@/component/Dialog.vue"
import NoData from "@/component/NoData.vue"
import Cover from "@/component/Cover.vue"
import Avatar from "@/component/Avatar.vue"

import Utils from "@/utils/Utils.js"
import Verify from "@/utils/Verify.js"
import Api from "@/utils/Api.js"
import Request from "@/utils/Request.js"
import Message from "@/utils/Message.js"
import {
    Confirm,
    Alert
} from "@/utils/Confirm.js"


const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.component("Table", Table);
app.component("Dialog", Dialog);
app.component("NoData", NoData);
app.component("Cover", Cover);
app.component("Avatar", Avatar);

app.config.globalProperties.Utils = Utils;
app.config.globalProperties.Verify = Verify;
app.config.globalProperties.Api = Api;
app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Confirm = Confirm;
app.config.globalProperties.imageAccept = ".jpg,.png,.gif,.bmp,.webp";

app.mount('#app')
