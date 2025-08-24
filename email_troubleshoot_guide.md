# 📧 DateGenie 이메일 인증 문제 해결 가이드

## 🔍 1단계: 진단 도구 사용

즉시 다음 URL에 접속하여 시스템 상태를 확인하세요:

```
http://localhost:8081/DateGenie/email-debug.jsp
```

## 🛠️ 2단계: 일반적인 문제 해결

### A. Gmail 앱 패스워드 문제 (90% 확률)

**증상**: "Authentication failed" 또는 "Username and Password not accepted"

**해결법**:
1. [Google 계정 관리](https://myaccount.google.com/) → **보안**
2. **2단계 인증** → **앱 패스워드**
3. 새 앱 패스워드 생성 (메일 앱용)
4. `email.properties` 파일의 password 값 업데이트
5. 서버 재시작

### B. 네트워크 연결 문제 (5% 확률)

**증상**: "Connection timed out" 또는 "Connection refused"

**해결법**:
1. 방화벽에서 `smtp.gmail.com:587` 허용
2. 회사/학교 네트워크의 SMTP 차단 정책 확인
3. VPN 사용 시 VPN 연결 해제 후 테스트

### C. Gmail 계정 보안 설정 (3% 확률)

**증상**: "Less secure app access" 관련 오류

**해결법**:
1. Gmail 계정에 2단계 인증이 **활성화**되어 있는지 확인
2. "보안 수준이 낮은 앱 액세스"는 더 이상 지원되지 않음
3. 반드시 앱 패스워드 사용

### D. 환경변수 설정 (2% 확률)

**환경변수 우선순위**:
1. `EMAIL_USERNAME` 환경변수
2. `EMAIL_PASSWORD` 환경변수  
3. `email.properties` 파일 설정

## 🧪 3단계: 단계별 테스트

### 1. 설정 확인
```
이메일 진단 페이지에서 모든 설정값이 올바른지 확인
```

### 2. 연결 테스트
```
"이메일 서비스 설정 테스트"가 성공하는지 확인
```

### 3. 실제 발송 테스트
```
본인 이메일로 테스트 메일 발송
```

### 4. 회원가입 테스트
```
실제 회원가입 프로세스로 종단간 테스트
```

## 🚨 즉시 해결이 필요한 경우

### 임시 우회 방법 (개발 환경용)

1. **로컬 SMTP 서버 사용**:
   - MailHog나 FakeSMTP 설치
   - `email.properties`에서 SMTP 호스트 변경

2. **콘솔 출력으로 대체**:
   - `EmailService.java`에서 실제 발송 대신 콘솔 출력
   - 개발/테스트 목적으로만 사용

## 📝 문제 해결 후 확인사항

- [ ] 회원가입 시 이메일 발송 성공
- [ ] 이메일 인증 링크 동작 확인
- [ ] 6자리 코드 인증 동작 확인
- [ ] 이메일 인증 완료 후 로그인 가능

## 🔧 추가 지원이 필요한 경우

콘솔 로그에서 다음 정보를 확인하세요:
- `=== DEBUG: Starting email verification ===`
- `Email verification result:` 
- `=== Email Verification Error ===`

이 정보를 바탕으로 더 구체적인 해결책을 제시할 수 있습니다.