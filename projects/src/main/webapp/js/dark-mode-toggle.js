/**
 * 다크모드 토글 - 공통 컴포넌트
 * 모든 페이지에서 사용 가능한 다크모드 토글 기능
 */

// 다크모드 상태 관리
const DarkModeToggle = {
    // 로컬 스토리지 키
    STORAGE_KEY: 'darkMode',
    
    // 현재 다크모드 상태 확인
    isDarkMode() {
        return localStorage.getItem(this.STORAGE_KEY) === 'true';
    },
    
    // 다크모드 상태 설정
    setDarkMode(enabled) {
        localStorage.setItem(this.STORAGE_KEY, enabled.toString());
        this.applyDarkMode(enabled);
    },
    
    // 다크모드 토글
    toggle() {
        const newMode = !this.isDarkMode();
        this.setDarkMode(newMode);
        return newMode;
    },
    
    // 다크모드 스타일 적용
    applyDarkMode(enabled) {
        if (enabled) {
            document.body.classList.add('dark-mode');
        } else {
            document.body.classList.remove('dark-mode');
        }
        
        // 페이지별 추가 로직이 있다면 실행
        this.triggerCustomHandlers(enabled);
    },
    
    // 페이지별 커스텀 핸들러 실행
    triggerCustomHandlers(enabled) {
        // 설정 페이지의 다크모드 토글 스위치 동기화
        const settingsToggle = document.getElementById('darkModeToggle');
        if (settingsToggle) {
            settingsToggle.checked = enabled;
        }
        
        // 커스텀 이벤트 발생
        document.dispatchEvent(new CustomEvent('darkModeChanged', {
            detail: { enabled }
        }));
    },
    
    // 다크모드 버튼 HTML 생성
    createButton() {
        const button = document.createElement('button');
        button.className = 'dark-mode-toggle';
        button.setAttribute('aria-label', '다크모드 토글');
        button.setAttribute('title', '다크모드 전환');
        
        button.innerHTML = `
            <i class="fas fa-sun sun-icon" aria-hidden="true"></i>
            <i class="fas fa-moon moon-icon" aria-hidden="true"></i>
        `;
        
        // 클릭 이벤트 리스너
        button.addEventListener('click', () => {
            this.toggle();
        });
        
        // 키보드 접근성
        button.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                this.toggle();
            }
        });
        
        return button;
    },
    
    // 초기화 - 페이지 로드 시 실행
    init() {
        // 기존 버튼이 있는지 확인
        if (document.querySelector('.dark-mode-toggle')) {
            console.log('다크모드 버튼이 이미 존재합니다.');
            return;
        }
        
        // 버튼 생성 및 추가
        const button = this.createButton();
        document.body.appendChild(button);
        
        // 초기 상태 적용
        const initialMode = this.isDarkMode();
        this.applyDarkMode(initialMode);
        
        console.log('다크모드 토글 버튼이 초기화되었습니다. 현재 상태:', initialMode ? '다크' : '라이트');
    },
    
    // 수동으로 버튼 제거 (필요한 경우)
    remove() {
        const button = document.querySelector('.dark-mode-toggle');
        if (button) {
            button.remove();
        }
    }
};

// DOM이 로드되면 자동으로 초기화
document.addEventListener('DOMContentLoaded', function() {
    // FontAwesome이 로드될 때까지 약간 대기
    setTimeout(() => {
        DarkModeToggle.init();
    }, 100);
});

// 전역에서 접근 가능하도록 export
window.DarkModeToggle = DarkModeToggle;