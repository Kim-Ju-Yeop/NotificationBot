package kr.hs.dgsw.juyeop.notification.api

import android.os.StrictMode
import android.util.Log
import org.jsoup.Jsoup
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.annotations.JSStaticFunction
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object ApiClass {

    class Utils: ScriptableObject() {

        override fun getClassName(): String {
            return "Utils"
        }

        companion object {
            @JvmStatic
            @JSStaticFunction
            fun getWebText2(url: String): String {
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)

                try {
                    val con = URL(url).openConnection()
                    val isr = InputStreamReader(con.getInputStream())
                    val br = BufferedReader(isr)
                    val str = br.readLine()
                    br.close()
                    isr.close()

                    Log.e("parsingData", str)
                    return str
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return ""
            }
        }
    }
}