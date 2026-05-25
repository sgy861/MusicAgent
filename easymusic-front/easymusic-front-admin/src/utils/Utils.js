import moment from "moment";
const imageModules = import.meta.glob('@/assets/*/*.{png,jpg,jpeg,svg,mp4,gif}', {
    eager: true
});
moment.locale('zh-cn', {
    months: '一月_二月_三月_四月_五月_六月_七月_八月_九月_十月_十一月_十二月'.split('_'),
    monthsShort: '1月_2月_3月_4月_5月_6月_7月_8月_9月_10月_11月_12月'.split('_'),
    weekdays: '星期日_星期一_星期二_星期三_星期四_星期五_星期六'.split('_'),
    longDateFormat: {
        LT: 'HH:mm',
        LTS: 'HH:mm:ss',
        L: 'YYYY-MM-DD',
        LL: 'YYYY年MM月DD日',
        LLL: 'YYYY年MM月DD日Ah点mm分',
        LLLL: 'YYYY年MM月DD日ddddAh点mm分',
        l: 'YYYY-M-D',
        ll: 'YYYY年M月D日',
        lll: 'YYYY年M月D日 HH:mm',
        llll: 'YYYY年M月D日dddd HH:mm'
    }
});
const formatDate = (timestamp, patten) => {
    const timestampTime = moment(timestamp);
    return timestampTime.format(patten);
}

const seconds2Min = (seconds) => {
    return moment.utc(seconds * 1000).format("mm:ss");
}

const getLocalResource = (resource) => {
    const path = Object.keys(imageModules).find(path => path.includes(resource));
    return path ? imageModules[path].default : '';
}

export default {
    formatDate,
    seconds2Min,
    getLocalResource
}