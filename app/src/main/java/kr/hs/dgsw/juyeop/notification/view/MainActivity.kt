package kr.hs.dgsw.juyeop.notification.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import kr.hs.dgsw.juyeop.notification.NotificationListenerService
import kr.hs.dgsw.juyeop.notification.R

class MainActivity : AppCompatActivity() {

    object key {
        const val PREFS_KEY = "bot"
        const val PREFS_KEY2 = "state"

        fun getState(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).getBoolean(PREFS_KEY2, false)
        }
        fun setState(context: Context, state: Boolean) {
            context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE).edit().putBoolean(PREFS_KEY2, state).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkSwitchChange()
        permissionSetting()
        if (!checkNotificationAllow()) startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    fun checkSwitchChange() {
        onOffSwitch.isChecked = key.getState(applicationContext)

        if (key.getState(applicationContext)) stateTextView.text = resources.getString(R.string.state_on)
        else stateTextView.text = resources.getString(R.string.state_off)

        onOffSwitch.setOnCheckedChangeListener { view, state ->
            if (state) stateTextView.text = resources.getString(R.string.state_on)
            else stateTextView.text = resources.getString(R.string.state_off)
            key.setState(applicationContext, state)
        }
    }

    fun reloadData(view: View) {
        NotificationListenerService().initialListScript()
        Snackbar.make(mainLayout, "response.js 파일을 리로드하였습니다.", Snackbar.LENGTH_SHORT).show()
    }

    fun permissionSetting() {
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
    fun checkNotificationAllow(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
            .any { enabledPackageName -> enabledPackageName == packageName }
    }
}