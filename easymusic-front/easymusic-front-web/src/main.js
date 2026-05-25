import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
//import 'element-plus/dist/index.css'
import { Loading } from '@element-plus/icons-vue'

import "@/assets/icon/iconfont.css"
import '@/assets/base.scss';

import Cover from "@/component/common/Cover.vue"
import Avatar from "@/component/common/Avatar.vue"
import NoData from "@/component/common/NoData.vue"
import DataLoadMoreList from "@/component/common/DataLoadMoreList.vue"
import PlayBtn from "@/component/common/PlayBtn.vue"
import Dialog from "@/component/common/Dialog.vue"
import Switch from "@/component/common/Switch.vue"
import BackBtn from "@/component/common/BackBtn.vue"

import Api from "@/utils/Api.js"
import Utils from "@/utils/Utils"
import Request from "@/utils/Request.js"
import Message from "@/utils/Message.js"
import { Confirm, Alert } from "@/utils/Confirm.js"
import Verify from "@/utils/Verify"

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)


app.component("Loading", Loading)
app.component("Cover", Cover)
app.component("Avatar", Avatar)
app.component("NoData", NoData)
app.component("DataLoadMoreList", DataLoadMoreList)
app.component("PlayBtn", PlayBtn)
app.component("Dialog", Dialog)
app.component("Switch", Switch)
app.component("BackBtn", BackBtn)

app.config.globalProperties.Utils = Utils;
app.config.globalProperties.Api = Api;
app.config.globalProperties.Request = Request;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Confirm = Confirm;
app.config.globalProperties.Alert = Alert;
app.config.globalProperties.Verify = Verify;

app.config.globalProperties.imageAccept = ".jpg,.png,.gif,.bmp,.webp";
app.mount('#app')
