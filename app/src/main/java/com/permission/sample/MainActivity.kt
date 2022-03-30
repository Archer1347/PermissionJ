package com.permission.sample

import android.Manifest
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.permission.core.annotation.PermissionRequest
import com.permission.core.annotation.PermissionRequestFailed
import com.permission.core.aspect.PermissionDetail
import com.permission.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            test()
        }
        test()
    }

    @PermissionRequest(
        permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA],
        requestCode = 1
    )
    fun test() {
        Toast.makeText(this, "申请权限成功", Toast.LENGTH_SHORT).show()
    }

    @PermissionRequestFailed
    fun failed(permissionDetail: PermissionDetail) {
        Toast.makeText(
            this, "申请权限失败\n" +
                    "请求码:${permissionDetail.requestCode}\n" +
                    "成功:${permissionDetail.grantedPermissions.joinToString()}\n" +
                    "失败:${permissionDetail.deniedPermissions.joinToString()}\n" +
                    "是否勾选了不再提示:${permissionDetail.rejectRemind}", Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}