# NotificationBot
📲 휴대폰에서 발생하는 알림에 대한 특정 응답을 보낼 수 있도록 도와주는 메세지 봇입니다.

> Project Target : Developer<br/>
> 휴대폰 단말 내에 발생하는 모든 알림에 대한 정보를 확인할 수 있습니다.<br/>
> 이번 프로젝트에서는 카카오톡 알림에 대해서 특정 응답을 보낼 수 있도록 구현하였습니다.

## Necessary
- Wear OS by Google 애플리케이션 단말 내 설치 (알림 권한 활성화)
- 프로젝트 애플리케이션 설치 (알림, 저장공간 권한 활성화)
- 외부 서비스의 알림을 받기 위해 특정 채팅방 알림 활성화 (카카오톡 내부 설정)
- 특정 명령어 알림에 대한 준비된 동작을 실시하기 위해 JavaScript 파일 준비 (response.js 파일)

## Method
- 프로젝트 애플리케이션 실행 전 미리 준비한 response.js 파일을 단말 내 특정 경로에 추가
- 모바일 상 특정 경로의 위치는 [내 파일] > [내장 메모리] > [bot 폴더 신규 생성] > response.js 파일 추가
- 컴퓨터 상 특정 경로의 위치는 [단말 기기] > [Phone] > [bot 폴더 신규 생성] > response.js 파일 추가

```js
function response(room, msg, sender, isGroupChat, replier) {
    if (msg.equals("/Hello")) {
       replier.reply("안녕하세요 반갑습니다.")
    }
}
```

- 프로젝트 애플리케이션 실행 시 알림 및 저장공간에 관련된 필수 권한 허용
- 앱에 존재하는 On/Off 스위치를 통해 NotificationListenerService 연결 및 해제
- 단말 내 특정 명령어 알림 발생 시 response.js 파일에 준비된 동작을 실시하여 메세지 답변 처리
- response.js 파일이 변경될 시 앱에 존재하는 refresh 버튼을 통해 response.js 파일 리로드 처리</br>

<div align="left">
  <a><img src="https://user-images.githubusercontent.com/49600974/95646602-2e08c200-0b05-11eb-997b-9c0c5b2634c0.png" alt="IMAGE ALT TEXT" width=65%></a>
</div>

## Core Code
### Permisson Setting
- TedPermisson 라이브러리를 활용하여 프로젝트 애플리케이션에 필요한 권한을 요청합니다.

```kotlin
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
```

### Initial Script
- 앞에서 추가한 response.js 파일의 존재 여부를 확인하며 Script 파일을 초기 설정합니다.

```kotlin
fun initialListScript() {
    try {
        val scriptDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + "bot")
        val script = File(scriptDir, "response.js")

        val parseContext = Context.enter()
        parseContext.optimizationLevel = -1

        val rhino = RhinoAndroidHelper().enterContext()
        val scriptReal = parseContext.compileReader(FileReader(script), script.name, 0, null)

        val scope = rhino.initStandardObjects()
        ScriptableObject.defineClass(scope, ApiClass.Utils::class.java, false, true)

        scriptReal.exec(parseContext, scope)
        data.execScope = scope
        data.responder = scope.get("response", scope) as Function

        Context.exit()
    } catch (e: Exception) {
        Log.e("Exception", e.printStackTrace().toString())
        Process.killProcess(Process.myPid())
    }
}
```

### Notification Check
- 단말 내 알림 발생 시 관찰 및 관련 데이터들을 수집하여 다른 메소드로 전달합니다.

```kotlin
override fun onNotificationPosted(sbn: StatusBarNotification) {
    super.onNotificationPosted(sbn)

    with(MainActivity.key) {
        if (!getState(applicationContext)) return
    }

    if (sbn!!.packageName == "com.kakao.talk") {
        val wearableExtender = Notification.WearableExtender(sbn.notification)
        wearableExtender.actions.forEach { action ->
            if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
                if (action.title.toString().toLowerCase().contains("reply") ||
                    action.title.toString().toLowerCase().contains("Reply") ||
                    action.title.toString().toLowerCase().contains("답장")) {
                    data.execContext = applicationContext
                    callResponder(sbn.notification.extras.getString("android.title").toString(), sbn.notification.extras.get("android.text").toString(), action)
                }
            }
        }
    }
}
```

### Message Reply
- response.js 파일에서 특정 명령어에 대한 응답을 보내기 위해 reply 메소드를 호출합니다.

```kotlin
class SessionCacheReplier(val session: Notification.Action) {
    fun reply(value: String) {
        val sendIntent = Intent()
        val msg = Bundle()

        session.remoteInputs.forEach { remoteInput ->
            msg.putCharSequence(remoteInput.resultKey, value)
        }
        RemoteInput.addResultsToIntent(session.remoteInputs, sendIntent, msg)

        try {
            session.actionIntent.send(kr.hs.dgsw.juyeop.notification.NotificationListenerService.data.execContext, 0, sendIntent)
        } catch (e: Exception) {
            Log.e("Exception", e.printStackTrace().toString())
        }
    }
}
```

### Utils Method
- response.js 파일에서 필요로하는 클래스 또는 메소드들을 제공해주는 역할을 합니다.

```kotlin
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
```

## Demo Video
- 아래 사진을 클릭하여 실제 애플리케이션이 작동하는 모습을 확인해보시기 바랍니다.</br>

<div align="left">
  <a href="https://youtu.be/16KTr0SJQAI"><img src="https://user-images.githubusercontent.com/49600974/95648080-9492dd80-0b0f-11eb-8012-fecf6128552a.png" alt="IMAGE ALT TEXT" width=65%></a>
</div>

## Reference
- zxc010613 개발자님의 <a href="https://github.com/zxc010613/jskakaobot">jskakaobot 프로젝트</a>
- ljuwon321 개발자님의 <a href="https://github.com/ljuwon321/ScriptableKakaoBot">ScriptableKakaoBot 프로젝트</a>
- sungbin5304 개발자님의 <a href="https://github.com/sungbin5304/KakaoTalkBotHub">KakaoTalkBotHub 프로젝트</a>
- DarkTornado 개발자님의 <a href="https://github.com/DarkTornado/KakaoTalkBot">KakaoTalkBot 프로젝트</a>
- Astro36 개발자님의 <a href="https://github.com/Astro36/kakaotalk-bots?fbclid=IwAR1LYhHVrxzvxIZ12H-_zjuF0VdZ9XMuQOlvNVPBoxLZGZmCQEZ74uhssL4">kakaotalk-bots 프로젝트</a>
- Violet's Devblog 블로그의 <a href="https://deviolet.tistory.com/entry/메신저봇-가이드-레거시-APIt">메신저봇 가이드 - API(레거시) 글</a>

## Close
- 위 프로젝트에 대해서 궁금한 것이 있다면 Issues 등록해주시면 빠르게 답변하겠습니다.
