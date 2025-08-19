"use strict";

// 메인 스크립트를 모듈 형태로 정리하여 기능은 유지하고 구조를 개선합니다.
(function () {
    // Mobile menu toggle
    function initMobileMenu() {
        const hamburger = document.querySelector('.hamburger');
        const navMenu = document.querySelector('.nav-menu');
        if (!hamburger || !navMenu) return;
        hamburger.addEventListener('click', function () {
            const isActive = hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
            hamburger.setAttribute('aria-expanded', String(isActive));
            navMenu.setAttribute('aria-hidden', String(!isActive));
        });

        // 메뉴 클릭 시 자동 닫기(모바일 UX)
        navMenu.querySelectorAll('a[href^="#"]').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('active');
                navMenu.classList.remove('active');
                hamburger.setAttribute('aria-expanded', 'false');
                navMenu.setAttribute('aria-hidden', 'true');
            });
        });
    }

    // Smooth scrolling for navigation links
    function initSmoothScrolling() {
        const navLinks = document.querySelectorAll('.nav-menu a[href^="#"]');
        navLinks.forEach(link => {
            link.addEventListener('click', function (e) {
                e.preventDefault();
                const targetId = this.getAttribute('href');
                const targetSection = document.querySelector(targetId);
                if (targetSection) {
                    targetSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
            });
        });
    }

    // Navbar background on scroll
    function initNavbarStyleOnScroll() {
        function applyNavbarStyle() {
            const navbar = document.querySelector('.navbar');
            if (!navbar) return;
            if (window.scrollY > 50) {
                navbar.style.background = 'rgba(255, 255, 255, 0.98)';
                navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
            } else {
                navbar.style.background = 'rgba(255, 255, 255, 0.95)';
                navbar.style.boxShadow = 'none';
            }
            // update CSS var for navbar height (responsive)
            const height = navbar.getBoundingClientRect().height;
            document.documentElement.style.setProperty('--navbar-height', height + 'px');
        }
        window.addEventListener('scroll', applyNavbarStyle);
        // 초기 상태 반영
        applyNavbarStyle();
        // 화면 리사이즈 시 높이 재계산
        window.addEventListener('resize', applyNavbarStyle);
    }

    // Animate elements on scroll
    function initScrollAnimations() {
        const observerOptions = { threshold: 0.1, rootMargin: '0px 0px -50px 0px' };
        const observer = new IntersectionObserver(function (entries) {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        const animateElements = document.querySelectorAll('.glow-card, .section-header');
        animateElements.forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(el);
        });
    }

    // Glow effect for feature cards (현재 비활성화 유지)
    function initGlowEffects() { /* no-op */ }
    function getGlowColor(color) { return '139, 92, 246'; }

    // Parallax effect for hero section
    function initParallax() {
        window.addEventListener('scroll', function () {
            const scrolled = window.pageYOffset;
            const hero = document.querySelector('.hero');
            if (hero) {
                const rate = scrolled * -0.5;
                hero.style.transform = `translateY(${rate}px)`;
            }
        });
    }

    // Notification utilities (스타일 주입 포함)
    function ensureNotificationStyles() {
        if (document.getElementById('notification-styles')) return;
        const notificationStyles = `
            .notification { position: fixed; top: 20px; right: 20px; padding: 1rem 1.5rem; border-radius: 8px; color: white; font-weight: 500; z-index: 10000; transform: translateX(100%); transition: transform 0.3s ease; }
            .notification.show { transform: translateX(0); }
            .notification-info { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
            .notification-success { background: linear-gradient(135deg, #10b981, #059669); }
            .notification-error { background: linear-gradient(135deg, #ef4444, #dc2626); }
        `;
        const styleSheet = document.createElement('style');
        styleSheet.id = 'notification-styles';
        styleSheet.textContent = notificationStyles;
        document.head.appendChild(styleSheet);
    }

    function showNotification(message, type = 'info') {
        ensureNotificationStyles();
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        document.body.appendChild(notification);
        setTimeout(() => { notification.classList.add('show'); }, 100);
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => { document.body.removeChild(notification); }, 300);
        }, 3000);
    }

    // 색상 변경 함수 (t21ff.html 방식) - 현재 동작은 유지하되 명시적으로 분리
    function initColorTransition() {
        const hero = document.querySelector('.hero');
        if (!hero) return;
        const bottomColors = ['#6e1a65', '#1e3a8a', '#7c2d12', '#059669', '#dc2626'];
        let currentIndex = 0;
        function changeBottomColor() {
            currentIndex = (currentIndex + 1) % bottomColors.length;
            const newBottomColor = bottomColors[currentIndex];
            hero.style.setProperty('--after-bg', `linear-gradient(to bottom, transparent 0%, ${newBottomColor} 100%)`);
        }
        setInterval(changeBottomColor, 3000);
    }

    // 페이지 로드 시 최상단 고정(기존 동작 유지)
    function initScrollToTopPersistence() {
        function scrollToTop() { window.scrollTo(0, 0); }
        window.addEventListener('load', scrollToTop);
        window.addEventListener('DOMContentLoaded', scrollToTop);
        window.addEventListener('pageshow', scrollToTop);
        window.addEventListener('beforeunload', function () {
            sessionStorage.setItem('wasRefreshed', 'true');
        });
        window.addEventListener('load', function () {
            window.scrollTo(0, 0);
            if (sessionStorage.getItem('wasRefreshed') === 'true') {
                setTimeout(() => { window.scrollTo(0, 0); }, 100);
                sessionStorage.removeItem('wasRefreshed');
            }
        });
        document.addEventListener('keydown', function (e) {
            if (e.key === 'F5' || (e.ctrlKey && e.key === 'r')) {
                sessionStorage.setItem('wasRefreshed', 'true');
            }
        });
    }

    // hue-rotate 값 실시간 모니터링 (기존 로그 유지)
    function monitorHueRotate() {
        const hero = document.querySelector('.hero');
        if (!hero) return;
        const startTime = Date.now();
        const animationDuration = 30000; // 30초
        function updateHueValue() {
            const elapsed = (Date.now() - startTime) % animationDuration;
            const progress = elapsed / animationDuration;
            let hueValue;
            if (progress <= 0.25) {
                hueValue = (progress * 4) * 40;
            } else if (progress <= 0.5) {
                hueValue = 40 + ((progress - 0.25) * 4) * 40;
            } else if (progress <= 0.75) {
                hueValue = 80 - ((progress - 0.5) * 4) * 40;
            } else {
                hueValue = 40 - ((progress - 0.75) * 4) * 40;
            }
        }
        setInterval(updateHueValue, 100);
    }

    // 다크모드 토글 기능
    function initDarkMode() {
        const darkModeToggle = document.getElementById('darkModeToggle');
        const body = document.body;
        const savedDarkMode = localStorage.getItem('darkMode');
        if (savedDarkMode === 'true') {
            body.classList.add('dark-mode');
        }
        if (darkModeToggle) {
            darkModeToggle.addEventListener('click', () => {
                body.classList.toggle('dark-mode');
                const isDarkMode = body.classList.contains('dark-mode');
                localStorage.setItem('darkMode', isDarkMode);
            });
        }
    }

    // FAQ 토글 기능
    function initFAQ() {
        const faqItems = document.querySelectorAll('.faq-item');
        if (!faqItems.length) return;
        faqItems.forEach(item => {
            const question = item.querySelector('.faq-question');
            if (!question) return;
            question.addEventListener('click', () => {
                const isActive = item.classList.contains('active');
                faqItems.forEach(otherItem => otherItem.classList.remove('active'));
                if (!isActive) { item.classList.add('active'); }
            });
        });
    }

    // 공개 API (기존 전역 함수 유지)
    function startAnalysis() { alert('분석 페이지로 이동합니다...'); /* window.location.href = 'analysis.html'; */ }
    function showDemo() { alert('데모를 보여줍니다...'); }
    window.startAnalysis = startAnalysis;
    window.showDemo = showDemo;
    window.showNotification = showNotification;

    // 호환성을 위한 기존 함수명 제공 (콘솔 오류 방지)
    function initNavigation() { initMobileMenu(); initNavbarStyleOnScroll(); }
    window.initNavigation = initNavigation;
    window.initSmoothScrolling = initSmoothScrolling;
    window.initScrollAnimations = initScrollAnimations;

    // 초기화
    document.addEventListener('DOMContentLoaded', function () {
        initMobileMenu();
        initSmoothScrolling();
        initNavbarStyleOnScroll();
        initScrollAnimations();
        initGlowEffects();
        initParallax();
        ensureNotificationStyles();
        initFAQ();
        initDarkMode();
        monitorHueRotate();
        initScrollToTopPersistence();
        // 로고 클릭 시 최상단 이동(Inline JS 제거 대체)
        const logoLink = document.getElementById('logoHomeLink');
        if (logoLink) {
            logoLink.addEventListener('click', function (e) {
                e.preventDefault();
                window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
            });
        }
		// "대화 분석 시작하기" 버튼 → 로그인 페이지로 이동
		const startAnalysisBtn = document.querySelector('.hero .btn-primary');
		if (startAnalysisBtn) {
			startAnalysisBtn.addEventListener('click', function (e) {
				e.preventDefault();
				// GitHub Pages 환경 지원
				if (window.location.hostname === '2025-smhrd-is-cloud-3.github.io') {
					window.location.href = 'https://2025-smhrd-is-cloud-3.github.io/DateGenie/login.html';
				} else {
					window.location.href = window.location.origin + '/login.html';
				}
			});
		}
        // 필요 시 색상 전환 효과 활성화
        // initColorTransition();
    });

    // 공통 로그아웃 함수 (전역으로 노출)
    window.logout = function() {
        const authProvider = localStorage.getItem('authProvider');
        
        // 구글 로그아웃
        if (typeof google !== 'undefined' && google.accounts) {
            google.accounts.id.disableAutoSelect();
        }
        
        // 카카오 로그아웃
        if (authProvider === 'kakao' && window.KakaoAuth && typeof window.KakaoAuth.logout === 'function') {
            window.KakaoAuth.logout();
        } else {
            // 카카오가 아니거나 카카오 SDK가 없는 경우 직접 처리
            localStorage.removeItem('user');
            localStorage.removeItem('authProvider');
            
            if (window.showNotification) {
                window.showNotification('로그아웃되었습니다.', 'success');
            }
            
            // 로그인 페이지로 리다이렉트
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 500);
        }
    };

})();