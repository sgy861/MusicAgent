
import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useUserInfoStore = defineStore('userInfo', () => {
    const userInfo = ref({})
    const showLogin = ref(false);
    const lastReloadTime = ref(0);

    const setLoginInfo = (data) => {
        userInfo.value = data;
    }

    const checkLogin = () => {
        if (Object.keys(userInfo.value).length > 0) {
            return true;
        }
        showLogin.value = true;
        return false;
    }

    const updateLastReloadTime = () => {
        lastReloadTime.value = new Date().getTime();
    }

    return { userInfo, showLogin, lastReloadTime, setLoginInfo, checkLogin, updateLastReloadTime }
})