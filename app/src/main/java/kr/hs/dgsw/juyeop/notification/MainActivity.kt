package kr.hs.dgsw.juyeop.notification

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionSetting()
        if (!isNotificationPermissionAllowed()) startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    private fun isNotificationPermissionAllowed(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
            .any { enabledPackageName -> enabledPackageName == packageName }
    }

    private fun permissionSetting() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                NotificationListenerService().initialListScript()
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("response.js 파일을 접근하기 위해 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
        }
    }
}