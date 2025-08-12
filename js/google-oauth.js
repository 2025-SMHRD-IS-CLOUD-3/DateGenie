// Google OAuth Configuration and Implementation
class GoogleOAuth {
    constructor() {
        this.clientId = 'YOUR_GOOGLE_CLIENT_ID'; // 실제 Google Cloud Console에서 발급받은 클라이언트 ID로 교체
        this.redirectUri = window.location.origin + '/auth/google/callback';
        this.scope = 'email profile';
        this.init();
    }

    init() {
        // Google OAuth 초기화
        if (typeof google !== 'undefined' && google.accounts) {
            google.accounts.id.initialize({
                client_id: this.clientId,
                callback: this.handleCredentialResponse.bind(this),
                auto_select: false,
                cancel_on_tap_outside: true,
            });

            // 구글 로그인 버튼에 이벤트 리스너 추가
            this.setupGoogleLoginButton();
        } else {
            console.warn('Google OAuth library not loaded');
            // 폴백: 수동 OAuth 플로우
            this.setupManualGoogleLogin();
        }
    }

    setupGoogleLoginButton() {
        const googleBtn = document.getElementById('googleLoginBtn');
        if (googleBtn) {
            googleBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.startGoogleLogin();
            });
        }
    }

    setupManualGoogleLogin() {
        const googleBtn = document.getElementById('googleLoginBtn');
        if (googleBtn) {
            googleBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.startManualGoogleLogin();
            });
        }
    }

    startGoogleLogin() {
        try {
            // Google One Tap 로그인 표시
            google.accounts.id.prompt((notification) => {
                if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
                    // One Tap이 표시되지 않으면 수동 로그인으로 폴백
                    this.startManualGoogleLogin();
                }
            });
        } catch (error) {
            console.error('Google One Tap login failed:', error);
            this.startManualGoogleLogin();
        }
    }

    startManualGoogleLogin() {
        // 수동 OAuth 플로우 시작
        const authUrl = this.buildAuthUrl();
        window.location.href = authUrl;
    }

    buildAuthUrl() {
        const params = new URLSearchParams({
            client_id: this.clientId,
            redirect_uri: this.redirectUri,
            scope: this.scope,
            response_type: 'code',
            access_type: 'offline',
            prompt: 'consent'
        });

        return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
    }

    handleCredentialResponse(response) {
        console.log('Google OAuth response received');
        
        // 로딩 상태 표시
        this.showLoadingState();
        
        // ID 토큰을 서버로 전송하여 검증
        this.verifyTokenWithServer(response.credential);
    }

    async verifyTokenWithServer(idToken) {
        try {
            // 실제 구현에서는 서버 API 엔드포인트로 토큰을 전송
            const response = await fetch('/api/auth/google', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    id_token: idToken
                })
            });

            if (response.ok) {
                const data = await response.json();
                this.handleSuccessfulLogin(data);
            } else {
                throw new Error('Token verification failed');
            }
        } catch (error) {
            console.error('Token verification error:', error);
            this.handleLoginError('구글 로그인 중 오류가 발생했습니다.');
        }
    }

    handleSuccessfulLogin(userData) {
        // 로그인 성공 처리
        console.log('Google login successful:', userData);
        
        // 사용자 정보를 로컬 스토리지에 저장
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('authProvider', 'google');
        
        // 성공 알림 표시
        this.showNotification('구글 로그인에 성공했습니다!', 'success');
        
        // 대시보드로 리다이렉트
        setTimeout(() => {
            window.location.href = '/dashboard.html';
        }, 1500);
    }

    handleLoginError(message) {
        console.error('Google login error:', message);
        this.showNotification(message, 'error');
        this.hideLoadingState();
    }

    showLoadingState() {
        const googleBtn = document.getElementById('googleLoginBtn');
        if (googleBtn) {
            googleBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 로그인 중...';
            googleBtn.disabled = true;
        }
    }

    hideLoadingState() {
        const googleBtn = document.getElementById('googleLoginBtn');
        if (googleBtn) {
            googleBtn.innerHTML = `
                <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg" aria-hidden="true" focusable="false">
                    <g fill="none">
                        <path d="M17.64 9.2045c0-.638-.057-1.252-.164-1.841H9v3.481h4.844c-.209 1.125-.842 2.08-1.795 2.719v2.258h2.908c1.703-1.568 2.683-3.874 2.683-6.617z" fill="#4285F4"/>
                        <path d="M9 18c2.43 0 4.467-.806 5.956-2.179l-2.908-2.258c-.806.54-1.84.861-3.048.861-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.982 5.481 18 9 18z" fill="#34A853"/>
                        <path d="M3.964 10.713A5.42 5.42 0 0 1 3.679 9c0-.595.102-1.17.285-1.713V4.955H.957A9 9 0 0 0 0 9c0 1.45.348 2.818.957 4.045l3.007-2.332z" fill="#FBBC05"/>
                        <path d="M9 3.579c1.32 0 2.505.454 3.437 1.343l2.578-2.579C13.463.891 11.426 0 9 0 5.481 0 2.438 2.018.957 4.955l3.007 2.332C4.672 5.159 6.656 3.579 9 3.579z" fill="#EA4335"/>
                    </g>
                </svg>
                구글로 로그인
            `;
            googleBtn.disabled = false;
        }
    }

    showNotification(message, type = 'info') {
        if (typeof showNotification === 'function') {
            showNotification(message, type);
        } else {
            // 기본 알림 구현
            const notification = document.createElement('div');
            notification.className = `notification notification-${type}`;
            notification.textContent = message;
            
            document.body.appendChild(notification);
            
            setTimeout(() => {
                notification.classList.add('show');
            }, 100);
            
            setTimeout(() => {
                notification.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(notification)) {
                        document.body.removeChild(notification);
                    }
                }, 300);
            }, 3000);
        }
    }

    // 개발용 모의 로그인 (실제 구현 전 테스트용)
    mockGoogleLogin() {
        this.showLoadingState();
        
        setTimeout(() => {
            const mockUserData = {
                id: 'google_123456',
                email: 'test@example.com',
                name: '테스트 사용자',
                picture: 'https://via.placeholder.com/150',
                provider: 'google'
            };
            
            this.handleSuccessfulLogin(mockUserData);
        }, 2000);
    }
}

// OAuth 콜백 처리
function handleGoogleCallback() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const error = urlParams.get('error');
    
    if (error) {
        console.error('Google OAuth error:', error);
        showNotification('구글 로그인 중 오류가 발생했습니다.', 'error');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 2000);
        return;
    }
    
    if (code) {
        // 인증 코드를 서버로 전송하여 액세스 토큰 교환
        exchangeCodeForToken(code);
    }
}

async function exchangeCodeForToken(code) {
    try {
        const response = await fetch('/api/auth/google/callback', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ code })
        });

        if (response.ok) {
            const data = await response.json();
            // 로그인 성공 처리
            localStorage.setItem('user', JSON.stringify(data.user));
            localStorage.setItem('authProvider', 'google');
            
            showNotification('구글 로그인에 성공했습니다!', 'success');
            setTimeout(() => {
                window.location.href = '/dashboard.html';
            }, 1500);
        } else {
            throw new Error('Token exchange failed');
        }
    } catch (error) {
        console.error('Token exchange error:', error);
        showNotification('로그인 처리 중 오류가 발생했습니다.', 'error');
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 2000);
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // OAuth 콜백 페이지인지 확인
    if (window.location.pathname.includes('/auth/google/callback')) {
        handleGoogleCallback();
    } else {
        // 일반 로그인 페이지에서 Google OAuth 초기화
        const googleOAuth = new GoogleOAuth();
        
        // 개발 환경에서는 모의 로그인 활성화 (실제 구현 시 제거)
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            // 개발용 모의 로그인 버튼 이벤트
            const googleBtn = document.getElementById('googleLoginBtn');
            if (googleBtn) {
                googleBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    googleOAuth.mockGoogleLogin();
                });
            }
        }
    }
});

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.GoogleOAuth = GoogleOAuth;
window.handleGoogleCallback = handleGoogleCallback;
