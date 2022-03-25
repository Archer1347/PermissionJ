package com.permission.core.annotation

/**
 * Desc: 权限请求失败
 *
 * @PermissionRequestFailed
 * fun failed(permissionDetail: PermissionDetail) {
 *      Toast.makeText(this, "申请权限失败", Toast.LENGTH_LONG).show()
 * }
 * 或者
 * @PermissionRequestFailed
 * fun failed() {
 *      Toast.makeText(this, "申请权限失败", Toast.LENGTH_LONG).show()
 * }
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/23
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PermissionRequestFailed