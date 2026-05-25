const Api = {
    getResource: "/api/file/getResource?filePath=",
    checkCode: "/account/checkCode",//验证码
    login: "/account/login",//登录
    register: "/account/register",//注册
    logout: "/account/logout",//退出,
    updateUserInfo: "/account/updateUserInfo",//保存用户信息
    updatePassword: "/account/updatePassword",//修改密码
    getLoginInfo: "/account/getLoginInfo",//获取登录信息
    loadCommendMusic: "/music/loadCommendMusic",//获取推荐音乐
    loadLatestMusic: "/music/loadLatestMusic",//获取最新的音乐
    doGood: "/music/doGood",//音乐点赞
    updatePlayCount: "/music/updatePlayCount",//更新播放数
    getCreation: "/music/getCreation",//做同款
    musicDetail: "/music/musicDetail",//音乐详情
    loadSysDict: "/my/loadSysDict",//获取系统字典
    createMusic: "/my/createMusic",//创作音乐
    loadMyMusic: "/my/loadMyMusic",//获取我的音乐
    loadCreatingMusic: "/my/loadCreatingMusic",//获取创作中的音乐
    uploadMusicCover: "/my/uploadMusicCover",//上传音乐封面
    delMusic: "/my/delMusic",//删除音乐
    changeMusicTitle: "/my/changeMusicTitle",//需改音乐标题
    integralRecords: "/my/integralRecords",//获取用户积分记录
    loadProduct: "/buy/loadProduct",//获取商品
    getPayInfo: "/buy/getPayInfo",//获取支付信息
    checkPayOrder: "/buy/checkPayOrder",//校验支付结果
    havePay: "/buy/havePay",//已经支付
    buyByPayCode: "/buy/buyByPayCode",//付款码支付
    loadUserMusic: "/user/loadUserMusic",//获取用户的音乐
    getUserInfo: "/user/getUserInfo",//获取用户信息
}

export default Api