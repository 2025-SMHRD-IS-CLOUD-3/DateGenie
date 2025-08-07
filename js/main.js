// Navigation functionality
document.addEventListener('DOMContentLoaded', function() {
    // Mobile menu toggle
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    
    if (hamburger && navMenu) {
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });
    }

    // Smooth scrolling for navigation links
    const navLinks = document.querySelectorAll('.nav-menu a[href^="#"]');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                targetSection.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Navbar background on scroll
    window.addEventListener('scroll', function() {
        const navbar = document.querySelector('.navbar');
        if (window.scrollY > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
    });

    // Animate elements on scroll
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe elements for animation
    const animateElements = document.querySelectorAll('.glow-card, .section-header');
    animateElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });

    // Initialize glow effects
    initGlowEffects();
});

// Button click handlers
function startAnalysis() {
    // Redirect to analysis page or show modal
    alert('분석 페이지로 이동합니다...');
    // window.location.href = 'analysis.html';
}

function showDemo() {
    // Show demo modal or video
    alert('데모를 보여줍니다...');
    // You can implement a modal here
}

// Glow effect for feature cards
function initGlowEffects() {
    // Glow effects disabled
}

// Get glow color RGB values (disabled)
function getGlowColor(color) {
    return '139, 92, 246';
}

// Parallax effect for hero section
function initParallax() {
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const hero = document.querySelector('.hero');
        
        if (hero) {
            const rate = scrolled * -0.5;
            hero.style.transform = `translateY(${rate}px)`;
        }
    });
}

// Initialize parallax effect
document.addEventListener('DOMContentLoaded', function() {
    initParallax();
});

// Form validation (if needed)
function validateForm(formElement) {
    const inputs = formElement.querySelectorAll('input[required], textarea[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = '#ef4444';
            isValid = false;
        } else {
            input.style.borderColor = '#10b981';
        }
    });
    
    return isValid;
}

// Utility functions
function showNotification(message, type = 'info') {
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
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add notification styles dynamically
const notificationStyles = `
    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    }
    
    .notification.show {
        transform: translateX(0);
    }
    
    .notification-info {
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    }
    
    .notification-success {
        background: linear-gradient(135deg, #10b981, #059669);
    }
    
    .notification-error {
        background: linear-gradient(135deg, #ef4444, #dc2626);
    }
`;

const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet); 

// 색상 변경 함수 (t21ff.html 방식)
function initColorTransition() {
    const hero = document.querySelector('.hero');
    if (!hero) return;

    // 그라데이션 하단 색상들 (5가지)
    const bottomColors = [
        '#6e1a65', // 보라색
        '#1e3a8a', // 파란색
        '#7c2d12', // 주황색
        '#059669', // 초록색
        '#dc2626'  // 빨간색
    ];
    let currentIndex = 0;

    function changeBottomColor() {
        currentIndex = (currentIndex + 1) % bottomColors.length;
        const newBottomColor = bottomColors[currentIndex];
        // 가상 요소의 배경만 변경 (하단 색상만)
        hero.style.setProperty('--after-bg', `linear-gradient(to bottom, transparent 0%, ${newBottomColor} 100%)`);
    }

    // 3초마다 하단 색상 변경
    setInterval(changeBottomColor, 3000);
}

// 초기화 함수들 호출
document.addEventListener('DOMContentLoaded', function() {
    initNavigation();
    initSmoothScrolling();
    initScrollAnimations();
    initGlowEffects();
}); 

// 페이지 로드 시 최상단으로 이동
function scrollToTop() {
    window.scrollTo(0, 0);
}

// 여러 이벤트에서 최상단으로 이동
window.addEventListener('load', scrollToTop);
window.addEventListener('DOMContentLoaded', scrollToTop);
window.addEventListener('pageshow', scrollToTop);

// 새로고침 감지 및 강제 스크롤
window.addEventListener('beforeunload', function() {
    sessionStorage.setItem('wasRefreshed', 'true');
});

window.addEventListener('load', function() {
    // 즉시 최상단으로 이동
    window.scrollTo(0, 0);
    
    // 새로고침인 경우 추가로 최상단으로 이동
    if (sessionStorage.getItem('wasRefreshed') === 'true') {
        setTimeout(() => {
            window.scrollTo(0, 0);
        }, 100);
        sessionStorage.removeItem('wasRefreshed');
    }
});

// F5 키 감지
document.addEventListener('keydown', function(e) {
    if (e.key === 'F5' || (e.ctrlKey && e.key === 'r')) {
        sessionStorage.setItem('wasRefreshed', 'true');
    }
}); 

// hue-rotate 값 실시간 모니터링
function monitorHueRotate() {
    const hero = document.querySelector('.hero');
    if (!hero) return;

    // 애니메이션 시작 시간
    const startTime = Date.now();
    const animationDuration = 30000; // 30초

    function updateHueValue() {
        const elapsed = (Date.now() - startTime) % animationDuration;
        const progress = elapsed / animationDuration;
        
        // 애니메이션 진행도에 따른 hue-rotate 값 계산
        let hueValue;
        if (progress <= 0.25) {
            // 0% -> 25%: 0deg -> 40deg
            hueValue = (progress * 4) * 40;
        } else if (progress <= 0.5) {
            // 25% -> 50%: 40deg -> 80deg
            hueValue = 40 + ((progress - 0.25) * 4) * 40;
        } else if (progress <= 0.75) {
            // 50% -> 75%: 80deg -> 40deg
            hueValue = 80 - ((progress - 0.5) * 4) * 40;
        } else {
            // 75% -> 100%: 40deg -> 0deg
            hueValue = 40 - ((progress - 0.75) * 4) * 40;
        }

        console.log(`Hue Rotate: ${hueValue.toFixed(1)}deg (Progress: ${(progress * 100).toFixed(1)}%)`);
    }

    // 100ms마다 업데이트
    setInterval(updateHueValue, 100);
}

// 페이지 로드 시 모니터링 시작
document.addEventListener('DOMContentLoaded', function() {
    monitorHueRotate();
});

// 다크모드 토글 기능
function initDarkMode() {
    const darkModeToggle = document.getElementById('darkModeToggle');
    const body = document.body;
    
    // 저장된 다크모드 상태 불러오기
    const savedDarkMode = localStorage.getItem('darkMode');
    if (savedDarkMode === 'true') {
        body.classList.add('dark-mode');
    }
    
    darkModeToggle.addEventListener('click', () => {
        body.classList.toggle('dark-mode');
        
        // 다크모드 상태 저장
        const isDarkMode = body.classList.contains('dark-mode');
        localStorage.setItem('darkMode', isDarkMode);
    });
}

// 기존 FAQ 토글 기능
document.addEventListener('DOMContentLoaded', function() {
    const faqItems = document.querySelectorAll('.faq-item');
    
    faqItems.forEach(item => {
        const question = item.querySelector('.faq-question');
        const answer = item.querySelector('.faq-answer');
        
        question.addEventListener('click', () => {
            const isActive = item.classList.contains('active');
            
            // 모든 FAQ 아이템 닫기
            faqItems.forEach(otherItem => {
                otherItem.classList.remove('active');
            });
            
            // 클릭된 아이템만 토글
            if (!isActive) {
                item.classList.add('active');
            }
        });
    });
    
    // 다크모드 초기화
    initDarkMode();
}); 