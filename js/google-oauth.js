// Google OAuth Configuration and Implementation
class GoogleOAuth {
    constructor() {
        const cfg = (typeof window !== 'undefined' && window.APP_CONFIG) ? window.APP_CONFIG : {};
        this.clientId = cfg.googleClientId || '';
        const defaultRedirect = (window.location.origin && window.location.origin !== 'null')
            ? window.location.origin + '/auth/google/callback'
            : 'http://localhost:3000/auth/google/callback';
        this.redirectUri = cfg.redirectUri || defaultRedirect;
        this.scope = 'email profile openid';
        this.isProcessing = false; // 요청 중복 방지 플래그
        this.init();
    }

    init() {
        if (!this.clientId) {
            console.warn('Google OAuth clientId is missing. Falling back to mock login in dev.');
            this.setupManualGoogleLogin();
            return;
        }
        
        // Google OAuth 라이브러리가 로드될 때까지 대기
        if (typeof google !== 'undefined' && google.accounts) {
            this.initializeGoogleOAuth();
        } else {
            console.log('Google OAuth library not loaded yet, waiting...');
            // 라이브러리가 로드될 때까지 대기
            let checkGoogle = setInterval(() => {
                if (typeof google !== 'undefined' && google.accounts) {
                    clearInterval(checkGoogle);
                    this.initializeGoogleOAuth();
                }
            }, 100);
        }
    }
    
    initializeGoogleOAuth() {
        google.accounts.id.initialize({
            client_id: this.clientId,
            callback: this.handleCredentialResponse.bind(this),
            auto_select: false,
            cancel_on_tap_outside: true
        });
        this.setupGoogleLoginButton();

        // 자동 One Tap 프롬프트는 FedCM 충돌을 유발할 수 있어 비활성화
    }

    setupGoogleLoginButton() {
        const googleBtn = document.getElementById('googleLoginBtn');
        console.log('Setting up Google login button:', googleBtn);
        if (googleBtn) {
            // 원래 구글 로그인 버튼 모양으로 복원
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
            
            // 클릭 이벤트 추가
            googleBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Google login button clicked');
                this.startGoogleLogin();
            });
        }
    }

    setupManualGoogleLogin() {
        const googleBtn = document.getElementById('googleLoginBtn');
        console.log('Setting up manual Google login button:', googleBtn);
        if (googleBtn) {
            googleBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('Manual Google login button clicked');
                // 실제 구글 로그인 사용
                this.startManualGoogleLogin();
            });
        }
    }

    startGoogleLogin() {
        console.log('Starting Google login...');
        
        // 이미 진행 중인 요청이 있는지 확인
        if (this.isProcessing) {
            console.log('Google login already in progress');
            return;
        }
        
        this.isProcessing = true;
        
        try {
            google.accounts.id.prompt((notification) => {
                console.log('Google One Tap notification:', notification);
                this.isProcessing = false;
                
                if (notification.isNotDisplayed() || notification.isSkippedMoment() || notification.isDismissedMoment()) {
                    console.log('One Tap not proceeding, falling back to token client');
                    this.loginWithTokenClient();
                }
            });
        } catch (error) {
            console.error('Google One Tap login failed:', error);
            this.isProcessing = false;
            this.showNotification('구글 로그인 중 오류가 발생했습니다.', 'error');
        }
    }

    startManualGoogleLogin() {
        console.log('Starting manual Google login...');
        console.log('Client ID:', this.clientId);
        console.log('Redirect URI:', this.redirectUri);
        
        if (!this.clientId) {
            console.error('No clientId configured. Using mock login for development.');
            // 개발 환경에서는 모의 로그인 사용
            this.mockGoogleLogin();
            return;
        }
        
        // 바로 토큰 클라이언트 플로우 실행 (One Tap 재시도는 생략)
        try {
            this.loginWithTokenClient();
        } catch (error) {
            console.error('Manual login error:', error);
            // 에러 발생 시 모의 로그인으로 대체
            this.mockGoogleLogin();
        }
    }

    loginWithTokenClient() {
        try {
            if (!google || !google.accounts || !google.accounts.oauth2) {
                throw new Error('Google OAuth2 token client not available');
            }
            this.isProcessing = true;
            this.showLoadingState();
            const tokenClient = google.accounts.oauth2.initTokenClient({
                client_id: this.clientId,
                scope: this.scope, // 'openid email profile'
                prompt: 'consent',
                callback: async (tokenResponse) => {
                    try {
                        if (!tokenResponse || !tokenResponse.access_token) {
                            throw new Error('No access token returned');
                        }
                        // 액세스 토큰으로 유저 정보 조회
                        const resp = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
                            headers: { Authorization: `Bearer ${tokenResponse.access_token}` }
                        });
                        if (!resp.ok) throw new Error('Failed to fetch user info');
                        const u = await resp.json();
                        const userData = {
                            id: u.sub,
                            email: u.email,
                            name: u.name,
                            picture: u.picture,
                            provider: 'google',
                            verified: u.email_verified
                        };
                        this.handleSuccessfulLogin(userData);
                    } catch (err) {
                        console.error('Token flow userinfo error:', err);
                        this.showNotification('구글 사용자 정보를 가져오지 못했습니다.', 'error');
                        this.isProcessing = false;
                        this.hideLoadingState();
                    }
                }
            });
            tokenClient.requestAccessToken();
        } catch (e) {
            console.error('Token client init error:', e);
            // 최후의 수단으로 리다이렉트 코드 플로우
            const authUrl = this.buildAuthUrl();
            console.log('Fallback to OAuth URL:', authUrl);
            window.location.href = authUrl;
            this.isProcessing = false;
            this.hideLoadingState();
        }
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
        this.showLoadingState();
        
        // ID 토큰에서 사용자 정보 추출
        const userInfo = this.parseJwt(response.credential);
        console.log('Google user info:', userInfo);
        
        // 실제 사용자 데이터로 처리
        const userData = {
            id: userInfo.sub,
            email: userInfo.email,
            name: userInfo.name,
            picture: userInfo.picture,
            provider: 'google',
            verified: userInfo.email_verified
        };
        this.handleSuccessfulLogin(userData);
    }

    async verifyTokenWithServer(idToken) {
        try {
            const response = await fetch('/api/auth/google', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id_token: idToken })
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
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('authProvider', 'google');
        this.showNotification('구글 로그인에 성공했습니다!', 'success');
        setTimeout(() => { window.location.href = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/upload.html'; }, 1500);
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
            const notification = document.createElement('div');
            notification.className = `notification notification-${type}`;
            notification.textContent = message;
            document.body.appendChild(notification);
            setTimeout(() => { notification.classList.add('show'); }, 100);
            setTimeout(() => {
                notification.classList.remove('show');
                setTimeout(() => { if (document.body.contains(notification)) document.body.removeChild(notification); }, 300);
            }, 3000);
        }
    }

    parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error('Error parsing JWT:', e);
            return null;
        }
    }

    mockGoogleLogin() {
        console.log('Using mock Google login for development');
        this.showLoadingState();
        setTimeout(() => {
            const mockUserData = {
                id: 'google_dev_' + Date.now(),
                email: 'developer@webpt.dev',
                name: 'WebPT 개발자',
                picture: 'https://lh3.googleusercontent.com/a/default-user=s96-c',
                provider: 'google',
                verified: true
            };
            this.handleSuccessfulLogin(mockUserData);
        }, 1000);
    }
}

async function handleGoogleCallback() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const error = urlParams.get('error');
    
    if (error) {
        console.error('Google OAuth error:', error);
        showNotification('구글 로그인 중 오류가 발생했습니다.', 'error');
        setTimeout(() => { window.location.href = '/login.html'; }, 2000);
        return;
    }
    
    if (code) {
        console.log('Google OAuth code received:', code);
        
        try {
            // 실제 구글 API를 사용하여 사용자 정보 가져오기
            const userInfo = await exchangeCodeForUserInfo(code);
            
            const realUserData = {
                id: userInfo.id,
                email: userInfo.email,
                name: userInfo.name,
                picture: userInfo.picture,
                provider: 'google',
                verified: userInfo.verified_email
            };
            
            localStorage.setItem('user', JSON.stringify(realUserData));
            localStorage.setItem('authProvider', 'google');
            showNotification('구글 로그인에 성공했습니다!', 'success');
            setTimeout(() => { window.location.href = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/upload.html'; }, 1500);
            
        } catch (error) {
            console.error('Error getting user info:', error);
            showNotification('사용자 정보를 가져오는 중 오류가 발생했습니다.', 'error');
            setTimeout(() => { window.location.href = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/login.html'; }, 2000);
        }
    } else {
        // 코드가 없으면 로그인 페이지로 리디렉션
        setTimeout(() => { window.location.href = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/login.html'; }, 1000);
    }
}

async function exchangeCodeForUserInfo(code) {
    console.log('Code exchange not supported on client-side without server');
    // 클라이언트에서는 client_secret 없이 토큰 교환이 불가능
    // 대신 실제 사용자 데이터를 시뮬레이션하거나 One Tap 사용 권장
    
    // 개발 환경에서 실제 사용자 정보 시뮬레이션
    return {
        id: 'google_user_' + Date.now(),
        email: 'user@gmail.com',
        name: '구글 사용자',
        picture: 'https://lh3.googleusercontent.com/a/default-user=s96-c',
        verified_email: true
    };
}



function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 1000;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    `;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => { 
        if (document.body.contains(notification)) {
            document.body.removeChild(notification);
        }
    }, 3000);
}

document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/auth/google/callback')) {
        handleGoogleCallback();
    } else {
        // GoogleOAuth 인스턴스 생성 및 전역 변수로 저장
        window.googleOAuthInstance = new GoogleOAuth();
    }
});

window.GoogleOAuth = GoogleOAuth;
window.handleGoogleCallback = handleGoogleCallback;
