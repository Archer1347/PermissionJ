package com.permission.core.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.permission.core.aspect.PermissionDetail
import java.lang.reflect.Field
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal const val TAG = "PermissionAspect"

internal val application by lazy {
    @SuppressLint("PrivateApi") val activityThread = Class.forName("android.app.ActivityThread")
    val thread = activityThread.getMethod("currentActivityThread").invoke(null)
    val app = activityThread.getMethod("getApplication").invoke(thread) ?: throw NullPointerException("u should init first")
    app as Application
}

/**
 * Desc: 获取栈顶activity
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/30
 */
@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
internal fun getTopActivity(): Activity? {
    try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
            null
        )
        val activitiesField: Field = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        val activities = activitiesField.get(activityThread) as Map<*, *>
        for (activityRecord in activities.values) {
            val activityRecordClass: Class<*> = activityRecord!!.javaClass
            val pausedField: Field = activityRecordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (!pausedField.getBoolean(activityRecord)) {
                val activityField: Field = activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField.get(activityRecord) as? Activity
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * Desc: 等待activity创建并返回activity实例
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/30
 */
internal suspend fun <T> awaitStartActivityAndCreate(context: Context, activityClass: Class<T>): T {
    return suspendCoroutine {
        val intent = Intent(application, activityClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        application.startActivity(intent)
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity::class.java.simpleName == activityClass.simpleName) {
                    (context.applicationContext as Application).unregisterActivityLifecycleCallbacks(this)
                    it.resume(activity as T)
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }
}

/**
 * Desc: 批量申请权限
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/30
 *
 * @param permissions 权限列表
 * @param requestCode 请求码
 *
 * @return 权限请求结果详情[PermissionDetail]
 */
internal suspend fun FragmentActivity.requestPermissionsForResult(permissions: Array<String>, requestCode: Int): PermissionDetail {
    return suspendCoroutine { continuation ->
        val fragment = Fragment()
        var launch: ActivityResultLauncher<Array<String>>? = null
        launch = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isEmpty()) {
                launch?.unregister()
                continuation.resumeWithException(IllegalArgumentException("权限${permissions.contentToString()}申请结果为空。注意：权限请求只有第一次有效，请检查是否有重复调用权限请求的地方"))
                return@registerForActivityResult
            }
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            var rejectRemind = false
            it.forEach { entry ->
                val permission = entry.key
                if (!entry.value) {
                    deniedPermissions.add(permission)
                    if (!rejectRemind) {
                        rejectRemind = !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    }
                } else {
                    grantedPermissions.add(permission)
                }
            }
            val detail = PermissionDetail(
                requestCode = requestCode,
                grantedPermissions = grantedPermissions,
                deniedPermissions = deniedPermissions,
                rejectRemind = rejectRemind,
            )
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
            continuation.resume(detail)
        }
        supportFragmentManager.beginTransaction().add(fragment, "PermissionRequestFragment").commitAllowingStateLoss()
        fragment.lifecycleScope.launchWhenResumed {
            launch.launch(permissions)
        }
    }
}
