# NotificationBot
📲 휴대폰에서 발생하는 알림에 대한 특정 응답을 보낼 수 있도록 도와주는 메세지 봇입니다.

> Project Target : Developer<br/>
> 휴대폰 단말 내에 발생하는 모든 알림에 대한 정보를 확인할 수 있습니다.<br/>
> 이번 프로젝트에서는 카카오톡 알림에 대해서 특정 응답을 보낼 수 있도록 구현하였습니다.

## Necessary
- Wear OS by Google 애플리케이션 단말 내 설치 (알림 권한 활성화)
- 프로젝트 애플리케이션 설치 (알림, 저장공간 권한 활성화)
- 외부 서비스의 알림을 받기 위해 특정 채팅방 알림 활성화 (카카오톡 내부 설정)

## Method
- 프로젝트 애플리케이션 실행 전 미리 준비한 response.js 파일을 단말 내 특정 경로에 추가
- 특정 경로의 위치는 [내 파일] > [내장 메모리] > [bot 폴더 신규 생성] > response.js 파일 추가

```js
function response(room, msg, sender, isGroupChat, replier) {
    if (msg.equals("Hello")) {
       replier.reply("안녕하세요")
    }
}
```

- 프로젝트 애플리케이션 실행 시 알림 및 저장공간에 관련된 필수 권한 허용
- 시스템에서 NotificationListenerService 서비스 연결 및 단말 내 알림을 받을 준비
- 단말 내 카톡 알림 발생 시 response.js 파일에 등록한 명령어에 따른 메세지 답변 처리</br>

<div align="left">
  <a><img src="https://user-images.githubusercontent.com/49600974/95354097-00522c00-08ff-11eb-8e91-2df19346bf53.png" alt="IMAGE ALT TEXT" width=65%></a>
</div>

## Core Code
### Permisson Setting
- TedPermisson 라이브러리를 활용하여 프로젝트 애플리케이션에 필요한 권한을 요청합니다.

```kotlin
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

        val scriptReal = parseContext.compileReader(FileReader(script), script.name, 0, null)
        val scope = parseContext.initStandardObjects()

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
override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)

    if (sbn!!.packageName == "com.kakao.talk") {
        val wearableExtender = Notification.WearableExtender(sbn.notification)
        wearableExtender.actions.forEach { action ->
            if (action.remoteInputs != null && action.remoteInputs.isNotEmpty()) {
                if (action.title.toString().toLowerCase().contains("reply") ||
                    action.title.toString().toLowerCase().contains("Reply") ||
                    action.title.toString().toLowerCase().contains("답장")) {
                    kr.hs.dgsw.juyeop.notification.NotificationListenerService.data.execContext = applicationContext
                    callResponder(sbn.notification.extras.getString("android.title").toString(), sbn.notification.extras.get("android.text").toString(), action)
                }
            }
        }
    }
}
```

### Message Reply
- 보통 response.js 파일에 작성한 명령어 코드에서 답변을 수행하지만 reply 메소드를 통해서도 가능합니다.

```kotlin
 class SessionCacheReplier(val session: Notification.Action) {
    init {
        reply("메세지 답장 전송")
    }

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

## Demo Video
- 아래 사진을 클릭하여 실제 애플리케이션이 작동하는 모습을 확인해보시기 바랍니다.</br>

<div align="left">
  <a href="https://www.youtube.com/watch?v=LmNMuACIZEU"><img src="https://user-images.githubusercontent.com/49600974/95349743-5ec8db80-08fa-11eb-9c4e-010da14b3c8d.png" alt="IMAGE ALT TEXT" width=65%></a>
</div>

## Reference
- zxc010613 개발자님의 <a href="https://github.com/zxc010613/jskakaobot">jskakaobot 프로젝트</a>
- ljuwon321 개발자님의 <a href="https://github.com/ljuwon321/ScriptableKakaoBot">ScriptableKakaoBot 프로젝트</a>

## Close
- 위 프로젝트에 대해서 궁금한 것이 있다면 Issues 등록해주시면 빠르게 답변하겠습니다.
