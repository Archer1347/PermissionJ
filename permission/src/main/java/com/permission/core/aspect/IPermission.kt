package com.permission.core.aspect

/**
 * Desc: 权限申请结果回调
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/25
 */
interface IPermission {

    /**
     * Desc: 同意权限
     * <p>
     * Author: linjiaqiang
     * Date: 2022/3/25
     */
    fun onPermissionGranted()

    /**
     * Desc: 权限被拒绝
     * <p>
     * Author: linjiaqiang
     * Date: 2022/3/25
     *
     * @param permissionDetail 权限申请结果明细
     */
    fun onPermissionDenied(permissionDetail: PermissionDetail)

}