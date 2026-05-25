const MUSIC_MODEL = [{
    modelId: "V3",
    desc: "生成最长120s的带人声音乐(0.3元/首)"
}, {
    modelId: "V3.5",
    desc: "生成最长270s的带人声音乐(0.4元/首)"
}]

const MUSIC_GENRE = ["流行", "摇滚", "迪斯科", "电子", "民谣", "放克", "乡村", "爵士", "嘻哈", "金属", "蓝调", "朋克"]
const MUSIC_EMOTION = ["放松", "生气", "快乐", "悲伤", "冷静", "灵感", "神秘", "雄伟", "古怪", "充满活力"]
const MUSIC_SEX = ["女声", "男声"]

const MUSIC_PROMPT = ["一首梦幻的浪漫歌曲，讲述霓虹灯下时间仿佛静止，每一刻都令人难",
    "一首充满力量的主题曲，强烈的嗓音和鼓舞人心的节拍，讲述一起征",
    "一首充满活力的浪漫歌曲，分享一个魔幻之吻",
    "一首反思性歌曲，带有令人难忘的旋律和强有力的重复副歌"]

const getPrompt = () => {
    return MUSIC_PROMPT[Math.floor(Math.random() * MUSIC_PROMPT.length)];
}

export default {
    MUSIC_MODEL,
    MUSIC_GENRE,
    MUSIC_EMOTION,
    MUSIC_SEX,
    getPrompt
}