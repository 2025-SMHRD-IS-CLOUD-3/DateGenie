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