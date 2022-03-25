package com.permission.core.aspect

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.permission.core.annotation.PermissionRequest
import com.permission.core.annotation.PermissionRequestFailed
import com.permission.core.util.*
import kotlinx.coroutines.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 * Desc: Aspect切面
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/23
 */
@Aspect
@DelicateCoroutinesApi
class PermissionAspect {

    @Pointcut("execution(@com.permission.core.annotation.PermissionRequest * *(..))" + " && @annotation(permissionRequest)")
    fun requestPermissionMethod(permissionRequest: PermissionRequest) {
    }

    @Around("requestPermissionMethod(permissionRequest)")
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint, permissionRequest: PermissionRequest) {
        if (hasSelfPermissions(application, *permissionRequest.permissions)) {
            try {
                joinPoint.proceed()
            } catch (throwable: Throwable) {
                Log.d(TAG, throwable.localizedMessage.orEmpty())
            }
            return
        }
        requestPermissions(joinPoint, permissionRequest)
    }

    private fun requestPermissions(joinPoint: ProceedingJoinPoint, permissionRequest: PermissionRequest) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            var fragment = generateFragmentFromContext(joinPoint.`this`)
            var activity: Activity? = null
            // 如果上下文无法添加Fragment，则启动一个透明Activity，并添加Fragment
            if (fragment == null) {
                val intent = Intent(application, PermissionRequestActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                application.startActivity(intent)
                activity = awaitActivityCreate(application, PermissionRequestActivity::class.java)
                fragment = PermissionRequestFragment.generate(activity)
            }
            fragment.lifecycleScope.launchWhenResumed {
                // 使用fragment发起权限请求
                fragment.requestPermission(permissionRequest.permissions as Array<String>, permissionRequest.requestCode, object : IPermission {
                    override fun onPermissionGranted() {
                        try {
                            joinPoint.proceed()
                        } catch (throwable: Throwable) {
                            Log.d(TAG, throwable.localizedMessage.orEmpty())
                        } finally {
                            activity?.finish()
                        }
                    }

                    override fun onPermissionDenied(permissionDetail: PermissionDetail) {
                        try {
                            onDenied(joinPoint.`this`, permissionDetail)
                        } catch (throwable: Throwable) {
                            Log.d(TAG, throwable.localizedMessage.orEmpty())
                        } finally {
                            activity?.finish()
                        }
                    }
                })
            }
        }
    }

    private fun onDenied(any: Any, permissionDetail: PermissionDetail) {
        val cls: Class<*> = any.javaClass
        val methods = cls.declaredMethods
        if (methods.isEmpty()) return
        methods.firstOrNull {
            it.isAnnotationPresent(PermissionRequestFailed::class.java)
        }?.apply {
            isAccessible = true
            val types = parameterTypes
            if (types.isEmpty()) {
                invoke(any)
            } else if (types.size == 1) {
                invoke(any, permissionDetail)
            }
        }
    }

    /**
     * Desc: 从上下文中生成Fragment
     * <p>
     * Author: linjiaqiang
     * Date: 2022/3/25
     */
    private fun generateFragmentFromContext(any: Any): PermissionRequestFragment? {
        if (any is Fragment) {
            val activity = any.activity
            if (activity != null && !activity.isDestroyed) {
                return PermissionRequestFragment.generate(activity)
            }
        }
        if (any is FragmentActivity) {
            return PermissionRequestFragment.generate(any)
        }
        val curActivity = getCurrentActivity()
        if (curActivity is FragmentActivity) {
            return PermissionRequestFragment.generate(curActivity)
        }
        return null
    }
}