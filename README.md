# NotificationBot
📲 휴대폰에서 발생하는 알림에 대한 특정 응답을 보낼 수 있도록 도와주는 메세지 봇입니다.

> Project Target : Developer<br/>
> 휴대폰 단말 내에 발생하는 모든 알림에 대한 정보를 관찰할 수 있습니다.<br/>
> 이번 프로젝트에서는 카카오톡 알림에 대한 특정 응답을 보낼 수 있도록 구현하였습니다.

## Necessary
- Wear OS by Google 애플리케이션 단말 내 설치 (알림 권한 활성화)
- 프로젝트 애플리케이션 설치 (알림, 저장공간 권한 활성화)
- 외부 서비스의 알림을 받기 위해 특정 채팅방 알림 활성화 (카카오톡 내부 설정)

## Method
- 프로젝트 애플리케이션 실행 전 미리 준비한 response.js 파일을 단말 내 특정 경로에 추가
- response.js 파일을 추가해야하는 특정 경로의 위치는 [내 파일] > [내장 메모리] > [bot 폴더 신규 생성] > response.js 파일 추가
```js
function response(room, msg, sender, isGroupChat, replier) {
    if (msg.equals("Hello")) {
       replier.reply("안녕하세요")
    }
}
```
