package com.permission.core.aspect

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permission.core.util.application
import com.permission.core.util.hasSelfPermissions

/**
 * Desc: 占位fragment，用于接收onActivityResult
 * <p>
 * Author: linjiaqiang
 * Date: 2022/3/23
 */
internal class PermissionRequestFragment : Fragment() {

    private var iPermission: IPermission? = null

    /**
     * Desc: 申请权限
     * <p>
     * Author: linjiaqiang
     * Date: 2022/3/25
     */
    fun requestPermission(permissions: Array<String>, requestCode: Int, iPermission: IPermission) {
        if (hasSelfPermissions(application, *permissions)) {
            iPermission.onPermissionGranted()
        } else {
            this.iPermission = iPermission
            requestPermissions(permissions, requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!grantResults.contains(PackageManager.PERMISSION_DENIED)) {
            iPermission?.onPermissionGranted()
        } else {
            val grantedPermissions = mutableListOf<String>()
            val deniedPermissions = mutableListOf<String>()
            var rejectRemind = false
            grantResults.forEachIndexed { index, result ->
                val permission = permissions[index]
                if (result == PackageManager.PERMISSION_DENIED) {
                    deniedPermissions.add(permission)
                    if (!rejectRemind) {
                        rejectRemind = !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
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
            iPermission?.onPermissionDenied(detail)
            iPermission = null
        }
    }

    companion object {

        private const val TAG = "PermissionRequestFragment"

        /**
         * Desc: 此方法返回一个可用的SpaceFragment，一个activity只会创建一个
         * <p>
         * author: linjiaqiang
         * Date: 2019/11/6
         */
        fun generate(activity: FragmentActivity): PermissionRequestFragment {
            val fragmentManager = activity.supportFragmentManager
            var fragment = fragmentManager.findFragmentByTag(TAG)
            return if (fragment is PermissionRequestFragment) {
                fragment
            } else {
                fragment = PermissionRequestFragment()
                fragmentManager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss()
                fragmentManager.executePendingTransactions()
                fragment
            }
        }
    }
}