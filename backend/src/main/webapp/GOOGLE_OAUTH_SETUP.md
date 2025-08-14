# Google OAuth 설정 가이드

이 프로젝트에서 구글 로그인을 사용하기 위한 설정 방법을 안내합니다.

## 1. Google Cloud Console 설정

### 1.1 프로젝트 생성
1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택

### 1.2 OAuth 동의 화면 설정
1. **API 및 서비스** > **OAuth 동의 화면**으로 이동
2. 사용자 유형 선택 (외부 또는 내부)
3. 앱 정보 입력:
   - 앱 이름: "Date Genie"
   - 사용자 지원 이메일
   - 개발자 연락처 정보
4. 범위 추가:
   - `email`
   - `profile`
   - `openid`

### 1.3 OAuth 2.0 클라이언트 ID 생성
1. **API 및 서비스** > **사용자 인증 정보**로 이동
2. **사용자 인증 정보 만들기** > **OAuth 2.0 클라이언트 ID** 선택
3. 애플리케이션 유형: **웹 애플리케이션** 선택
4. 승인된 리디렉션 URI 추가:
   - `http://localhost:3000/auth/google/callback` (개발용)
   - `https://yourdomain.com/auth/google/callback` (프로덕션용)

### 1.4 클라이언트 ID 복사
생성된 클라이언트 ID를 복사하여 다음 파일에서 사용:
- `js/google-oauth.js`의 `clientId` 변수

## 2. 코드 설정

### 2.1 클라이언트 ID 업데이트
```javascript
// js/google-oauth.js
class GoogleOAuth {
    constructor() {
        this.clientId = 'YOUR_ACTUAL_GOOGLE_CLIENT_ID'; // 여기에 실제 클라이언트 ID 입력
        // ...
    }
}
```

### 2.2 서버 API 엔드포인트 설정
실제 서버 구현 시 다음 API 엔드포인트가 필요합니다:

#### POST /api/auth/google
- ID 토큰 검증
- 사용자 정보 반환

#### POST /api/auth/google/callback
- 인증 코드를 액세스 토큰으로 교환
- 사용자 정보 반환

## 3. 개발 환경 테스트

### 3.1 로컬 서버 실행
```bash
# Python 간단한 서버 (예시)
python -m http.server 3000

# 또는 Node.js 서버
npm install -g http-server
http-server -p 3000
```

### 3.2 테스트
1. `http://localhost:3000/login.html` 접속
2. "구글로 로그인" 버튼 클릭
3. 개발 환경에서는 모의 로그인이 실행됨

## 4. 프로덕션 배포

### 4.1 도메인 설정
1. Google Cloud Console에서 승인된 리디렉션 URI에 실제 도메인 추가
2. HTTPS 필수 (보안 요구사항)

### 4.2 환경 변수 설정
```bash
# 서버 환경 변수
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
```

## 5. 보안 고려사항

### 5.1 클라이언트 보안
- 클라이언트 ID는 공개되어도 안전함
- 클라이언트 시크릿은 절대 클라이언트에 노출하지 않음

### 5.2 서버 보안
- ID 토큰 검증 필수
- HTTPS 사용 필수
- CSRF 보호 구현

## 6. 문제 해결

### 6.1 일반적인 오류
- **"redirect_uri_mismatch"**: 승인된 리디렉션 URI 확인
- **"invalid_client"**: 클라이언트 ID 확인
- **"access_denied"**: 사용자가 로그인 취소

### 6.2 디버깅
- 브라우저 개발자 도구 콘솔 확인
- 네트워크 탭에서 요청/응답 확인
- Google Cloud Console 로그 확인

## 7. 추가 기능

### 7.1 로그아웃
```javascript
// 구글 로그아웃
google.accounts.id.disableAutoSelect();
localStorage.removeItem('user');
localStorage.removeItem('authProvider');
```

### 7.2 사용자 정보 가져오기
```javascript
// ID 토큰에서 사용자 정보 추출
function parseJwt(token) {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(window.atob(base64));
}
```

## 8. 참고 자료

- [Google Identity Services](https://developers.google.com/identity/gsi/web)
- [OAuth 2.0 가이드](https://developers.google.com/identity/protocols/oauth2)
- [Google Sign-In 가이드](https://developers.google.com/identity/sign-in/web)
