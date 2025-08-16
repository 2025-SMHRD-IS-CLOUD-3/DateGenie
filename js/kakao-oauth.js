/**
 * 카카오 OAuth 로그인 기능
 * Date Genie - AI 기반 연애 분석 서비스
 */

"use strict";

(function() {
    // 카카오 앱 키 (실제 운영 시에는 환경변수로 관리)
    // 카카오 개발자 콘솔(https://developers.kakao.com/)에서 발급받은 JavaScript 키를 입력하세요
    const KAKAO_APP_KEY = '2919faf31216eeb43597836a0b911aa3'; // 실제 앱 키로 교체 필요
    
    // 개발용 임시 키 (테스트 목적 - 실제 서비스에서는 사용하지 마세요)
    // const KAKAO_APP_KEY = 'demo_key_for_testing';
    
    // 카카오 SDK 초기화
    function initKakaoSDK() {
        console.log('카카오 SDK 초기화 시도...');
        console.log('앱 키:', KAKAO_APP_KEY);
        
        try {
            if (window.Kakao) {
                console.log('카카오 SDK 객체 확인됨');
                
                if (!Kakao.isInitialized()) {
                    console.log('카카오 SDK 초기화 중...');
                    Kakao.init(KAKAO_APP_KEY);
                    console.log('카카오 SDK 초기화 완료');
                    console.log('초기화 상태:', Kakao.isInitialized());
                } else {
                    console.log('카카오 SDK가 이미 초기화됨');
                }
                
                // 카카오 SDK 버전 확인
                if (Kakao.VERSION) {
                    console.log('카카오 SDK 버전:', Kakao.VERSION);
                }
                
            } else {
                console.error('카카오 SDK가 로드되지 않았습니다.');
                alert('카카오 SDK 로딩 실패. 네트워크를 확인해주세요.');
                return false;
            }
        } catch (error) {
            console.error('카카오 SDK 초기화 실패:', error);
            alert('카카오 SDK 초기화 실패: ' + error.message);
            return false;
        }
        return true;
    }

    // 카카오 로그인 함수 (리다이렉트 방식)
    function loginWithKakao() {
        console.log('카카오 로그인 시도 시작...');
        
        if (!window.Kakao) {
            console.error('카카오 SDK가 로드되지 않았습니다.');
            alert('카카오 SDK 로딩 오류입니다. 페이지를 새로고침해주세요.');
            return;
        }
        
        if (!Kakao.isInitialized()) {
            console.error('카카오 SDK가 초기화되지 않았습니다.');
            alert('카카오 SDK 초기화 오류입니다. 앱 키를 확인해주세요.');
            return;
        }

        console.log('카카오 SDK 상태: 정상');
        console.log('현재 도메인:', window.location.origin);
        
        // GitHub Pages와 로컬 환경 모두 지원
        let redirectUri;
        if (window.location.hostname === '2025-smhrd-is-cloud-3.github.io') {
            redirectUri = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/auth/kakao/callback.html';
        } else if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            redirectUri = window.location.origin + '/auth/kakao/callback.html';
        } else {
            // 기타 환경 (예: 다른 도메인)
            redirectUri = window.location.origin + '/DateGenie/auth/kakao/callback.html';
        }
        console.log('리다이렉트 URI:', redirectUri);
        
        // 카카오 인가 코드 요청 URL 생성
        const kakaoAuthUrl = 'https://kauth.kakao.com/oauth/authorize?' + 
            'client_id=' + encodeURIComponent(KAKAO_APP_KEY) +
            '&redirect_uri=' + encodeURIComponent(redirectUri) +
            '&response_type=code' +
            '&scope=profile_nickname,profile_image,account_email';
        
        console.log('카카오 인증 URL:', kakaoAuthUrl);
        console.log('카카오 로그인 페이지로 리다이렉트...');
        
        // 카카오 로그인 페이지로 리다이렉트
        window.location.href = kakaoAuthUrl;
    }

    // 카카오 로그인 상태 확인 함수
    function checkKakaoLoginStatus() {
        if (!window.Kakao || !Kakao.isInitialized()) {
            return false;
        }
        
        const accessToken = Kakao.Auth.getAccessToken();
        console.log('카카오 액세스 토큰:', accessToken);
        
        return !!accessToken;
    }

    // 카카오 로그아웃 함수
    function logoutKakao() {
        console.log('카카오 로그아웃 시도...');
        
        if (!window.Kakao || !Kakao.isInitialized()) {
            console.warn('카카오 SDK가 초기화되지 않았습니다.');
            // 로컬 스토리지만 정리
            localStorage.removeItem('user');
            localStorage.removeItem('authProvider');
            return;
        }

        const accessToken = Kakao.Auth.getAccessToken();
        if (accessToken) {
            Kakao.Auth.logout(function() {
                console.log('카카오 로그아웃 완료');
                localStorage.removeItem('user');
                localStorage.removeItem('authProvider');
                
                if (window.showNotification) {
                    window.showNotification('로그아웃되었습니다.', 'success');
                }
            });
        } else {
            // 토큰이 없으면 로컬 스토리지만 정리
            console.log('카카오 액세스 토큰이 없음, 로컬 스토리지만 정리');
            localStorage.removeItem('user');
            localStorage.removeItem('authProvider');
        }
    }

    // 전역 함수로 노출
    window.KakaoAuth = {
        init: initKakaoSDK,
        login: loginWithKakao,
        logout: logoutKakao,
        checkStatus: checkKakaoLoginStatus
    };

    // DOM이 로드되면 초기화
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            initKakaoSDK();
        });
    } else {
        initKakaoSDK();
    }

})();