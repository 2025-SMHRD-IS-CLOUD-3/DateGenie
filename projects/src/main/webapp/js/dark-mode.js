// 다크모드 토글 기능
class DarkModeManager {
    constructor() {
        this.init();
    }

    init() {
        this.createToggleButton();
        this.loadTheme();
        this.setupEventListeners();
    }

    // 다크모드 토글 버튼 생성
    createToggleButton() {
        // 기존 버튼이 있으면 제거
        const existingButton = document.getElementById('dark-mode-toggle');
        if (existingButton) {
            existingButton.remove();
        }

        const button = document.createElement('button');
        button.id = 'dark-mode-toggle';
        button.className = 'dark-mode-toggle';
        button.setAttribute('aria-label', '다크모드 토글');
        
        // 초기 아이콘 설정
        this.updateButtonIcon(button);
        
        document.body.appendChild(button);
        return button;
    }

    // 버튼 아이콘 업데이트
    updateButtonIcon(button) {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        
        if (isDark) {
            // 다크모드일 때 태양 아이콘 (밝게 바꿀 수 있도록)
            button.innerHTML = `
                <svg class="icon-sun" viewBox="0 0 24 24" fill="currentColor" stroke="currentColor" stroke-width="1.5">
                    <circle cx="12" cy="12" r="4"/>
                    <path d="m12 2 0 2"/>
                    <path d="m12 20 0 2"/>
                    <path d="m4.93 4.93 1.41 1.41"/>
                    <path d="m17.66 17.66 1.41 1.41"/>
                    <path d="m2 12 2 0"/>
                    <path d="m20 12 2 0"/>
                    <path d="m6.34 17.66-1.41 1.41"/>
                    <path d="m19.07 4.93-1.41 1.41"/>
                </svg>
            `;
            button.className = 'dark-mode-toggle dark';
            button.title = '라이트 모드로 변경';
        } else {
            // 라이트모드일 때 달 아이콘 (어둡게 바꿀 수 있도록)
            button.innerHTML = `
                <svg class="icon-moon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
                    <path d="M19 3v4"/>
                    <path d="M21 5h-4"/>
                </svg>
            `;
            button.className = 'dark-mode-toggle light';
            button.title = '다크 모드로 변경';
        }
    }

    // 테마 로드
    loadTheme() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        this.setTheme(savedTheme);
    }

    // 테마 설정
    setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
        
        // 버튼 아이콘 업데이트
        const button = document.getElementById('dark-mode-toggle');
        if (button) {
            this.updateButtonIcon(button);
        }
        
        // 페이지별 추가 스타일 적용
        this.applyPageSpecificStyles(theme);
    }

    // 페이지별 추가 스타일 적용
    applyPageSpecificStyles(theme) {
        const isDark = theme === 'dark';
        
        // body 스타일 직접 적용
        document.body.style.backgroundColor = isDark ? '#1a1a1a' : '#ffffff';
        document.body.style.color = isDark ? '#ffffff' : '#333333';
        document.body.style.transition = 'background-color 0.3s ease, color 0.3s ease';
        
        // 모든 일반적인 요소들 스타일 적용
        const elements = document.querySelectorAll(`
            .container, .content, .main-content, .panel, .card, 
            .dashboard-card, .stats-card, .sidebar-nav, .nav-container,
            .upload-area, .drop-zone, .analysis-container, .result-section,
            .settings-content, .profile-content, nav.navbar, footer,
            .upload-shell, .dashboard-shell, .settings-shell, .profile-edit-shell,
            .login-container, .signup-container, .privacy-content, .terms-content,
            header, section, article, aside, .wrapper
        `);
        
        elements.forEach(element => {
            if (isDark) {
                element.style.backgroundColor = '#2d2d2d';
                element.style.color = '#ffffff';
                element.style.borderColor = '#404040';
            } else {
                element.style.backgroundColor = '';
                element.style.color = '';
                element.style.borderColor = '';
            }
        });
        
        // 입력 요소들
        const inputs = document.querySelectorAll('input, textarea, select, button:not(#dark-mode-toggle)');
        inputs.forEach(input => {
            if (isDark) {
                input.style.backgroundColor = '#2d2d2d';
                input.style.color = '#ffffff';
                input.style.borderColor = '#404040';
            } else {
                input.style.backgroundColor = '';
                input.style.color = '';
                input.style.borderColor = '';
            }
        });
        
        // 링크 색상
        const links = document.querySelectorAll('a:not(.btn)');
        links.forEach(link => {
            if (isDark) {
                link.style.color = '#66d9ff';
            } else {
                link.style.color = '';
            }
        });
        
        // 텍스트 요소들
        const textElements = document.querySelectorAll('h1, h2, h3, h4, h5, h6, p, span, div, li, td, th, label');
        textElements.forEach(element => {
            if (isDark) {
                element.style.color = '#ffffff';
            } else {
                element.style.color = '';
            }
        });
        
        console.log(`다크모드 ${isDark ? '활성화' : '비활성화'} 완료`);
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        document.addEventListener('click', (e) => {
            if (e.target.id === 'dark-mode-toggle' || e.target.closest('#dark-mode-toggle')) {
                this.toggleTheme();
            }
        });

        // 키보드 접근성
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.id === 'dark-mode-toggle') {
                this.toggleTheme();
            }
        });
    }

    // 테마 토글
    toggleTheme() {
        const button = document.getElementById('dark-mode-toggle');
        if (button) {
            // 회전 애니메이션 추가
            button.classList.add('rotating');
            setTimeout(() => {
                button.classList.remove('rotating');
            }, 500);
        }

        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        this.setTheme(newTheme);
    }

    // 현재 테마 반환
    getCurrentTheme() {
        return document.documentElement.getAttribute('data-theme') || 'light';
    }
}

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', () => {
    window.darkModeManager = new DarkModeManager();
});

// 페이지 로드 시에도 초기화 (DOMContentLoaded가 이미 지난 경우)
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        if (!window.darkModeManager) {
            window.darkModeManager = new DarkModeManager();
        }
    });
} else {
    if (!window.darkModeManager) {
        window.darkModeManager = new DarkModeManager();
    }
}