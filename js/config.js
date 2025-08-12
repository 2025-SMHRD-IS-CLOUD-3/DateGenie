// App runtime configuration
// 환경에 따라 자동으로 설정을 적용합니다

const isProduction = window.location.hostname === 'kwangdss.github.io' || 
                    window.location.hostname === 'dategenie.shop' || 
                    window.location.hostname === 'www.dategenie.shop';

const isDevelopment = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.protocol === 'file:';

window.APP_CONFIG = {
  // 개발 환경에서도 실제 클라이언트 ID 사용
  googleClientId: '659605189531-q456cob1mu23civhuu85mo8lsqrcnal1.apps.googleusercontent.com',
  
  // 환경별 리디렉션 URI
  redirectUri: isProduction 
    ? (window.location.hostname === 'kwangdss.github.io' 
       ? 'https://kwangdss.github.io/WebPT/auth/google/callback'
       : 'https://dategenie.shop/auth/google/callback')
    : 'http://127.0.0.1:5500/callback.html'
};

// 설정 확인 로그
console.log('Environment:', isProduction ? 'Production' : 'Development');
console.log('Hostname:', window.location.hostname);
console.log('Redirect URI:', window.APP_CONFIG.redirectUri);
