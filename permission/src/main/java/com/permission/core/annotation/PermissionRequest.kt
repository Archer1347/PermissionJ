package com.permission.core.annotation

/**
 * Desc: 权限注解
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/23
 *
 * @PermissionRequest(
 *      permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA],
 *      requestCode = 1
 * )
 * fun test() {
 *      Toast.makeText(this, "申请权限成功", Toast.LENGTH_SHORT).show()
 * }
 *
 * @param permissions 需要申请的权限列表
 * @param requestCode 请求码，不传默认0
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PermissionRequest(vararg val permissions: String, val requestCode: Int = 0)