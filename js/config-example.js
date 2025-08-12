// 예시 설정 파일
// 이 파일의 내용을 js/config.js에 복사하여 사용하세요

window.APP_CONFIG = {
  // 아래는 예시입니다. 실제 클라이언트 ID로 교체하세요.
  googleClientId: '123456789012-abcdefghijklmnopqrstuvwxyz012345.apps.googleusercontent.com',
  redirectUri: 'http://localhost:8080/auth/google/callback'
};

// 주의사항:
// 1. 클라이언트 ID는 Google Cloud Console에서 생성해야 합니다
// 2. 승인된 JavaScript 원본에 http://localhost:8080을 추가해야 합니다
// 3. 프로덕션 배포 시에는 도메인에 맞게 설정을 변경해야 합니다
