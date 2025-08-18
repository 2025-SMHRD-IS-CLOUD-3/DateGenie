# WebPT - AI 기반 연애 분석 서비스

![Status](https://img.shields.io/badge/status-active-success?style=for-the-badge)
![Made with](https://img.shields.io/badge/Made%20with-HTML5%20%7C%20CSS3%20%7C%20JavaScript-orange?style=for-the-badge&logo=javascript)
![Responsive](https://img.shields.io/badge/Responsive-Yes-00c853?style=for-the-badge)
![Dark Mode](https://img.shields.io/badge/Dark%20Mode-Supported-4a148c?style=for-the-badge)

## ✨ 핵심 기능

- **AI 기반 연애 분석**: 카카오톡 대화와 통화 녹음을 분석하여 썸 가능성과 맞춤형 연애 전략 제안
- **소셜 로그인**: 구글 OAuth, 카카오 로그인 지원
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 모든 디바이스 지원
- **모던 UI/UX**: 그라디언트 배경, 애니메이션, 다크 모드 지원

## 🚀 데모

- **Live Demo**: [https://kwanGDss.github.io/WebPT/](https://kwanGDss.github.io/WebPT/)
- **메인 페이지**: `index.html`
- **로그인 페이지**: `login.html`

## 🛠️ 기술 스택

- **Frontend**: HTML5, CSS3, JavaScript (ES6+)
- **Authentication**: Google OAuth 2.0, Kakao Login
- **UI/UX**: Font Awesome, Google Fonts
- **Styling**: CSS Grid, Flexbox, Gradients

## 📁 프로젝트 구조

```
WebPT/
├── index.html              # 메인 랜딩 페이지
├── login.html              # 로그인 페이지
├── signup.html             # 회원가입 페이지
├── dashboard.html          # 대시보드 페이지
├── privacy.html            # 개인정보처리방침
├── terms.html              # 이용약관
├── css/
│   ├── style.css           # 글로벌 스타일
│   ├── login.css           # 로그인 페이지 스타일
│   └── signup.css          # 회원가입 페이지 스타일
├── js/
│   ├── config.js           # 환경 설정
│   ├── config-production.js # 프로덕션 설정
│   ├── google-oauth.js     # Google OAuth 구현
│   ├── login.js            # 로그인 로직
│   ├── signup.js           # 회원가입 로직
│   └── main.js             # 메인 JavaScript
├── auth/
│   └── google/
│       └── callback.html   # Google OAuth 콜백
├── assets/
│   └── images/             # 이미지 파일들
├── scripts/
│   └── auto-commit.ps1     # 자동 커밋 스크립트
├── GOOGLE_OAUTH_SETUP.md   # Google OAuth 설정 가이드
└── README.md               # 프로젝트 설명서
```

## 🔧 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone https://github.com/kwanGDss/WebPT.git
cd WebPT
```

### 2. 웹 서버 실행
```bash
# Python 내장 서버 사용
python -m http.server 5500

# 또는 Node.js http-server 사용
npx http-server -p 5500
```

### 3. 브라우저에서 접속
```
http://localhost:5500
```

## 🔐 인증 설정

### Google OAuth 설정
1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. OAuth 2.0 클라이언트 ID 생성
3. `js/config.js` 파일에서 클라이언트 ID 설정
4. 승인된 리디렉션 URI 설정: `http://localhost:5500/auth/google/callback.html`

### Kakao Login 설정
1. [Kakao Developers](https://developers.kakao.com/)에서 앱 생성
2. JavaScript 키 설정
3. 도메인 등록: `http://localhost:5500`

## 📱 주요 페이지

### 1. 메인 페이지 (index.html)
- 서비스 소개 및 CTA
- AI 기반 분석 기능 소개
- 반응형 그라디언트 디자인

### 2. 로그인 페이지 (login.html)
- 이메일/비밀번호 로그인
- Google OAuth 로그인
- Kakao 로그인
- 회원가입 링크

### 3. 회원가입 페이지 (signup.html)
- 사용자 정보 입력
- 약관 동의
- 소셜 회원가입

### 4. 대시보드 페이지 (dashboard.html)
- 사용자 프로필
- 분석 결과 표시
- 설정 및 관리

## 🎨 디자인 특징

### 반응형 레이아웃
- CSS Grid와 Flexbox 활용
- 미디어 쿼리를 통한 반응형 디자인
- 모바일 우선 접근법

### 모던 UI/UX
- 그라디언트 배경과 애니메이션
- 호버 효과와 트랜지션
- 글래스모피즘 디자인 요소
- 다크 모드 지원

### JavaScript 기능
- 폼 검증 및 실시간 피드백
- 스무스 스크롤링
- 인터랙티브 애니메이션
- 알림 시스템

## 🔄 자동 커밋 스크립트

PowerShell 스크립트로 변경 사항을 자동 커밋합니다.

```powershell
# 백그라운드 실행
Start-Process powershell -WindowStyle Hidden -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File','scripts/auto-commit.ps1','-DebounceSeconds','3'

# 포그라운드 실행
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/auto-commit.ps1 -DebounceSeconds 3
```

## 🚧 개발 예정 기능

- [ ] AI 분석 엔진 연동
- [ ] 대화 파일 업로드 기능
- [ ] 감정 분석 시각화
- [ ] 맞춤형 연애 전략 추천
- [ ] 사용자 프로필 관리
- [ ] 분석 결과 저장 및 히스토리

## 🌐 브라우저 지원

- Chrome (최신 버전)
- Firefox (최신 버전)
- Safari (최신 버전)
- Edge (최신 버전)

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 연락처

프로젝트 관련 문의사항이 있으시면 이슈를 생성해주세요.

---

**WebPT** - AI로 분석하는 연애 전략 ❤️