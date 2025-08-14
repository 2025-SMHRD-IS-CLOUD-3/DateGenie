document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const togglePassword = document.querySelector('.toggle-password');
    const passwordInput = document.getElementById('password');
    const emailInput = document.getElementById('email');
    const socialButtons = document.querySelectorAll('.social-btn');

    // Password toggle functionality
    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            const icon = this.querySelector('i');
            icon.classList.toggle('fa-eye');
            icon.classList.toggle('fa-eye-slash');
        });
    }

    // Form validation
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function validatePassword(password) {
        return password.length >= 6;
    }

    function showError(input, message) {
        const wrapper = input.closest('.input-wrapper');
        wrapper.classList.remove('success');
        wrapper.classList.add('error');
        
        // Remove existing error message
        const existingError = wrapper.parentNode.querySelector('.error-message');
        if (existingError) {
            existingError.remove();
        }
        
        // Add new error message
        const errorMessage = document.createElement('div');
        errorMessage.className = 'error-message';
        errorMessage.textContent = message;
        wrapper.parentNode.appendChild(errorMessage);
    }

    function clearError(input) {
        const wrapper = input.closest('.input-wrapper');
        wrapper.classList.remove('error');
        wrapper.classList.remove('success');
        
        const errorMessage = wrapper.parentNode.querySelector('.error-message');
        if (errorMessage) {
            errorMessage.remove();
        }
    }

    function showSuccess(input) {
        const wrapper = input.closest('.input-wrapper');
        wrapper.classList.remove('error');
        wrapper.classList.add('success');
        
        const errorMessage = wrapper.parentNode.querySelector('.error-message');
        if (errorMessage) {
            errorMessage.remove();
        }
    }

    // Real-time validation
    if (emailInput) {
        emailInput.addEventListener('blur', function() {
            const email = this.value.trim();
            if (email === '') {
                showError(this, '이메일을 입력해주세요.');
            } else if (!validateEmail(email)) {
                showError(this, '올바른 이메일 형식을 입력해주세요.');
            } else {
                clearError(this);
                showSuccess(this);
            }
        });

        emailInput.addEventListener('input', function() {
            clearError(this);
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('blur', function() {
            const password = this.value;
            if (password === '') {
                showError(this, '비밀번호를 입력해주세요.');
            } else if (!validatePassword(password)) {
                showError(this, '비밀번호는 최소 6자 이상이어야 합니다.');
            } else {
                clearError(this);
                showSuccess(this);
            }
        });

        passwordInput.addEventListener('input', function() {
            clearError(this);
        });
    }

    // Form submission
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = emailInput.value.trim();
            const password = passwordInput.value;
            const remember = document.getElementById('remember').checked;
            
            // Validate form
            let isValid = true;
            
            if (email === '' || !validateEmail(email)) {
                showError(emailInput, '올바른 이메일을 입력해주세요.');
                isValid = false;
            }
            
            if (password === '' || !validatePassword(password)) {
                showError(passwordInput, '올바른 비밀번호를 입력해주세요.');
                isValid = false;
            }
            
            if (!isValid) {
                return;
            }
            
            // Show loading state
            const submitBtn = loginForm.querySelector('.login-btn');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 로그인 중...';
            submitBtn.disabled = true;
            
            // Simulate API call
            setTimeout(() => {
                // Here you would typically make an API call to your backend
                console.log('Login attempt:', { email, password, remember });
                
                // Simulate successful login
                showNotification('로그인에 성공했습니다!', 'success');
                
                // Redirect to dashboard or main page
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1500);
                
            }, 2000);
        });
    }

    // Social login buttons (카카오만 처리, 구글은 google-oauth.js에서 처리)
    socialButtons.forEach(button => {
        // 구글 로그인 버튼은 google-oauth.js에서 처리하므로 제외
        if (button.id === 'googleLoginBtn') {
            return;
        }
        
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const provider = this.classList.contains('kakao') ? '카카오' : '구글';
            
            // Show loading state
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 로그인 중...';
            this.disabled = true;
            
            // Simulate social login
            setTimeout(() => {
                console.log(`${provider} 로그인 시도`);
                showNotification(`${provider} 로그인을 시도합니다...`, 'info');
                
                // Reset button
                this.innerHTML = originalText;
                this.disabled = false;
            }, 2000);
        });
    });

    // Forgot password link
    const forgotPasswordLink = document.querySelector('.forgot-password');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', function(e) {
            e.preventDefault();
            showNotification('비밀번호 찾기 기능은 준비 중입니다.', 'info');
        });
    }

    // Input focus effects
    const inputs = document.querySelectorAll('.input-wrapper input');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentNode.classList.add('focused');
        });
        
        input.addEventListener('blur', function() {
            this.parentNode.classList.remove('focused');
        });
    });

    // Add focus styles
    const focusStyles = `
        .input-wrapper.focused input {
            border-color: #ec4899;
            box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.1);
        }
        
        .input-wrapper.focused i {
            color: #ec4899;
        }
    `;
    
    const styleSheet = document.createElement('style');
    styleSheet.textContent = focusStyles;
    document.head.appendChild(styleSheet);
});

// Notification function (if not already defined in main.js)
if (typeof showNotification === 'undefined') {
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
    
    // Add notification styles if not already present
    if (!document.querySelector('#notification-styles')) {
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
        styleSheet.id = 'notification-styles';
        styleSheet.textContent = notificationStyles;
        document.head.appendChild(styleSheet);
    }
} 