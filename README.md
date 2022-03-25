# PermissionJ

使用Aspect实现的面向切面进行Android动态权限申请

使用：
1. 根build.gradle
```
dependencies {
        classpath "com.hujiang.aspectjx:gradle-android-plugin-aspectjx:2.0.10"
}
```
2. 需要申请权限的module build.gradle
```
plugins {
    id 'kotlin-kapt'
}

// 选配
aspectjx {
    // 需要织入代码的包名
    include 'com.xxx'
    // 不需要织入代码的包名
    exclude 'com.xxx'
    // 关闭AspectJX功能 enabled默认为true，即默认AspectJX生效
    enabled true
}

dependencies {

    implementation 'com.github.Archer1347:PermissionJ:1.0.0'
}
```

3. 申请权限
```
@PermissionRequest(
        permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA],
        requestCode = 1
    )
fun test() {
    Toast.makeText(this, "申请权限成功", Toast.LENGTH_SHORT).show()
}
```

4. 申请权限失败（可选）
```
@PermissionRequestFailed
fun failed(permissionDetail: PermissionDetail) {
     Toast.makeText(this, "申请权限失败", Toast.LENGTH_LONG).show()
}
```
或
```
@PermissionRequestFailed
fun failed(permissionDetail: PermissionDetail) {
        Toast.makeText(
            this, "申请权限失败\n" +
                    "请求码:${permissionDetail.requestCode}\n" +
                    "成功:${permissionDetail.grantedPermissions?.joinToString()}\n" +
                    "失败:${permissionDetail.deniedPermissions?.joinToString()}\n" +
                    "是否勾选了不再提示:${permissionDetail.rejectRemind}", Toast.LENGTH_LONG
        ).show()
}
```

