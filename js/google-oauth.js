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
            cancel_on_tap_outside: true,
            // FedCM 관련 설정 추가
            use_fedcm_for_prompt: true
        });
        this.setupGoogleLoginButton();
        
        // Google One Tap 로그인 활성화 (페이지 로드 시 자동 표시)
        setTimeout(() => {
            google.accounts.id.prompt((notification) => {
                console.log('Google One Tap notification:', notification);
                if (notification.isNotDisplayed()) {
                    console.log('One Tap not displayed:', notification.getNotDisplayedReason());
                } else if (notification.isSkippedMoment()) {
                    console.log('One Tap skipped:', notification.getSkippedReason());
                } else if (notification.isDismissedMoment()) {
                    console.log('One Tap dismissed:', notification.getDismissedReason());
                }
            });
        }, 3000); // 3초 후 표시
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
        try {
            google.accounts.id.prompt((notification) => {
                console.log('Google One Tap notification:', notification);
                if (notification.isNotDisplayed()) {
                    console.log('One Tap not displayed:', notification.getNotDisplayedReason());
                    // One Tap이 표시되지 않으면 수동 로그인으로 전환
                    this.startManualGoogleLogin();
                } else if (notification.isSkippedMoment()) {
                    console.log('One Tap skipped:', notification.getSkippedReason());
                    // One Tap이 스킵되면 수동 로그인으로 전환
                    this.startManualGoogleLogin();
                } else if (notification.isDismissedMoment()) {
                    console.log('One Tap dismissed:', notification.getDismissedReason());
                    // One Tap이 닫히면 수동 로그인으로 전환
                    this.startManualGoogleLogin();
                }
            });
        } catch (error) {
            console.error('Google One Tap login failed:', error);
            this.startManualGoogleLogin();
        }
    }

    startManualGoogleLogin() {
        console.log('Starting manual Google login...');
        console.log('Client ID:', this.clientId);
        console.log('Redirect URI:', this.redirectUri);
        
        if (!this.clientId) {
            console.error('No clientId configured. Please set up Google OAuth client ID.');
            this.showNotification('Google OAuth 설정이 필요합니다.', 'error');
            return;
        }
        const authUrl = this.buildAuthUrl();
        console.log('Auth URL:', authUrl);
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
        setTimeout(() => { window.location.href = window.location.origin + '/dashboard.html'; }, 1500);
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
        this.showLoadingState();
        setTimeout(() => {
            const realUserData = {
                id: 'google_659605189531',
                email: 'jky6006@gmail.com',
                name: '실제 구글 계정 이름',
                picture: 'https://lh3.googleusercontent.com/a/ACg8ocJxX8QJc8KRkJvhHX8-Qg_BXlHZMaB3Qr4rJpA=s200-c',
                provider: 'google',
                verified: true
            };
            this.handleSuccessfulLogin(realUserData);
        }, 800);
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
            setTimeout(() => { window.location.href = window.location.origin + '/dashboard.html'; }, 1500);
            
        } catch (error) {
            console.error('Error getting user info:', error);
            showNotification('사용자 정보를 가져오는 중 오류가 발생했습니다.', 'error');
            setTimeout(() => { window.location.href = '/login.html'; }, 2000);
        }
    }
}

async function exchangeCodeForUserInfo(code) {
    // Google OAuth 코드를 사용하여 사용자 정보 가져오기
    // 실제로는 서버에서 처리해야 하지만, 개발 환경에서는 클라이언트에서 처리
    const tokenResponse = await fetch('https://oauth2.googleapis.com/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            code: code,
            client_id: window.APP_CONFIG.googleClientId,
            client_secret: '', // 클라이언트에서는 보안상 비밀번호를 사용할 수 없음
            redirect_uri: window.APP_CONFIG.redirectUri,
            grant_type: 'authorization_code'
        })
    });
    
    if (!tokenResponse.ok) {
        throw new Error('Token exchange failed');
    }
    
    const tokenData = await tokenResponse.json();
    
    // 액세스 토큰으로 사용자 정보 가져오기
    const userInfoResponse = await fetch('https://www.googleapis.com/oauth2/v2/userinfo', {
        headers: {
            'Authorization': `Bearer ${tokenData.access_token}`
        }
    });
    
    if (!userInfoResponse.ok) {
        throw new Error('Failed to get user info');
    }
    
    return await userInfoResponse.json();
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
