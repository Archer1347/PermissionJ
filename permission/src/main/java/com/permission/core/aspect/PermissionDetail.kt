package com.permission.core.aspect

/**
 * Desc: 权限申请结果明细
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/23
 *
 * @param requestCode 权限请求码
 * @param grantedPermissions 授予权限列表
 * @param deniedPermissions 拒绝权限列表
 * @param rejectRemind 拒绝权限且勾选了不再提示
 */
data class PermissionDetail(
    val requestCode: Int,
    val grantedPermissions: List<String>,
    val deniedPermissions: List<String>,
    val rejectRemind: Boolean,
)