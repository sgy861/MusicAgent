import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useMusicPlayStore = defineStore('musicPlayStore', () => {
    const playing = ref(false);
    const currentPlayTime = ref(0);
    const updateTime = ref(0);
    const currentMusic = ref({})
    //播放模式  单曲循环  循环  默认循环
    const playModeRepeat = ref(false);
    //音乐列表
    const playList = ref([]);
    const play = (data) => {
        if (data.musicId == currentMusic.value.musicId) {
            return;
        }
        currentMusic.value = { ...data }
        updateTime.value = new Date().getTime();
    }

    const setPlayStatus = (status) => {
        playing.value = status
    }

    const updatePlayTime = (time) => {
        currentPlayTime.value = time;
    }

    const savePlayList = (musicList) => {
        playList.value = musicList;
    }

    return {
        currentMusic, playing, updateTime, currentPlayTime, playList, playModeRepeat,
        play, setPlayStatus, updatePlayTime, savePlayList
    }
})
