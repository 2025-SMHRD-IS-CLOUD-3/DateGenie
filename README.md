# Date Genie — AI로 분석하는 연애 전략 (WebPT)

![Status](https://img.shields.io/badge/status-active-success?style=for-the-badge)
![Made with](https://img.shields.io/badge/Made%20with-HTML5%20%7C%20CSS3%20%7C%20JavaScript-orange?style=for-the-badge&logo=javascript)
![Responsive](https://img.shields.io/badge/Responsive-Yes-00c853?style=for-the-badge)
![Dark Mode](https://img.shields.io/badge/Dark%20Mode-Supported-4a148c?style=for-the-badge)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-blue?style=for-the-badge)

<p align="center">
  <a href="https://kwanGDss.github.io/WebPT/" target="_blank"><img src="https://img.shields.io/badge/Live%20Demo-GitHub%20Pages-8b5cf6?style=for-the-badge&logo=github" alt="Live Demo" /></a>
  <a href="https://github.com/kwanGDss/WebPT" target="_blank"><img src="https://img.shields.io/badge/Repo-WebPT-ec4899?style=for-the-badge&logo=github" alt="Repo" /></a>
</p>

AI가 카카오톡 대화와 통화 녹음을 분석하여 썸 가능성과 맞춤형 연애 전략을 제안하는 웹 랜딩 페이지입니다. 감성적인 그라디언트 UI, 다크 모드, 모바일 내비게이션을 지원합니다.

<<<<<<< HEAD
## ✨ 핵심 기능! !!!!!!!!
=======
## ✨ 핵심 기능! !!!!!!!
>>>>>>> origin/main

- AI 소개 랜딩(히어로/기능/요금제/FAQ)
- 모바일 햄버거 내비, 부드러운 스크롤, 패럴랙스
- 다크 모드 토글 및 상태 영구 저장(LocalStorage)
- FAQ 아코디언 인터랙션
- 반응형 레이아웃 및 애니메이션(IntersectionObserver)

## 🖋️ 타이포/브랜딩

- Body: Noto Sans KR
- Headings/Brand: Noto Serif KR
- 메인 그라디언트: `#ec4899 ↔ #8b5cf6`

## 🚀 데모

- Live: `https://kwanGDss.github.io/WebPT/`
- 메인: `index.html`
- 로그인: `login.html`

## 🛠️ 기술 스택

![HTML5](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-000000?logo=javascript&logoColor=F7DF1E)
![Font Awesome](https://img.shields.io/badge/Icons-Font%20Awesome-339AF0?logo=fontawesome&logoColor=white)

## 📁 폴더 구조

```text
WebPT/
├─ index.html         # 랜딩 페이지
├─ login.html         # 로그인
├─ css/
│  ├─ style.css       # 글로벌/랜딩 스타일
│  └─ login.css       # 로그인 스타일
├─ js/
│  ├─ main.js         # 내비/애니메이션/다크모드/FAQ 등 초기화
│  └─ login.js        # 로그인 UI 검증/알림
├─ images/            # 스크린샷/에셋(선택)
└─ scripts/
   └─ auto-commit.ps1 # 변경 감지 자동 커밋(선택 실행)
```

## 🧩 기능 요약

- Hero 심전도(Heartbeat) 애니메이션, 그라디언트 배경
- Features/Glow Cards, Pricing(Featured 카드), FAQ(아코디언)
- 모바일 햄버거 메뉴 + 접근성 속성(`aria-expanded`, `aria-hidden`)
- 부드러운 스크롤, 스크롤 애니메이션(IntersectionObserver)
- 다크 모드 토글 + LocalStorage 저장

## 🔧 로컬 실행

정적 사이트이므로 다음 중 하나로 실행합니다.

1) 파일 더블클릭
- `index.html`을 브라우저로 열기

2) 간단 서버(예: VS Code Live Server)
- 루트(`WebPT/`)에서 Live Server 실행

## ⚙️ 스크립트: 자동 커밋(선택)

PowerShell 감시 스크립트로 변경 사항을 자동 커밋합니다.

```powershell
# 백그라운드(숨김) 실행 예시
Start-Process powershell -WindowStyle Hidden -ArgumentList '-NoProfile','-ExecutionPolicy','Bypass','-File','scripts/auto-commit.ps1','-DebounceSeconds','3'

# 포그라운드 실행
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/auto-commit.ps1 -DebounceSeconds 3
```

> 커밋 메시지 예: `chore: auto-commit - index.html, css/style.css, ...`

## 🔒 개인정보/보안(초안)

- 데모 단계로, 실제 사용자 데이터는 수집하지 않습니다.
- 개인 데이터 사용 시 비식별화·동의 절차를 포함한 정책 수립 예정.

## 🗺️ 로드맵(예시)

- [ ] 분석 리포트 샘플 페이지 추가
- [ ] 성향 분석 섹션(그래프 샘플) 보강
- [ ] i18n(EN) 지원
- [ ] 접근성 개선(키보드 초점 스타일, 명도 대비)

## 🤝 Contributing

버그 제보/개선 제안/PR 환영합니다. 브랜치 네이밍 예:
- `feature/<요약>` / `fix/<이슈>` / `refactor/<영역>` / `docs/<문서>` / `chore/<작업>`

## 📜 라이선스

현재 저장소 라이선스는 미정입니다. 필요 시 `LICENSE` 파일로 고지합니다.

---

Made with ❤️ for better relationships.

# SomeTalk - AI 기반 연애 분석 서비스

## 프로젝트 개요

SomeTalk은 썸 관계에서 연인 관계로의 발전 가능성을 AI 기반으로 분석하고, 개인의 성격 특성을 반영한 맞춤형 연애 전략을 제공하는 웹 서비스입니다.

## 주요 기능

### 1. 회원 관리 서비스
- 회원가입 및 로그인
- 회원정보 수정 및 탈퇴
- 사용자 프로필(성격 유형, 관심사) 관리

### 2. 데이터 업로드 서비스
- 카카오톡 대화 파일(.txt) 업로드 기능
- 통화 녹음 파일 업로드 및 텍스트 변환(STT)

### 3. 대화 분석 서비스
- 업로드된 대화의 감정 흐름 분석(AI 기반)
- 감정 요약 및 대표 설렘 문장 추출
- 최고 호감일 자동 탐색
- 썸 가능성 점수 산출(0~100%)

### 4. 시각화 및 결과 제공 서비스
- 감정 곡선 그래프 시각화
- 상대방 성격 분석 및 공략법 추천(MBTI 기반)
- LLM 기반 고백 타이밍 및 대화 전략 조언 제공

### 5. 개인화 및 학습 서비스
- 저장 및 누적 데이터 기반 정교화
- 반복 분석 시 개선된 전략 제공(진화형 분석)

## 기술 스택

### 프론트엔드
- HTML5
- CSS3
- JavaScript (ES6+)
- Font Awesome (아이콘)
- Google Fonts (Inter)

### 백엔드 (예정)
- JSP/Servlet
- Oracle Database

### AI/ML (예정)
- FastAPI
- Python

## 프로젝트 구조

```
SomeTalk/
├── index.html          # 메인 랜딩 페이지
├── login.html          # 로그인 페이지
├── css/
│   ├── style.css       # 메인 스타일시트
│   └── login.css       # 로그인 페이지 스타일
├── js/
│   ├── main.js         # 메인 JavaScript
│   └── login.js        # 로그인 페이지 JavaScript
├── images/             # 이미지 파일들
└── README.md           # 프로젝트 설명서
```

## 설치 및 실행

1. 프로젝트 클론
```bash
git clone [repository-url]
cd SomeTalk
```

2. 웹 서버 실행
```bash
# Python 내장 서버 사용
python -m http.server 8000

# 또는 Node.js http-server 사용
npx http-server
```

3. 브라우저에서 접속
```
http://localhost:8000
```

## 페이지 구성

### 1. 메인 페이지 (index.html)
- 히어로 섹션: 서비스 소개 및 CTA
- 기능 소개: AI 기반 분석 기능들
- 서비스 설명: 주요 서비스 상세 설명
- CTA 섹션: 사용자 행동 유도

### 2. 로그인 페이지 (login.html)
- 이메일/비밀번호 로그인
- 소셜 로그인 (카카오, 구글)
- 비밀번호 찾기
- 회원가입 링크

## 주요 특징

### 반응형 디자인
- 모바일, 태블릿, 데스크톱 모든 디바이스 지원
- CSS Grid와 Flexbox 활용
- 미디어 쿼리를 통한 반응형 레이아웃

### 모던 UI/UX
- 그라디언트 배경과 애니메이션
- 호버 효과와 트랜지션
- 글래스모피즘 디자인 요소

### JavaScript 기능
- 폼 검증 및 실시간 피드백
- 스무스 스크롤링
- 인터랙티브 애니메이션
- 알림 시스템

## 개발 예정 기능

### 백엔드 연동
- JSP/Servlet 기반 서버 구현
- Oracle Database 연동
- RESTful API 설계

### AI 분석 기능
- FastAPI 기반 AI 서비스
- 감정 분석 모델
- 대화 패턴 분석
- 성격 유형 분석

### 추가 페이지
- 회원가입 페이지
- 대시보드 페이지
- 분석 결과 페이지
- 프로필 관리 페이지

## 브라우저 지원

- Chrome (최신 버전)
- Firefox (최신 버전)
- Safari (최신 버전)
- Edge (최신 버전)

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 연락처

프로젝트 관련 문의사항이 있으시면 이슈를 생성해주세요.

---

**SomeTalk** - AI로 분석하는 연애 전략 
