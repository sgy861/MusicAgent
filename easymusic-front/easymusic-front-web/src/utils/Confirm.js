import { ElMessageBox } from 'element-plus'

const Confirm = ({ message, okfun, showCancelBtn = true, showClose = true, okText = '确定', cancelText = '取消', cancelfun }) => {
    ElMessageBox.confirm(message, '提示', {
        "close-on-click-modal": false,
        confirmButtonText: okText,
        cancelButtonText: cancelText,
        showCancelButton: showCancelBtn,
        showClose: showClose,
        type: 'info',
    }).then(async () => {
        if (okfun) {
            okfun();
        }
    }).catch((action) => {
        if (action == "cancel" && cancelfun) {
            cancelfun()
        }
    });
};

const Alert = ({ message, btnText = '确定', okfun }) => {
    ElMessageBox.alert(message, '确定', {
        confirmButtonText: btnText,
        callback: (action) => {
            if (action == "confirm" && okfun) {
                okfun();
            }
        },
    })
}
export {
    Confirm,
    Alert
} 
