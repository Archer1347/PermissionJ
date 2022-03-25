package com.permission.core.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.reflect.Field
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal const val TAG = "PermissionAspect"

internal val application by lazy {
    @SuppressLint("PrivateApi") val activityThread = Class.forName("android.app.ActivityThread")
    val thread = activityThread.getMethod("currentActivityThread").invoke(null)
    val app = activityThread.getMethod("getApplication").invoke(thread) ?: throw NullPointerException("u should init first")
    app as Application
}

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
internal fun getCurrentActivity(): Activity? {
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

suspend fun <T> awaitActivityCreate(context: Context, activityClass: Class<T>): T {
    return suspendCoroutine {
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
