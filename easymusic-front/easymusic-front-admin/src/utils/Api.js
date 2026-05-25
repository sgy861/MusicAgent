const Api = {
    checkCode: "/account/checkCode",//验证码
    login: "/account/login",//登录
    logout: "/account/logout",//退出,
    getResource: "/api/file/getResource?filePath=",
    loadSysDict: "/settings/loadSysDictList",//获取字典列表
    saveSysDict: "/settings/saveSysDict",//保存字典
    delDict: "/settings/delSysDict",//删除字典
    changeDictSort: "/settings/changeSort",//字典排序
    loadProduct: "/product/loadProduct",//商品列表
    saveProduct: "/product/saveProduct",//保存商品
    changeProductSort: "/product/changeProductSort",//修改商品排序
    delProduct: "/product/delProduct",//删除商品
    loadOrder: "/order/loadOrder",//订单
    loadMusic: "/music/loadMusic",//获取音乐列表
    changeMusicCommendType: "/music/changeMusicCommendType",//修改推荐状态
    loadUser: "/user/loadUser",//获取用户列表
    changeUserStatus: "/user/changeUserStatus",//修改用户状态
    changeIntegral: "/user/changeIntegral",//修改用户积分
    loadPaycodeList: "/payCode/loadPayCodeList",//付款码列表
    createCode: "/payCode/createCode",//创建付款码
    delCode: "/payCode/delCode"//删除付款码
}

export default Api