let isEmailVerified = false;

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function validateNickname(nickname){
    // 2~20자, 한글/영문/숫자/언더스코어 허용, 공백/특수문자 불가
    return /^[A-Za-z0-9_가-힣]{2,20}$/.test((nickname||'').trim());
}

function validatePassword(password) {
    // 8~64자, 대문자/소문자/숫자/특수문자 중 3종류 이상 포함, 공백 불가
    if (!password || password.length < 8 || password.length > 64) return false;
    if (/\s/.test(password)) return false;
    const hasLower = /[a-z]/.test(password);
    const hasUpper = /[A-Z]/.test(password);
    const hasDigit = /\d/.test(password);
    const hasSymbol = /[^A-Za-z0-9]/.test(password);
    const kinds = [hasLower, hasUpper, hasDigit, hasSymbol].filter(Boolean).length;
    return kinds >= 3;
}

function showError(input, message) {
    const wrapper = input.closest('.input-wrapper');
    wrapper.classList.remove('success');
    wrapper.classList.add('error');
    const existing = wrapper.parentNode.querySelector('.error-message');
    if (existing) existing.remove();
    const msg = document.createElement('div');
    msg.className = 'error-message';
    msg.textContent = message;
    wrapper.parentNode.appendChild(msg);
}

function clearError(input) {
    const wrapper = input.closest('.input-wrapper');
    wrapper.classList.remove('error');
    wrapper.classList.remove('success');
    const existing = wrapper.parentNode.querySelector('.error-message');
    if (existing) existing.remove();
}

function showSuccess(input) {
    const wrapper = input.closest('.input-wrapper');
    wrapper.classList.remove('error');
    wrapper.classList.add('success');
    const existing = wrapper.parentNode.querySelector('.error-message');
    if (existing) existing.remove();
}

function checkSignupButtonState() {
    const form = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const agreeInput = document.getElementById('agree');
    const signupBtn = form ? form.querySelector('.signup-btn') : null;
    
    if (!signupBtn || !emailInput || !nicknameInput || !passwordInput || !confirmPasswordInput || !agreeInput) {
        return;
    }
    
    const emailValid = isEmailVerified;
    const nicknameValid = validateNickname(nicknameInput.value.trim());
    const passwordValid = validatePassword(passwordInput.value);
    const confirmPasswordValid = passwordInput.value === confirmPasswordInput.value && confirmPasswordInput.value !== '';
    const agreeValid = agreeInput.checked;
    
    signupBtn.disabled = !(emailValid && nicknameValid && passwordValid && confirmPasswordValid && agreeValid);
}

function showEmailVerificationError() {
    const emailInput = document.getElementById('email');
    if (!isEmailVerified && emailInput && emailInput.value.trim() && validateEmail(emailInput.value.trim())) {
        showError(emailInput, '이메일 인증을 완료해주세요.');
    }
}

function validateConfirmPassword(onBlur = false){
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    
    if (!confirmPasswordInput || !passwordInput) return false;
    
    const pw = passwordInput.value;
    const cf = confirmPasswordInput.value;
    
    if (!cf) {
        if (onBlur) showError(confirmPasswordInput, '비밀번호 확인을 입력해주세요.');
        else clearError(confirmPasswordInput);
        return false;
    }
    if (!pw) {
        showError(confirmPasswordInput, '먼저 비밀번호를 입력해주세요.');
        return false;
    }
    if (!validatePassword(pw)) {
        showError(confirmPasswordInput, '비밀번호 조건을 먼저 충족해주세요.');
        return false;
    }
    if (pw !== cf) {
        showError(confirmPasswordInput, '비밀번호가 일치하지 않습니다.');
        return false;
    }
    clearError(confirmPasswordInput);
    showSuccess(confirmPasswordInput);
    return true;
}

function startPollingVerification(email) {
    const emailInput = document.getElementById('email');
    const verifyEmailBtn = document.getElementById('verifyEmailBtn');
    
    const interval = setInterval(() => {
        fetch('/BackEnd/CheckVerificationStatus', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `email=${encodeURIComponent(email)}`
        })
        .then(response => response.json())
        .then(data => {
            if (data.verified) {
                clearInterval(interval);
                isEmailVerified = true;
                if (emailInput) showSuccess(emailInput);
                if (verifyEmailBtn) {
                    verifyEmailBtn.classList.add('verified');
                    verifyEmailBtn.innerHTML = '<i class="fas fa-check"></i> 인증완료';
                    verifyEmailBtn.disabled = true;
                }
                checkSignupButtonState();
                showNotification('이메일 인증이 완료되었습니다!', 'success');
            }
        })
        .catch(error => console.error('Polling error:', error));
    }, 3000);
}

function registerEventListeners() {
    console.log('이벤트 리스너 등록 시작');
    
    const form = document.getElementById('signupForm');
    
    if (form && form.hasAttribute('data-listeners-registered')) {
        console.log('기존 이벤트 리스너 있음 - 제거 후 재등록');
        const newForm = form.cloneNode(true);
        form.parentNode.replaceChild(newForm, form);
        
        const newFormRef = document.getElementById('signupForm');
        registerFormSubmitListener(newFormRef);
    } else if (form) {
        form.setAttribute('data-listeners-registered', 'true');
        registerFormSubmitListener(form);
    }
    
    console.log('이벤트 리스너 등록 완료');
}

function registerFormSubmitListener(form) {
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        console.log('=== 회원가입 폼 제출 시작 ===');
        
        const emailInput = document.getElementById('email');
        const nicknameInput = document.getElementById('nickname');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        const agreeInput = document.getElementById('agree');
        
        let isValid = true;

        const email = emailInput.value.trim();
        const nickname = nicknameInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (!validateEmail(email)) { 
            showError(emailInput,'올바른 이메일을 입력해주세요.'); 
            isValid = false; 
        }
        
        if (!validateNickname(nickname)) { 
            showError(nicknameInput,'닉네임은 2~20자, 한글/영문/숫자/_(언더스코어)만 가능합니다.'); 
            isValid = false; 
        }
        
        if (!validatePassword(password)) { 
            showError(passwordInput,'비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.'); 
            isValid = false; 
        }
        
        if (confirmPassword !== password) { 
            showError(confirmPasswordInput,'비밀번호가 일치하지 않습니다.'); 
            isValid = false; 
        }
        
        if (!agreeInput.checked) { 
            showNotification('약관에 동의해주세요.', 'error'); 
            isValid = false; 
        }
        
        if (!isEmailVerified) { 
            showNotification('이메일 인증을 완료해주세요.', 'error'); 
            isValid = false; 
        }

        if (!isValid) {
            console.log('폼 유효성 검사 실패');
            return;
        }

        const submitBtn = form.querySelector('.signup-btn');
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 가입 중...';
        submitBtn.disabled = true;

        console.log('서버로 폼 제출 시작');
        console.log('전송 데이터:', {
            email: email,
            nickname: nickname,
            password: password ? '****' : null
        });

        form.action = '/BackEnd/JoinService';
        form.method = 'POST';
        
        showNotification('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');
        
        setTimeout(function() {
            form.submit();
        }, 500);
    });
}

window.addEventListener('message', function(event) {
    console.log('메시지 수신:', event.data);
    
    if (event.data && event.data.type === 'emailVerified') {
        console.log('이메일 인증 성공 메시지 수신:', event.data.email);
        
        isEmailVerified = true;
        const emailInput = document.getElementById('email');
        const verifyEmailBtn = document.getElementById('verifyEmailBtn');
        
        if (emailInput) emailInput.value = event.data.email;
        
        if (verifyEmailBtn) {
            verifyEmailBtn.classList.add('verified');
            verifyEmailBtn.innerHTML = '<i class="fas fa-check"></i> 인증완료';
            verifyEmailBtn.disabled = true;
        }
        
        if (emailInput) showSuccess(emailInput);
        checkSignupButtonState();
        showNotification('이메일 인증이 완료되었습니다!', 'success');
        
    } else if (event.data && event.data.type === 'emailAlreadyVerified') {
        console.log('이미 인증된 이메일 메시지 수신:', event.data.email);
        
        isEmailVerified = true;
        const emailInput = document.getElementById('email');
        const verifyEmailBtn = document.getElementById('verifyEmailBtn');
        
        if (emailInput) emailInput.value = event.data.email;
        
        if (verifyEmailBtn) {
            verifyEmailBtn.classList.add('verified');
            verifyEmailBtn.innerHTML = '<i class="fas fa-check"></i> 인증완료';
            verifyEmailBtn.disabled = true;
        }
        
        if (emailInput) showSuccess(emailInput);
        checkSignupButtonState();
        showNotification('이미 인증된 이메일입니다!', 'info');
        
    } else if (event.data && event.data.type === 'emailVerificationError') {
        console.log('이메일 인증 실패 메시지 수신:', event.data.message);
        showNotification(event.data.message || '인증 처리 중 오류가 발생했습니다.', 'error');
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const isVerified = urlParams.get('verified');
    const verifiedEmail = urlParams.get('email');
    const form = document.getElementById('signupForm');
    const emailInput = document.getElementById('email');
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const agreeInput = document.getElementById('agree');
    const toggleButtons = document.querySelectorAll('.toggle-password');
    const verifyEmailBtn = document.getElementById('verifyEmailBtn');
    const signupBtn = form.querySelector('.signup-btn');
    
    if (isVerified === 'true' && verifiedEmail) {
        console.log('이메일 인증 완료된 상태로 페이지 로드:', verifiedEmail);
        
        isEmailVerified = true;
        emailInput.value = decodeURIComponent(verifiedEmail);
        
        verifyEmailBtn.classList.add('verified');
        verifyEmailBtn.innerHTML = '<i class="fas fa-check"></i> 인증완료';
        verifyEmailBtn.disabled = true;
        showSuccess(emailInput);
        
        showNotification('이메일 인증이 완료되었습니다!', 'success');
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    toggleButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.previousElementSibling;
            if (!input) return;
            const isPassword = input.getAttribute('type') === 'password';
            input.setAttribute('type', isPassword ? 'text' : 'password');
            const icon = this.querySelector('i');
            icon.classList.toggle('fa-eye');
            icon.classList.toggle('fa-eye-slash');
        });
    });

    if (emailInput) {
        emailInput.addEventListener('blur', function() {
            const v = this.value.trim();
            if (!v) {
                showError(this, '이메일을 입력해주세요.');
            } else if (!validateEmail(v)) {
                showError(this, '올바른 이메일 형식을 입력해주세요.');
            } else {
                clearError(this); 
                showSuccess(this);
            }
            checkSignupButtonState(); // ✅ 추가
        });
        emailInput.addEventListener('input', function(){ 
            clearError(this);
            if (isEmailVerified) {
                isEmailVerified = false;
                verifyEmailBtn.classList.remove('verified');
                verifyEmailBtn.innerHTML = '인증메일 발송';
                verifyEmailBtn.disabled = false;
            }
            checkSignupButtonState(); // ✅ 추가
        });
    }

    if (nicknameInput) {
        nicknameInput.addEventListener('blur', function(){
            const v = this.value.trim();
            if (!validateNickname(v)) {
                showError(this, '닉네임은 2~20자, 한글/영문/숫자/_(언더스코어)만 가능합니다.');
            } else {
                clearError(this); 
                showSuccess(this);
            }
            checkSignupButtonState(); // ✅ 추가
        });
        nicknameInput.addEventListener('input', function(){
            clearError(this); 
            checkSignupButtonState(); // ✅ 추가
        });
        nicknameInput.addEventListener('focus', showEmailVerificationError);
    }

    if (passwordInput) {
        passwordInput.addEventListener('blur', function(){
            if (!validatePassword(this.value)) {
                showError(this, '비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.');
            } else {
                clearError(this); 
                showSuccess(this);
            }
            checkSignupButtonState(); // ✅ 추가
        });
        passwordInput.addEventListener('input', function(){ 
            clearError(this);
            if (confirmPasswordInput.value) validateConfirmPassword(false); 
            else clearError(confirmPasswordInput);
            checkSignupButtonState(); // ✅ 추가
        });
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('blur', function(){ 
            validateConfirmPassword(true);
            checkSignupButtonState(); // ✅ 추가
        });
        confirmPasswordInput.addEventListener('input', function(){ 
            validateConfirmPassword(false);
            checkSignupButtonState(); // ✅ 추가
        });
    }
    
    // ✅ 약관 동의 체크박스 상태 변경 시 버튼 상태 업데이트
    if (agreeInput) {
        agreeInput.addEventListener('change', checkSignupButtonState);
    }

    if (verifyEmailBtn) {
        verifyEmailBtn.addEventListener('click', function() {
            const email = emailInput.value.trim();
            
            if (!validateEmail(email)) {
                showError(emailInput, '올바른 이메일 형식을 입력해주세요.');
                return;
            }

            verifyEmailBtn.disabled = true;
            verifyEmailBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 전송중...';

            fetch('/BackEnd/SendVerificationEmail', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `email=${encodeURIComponent(email)}`
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showSuccess(emailInput);
                    verifyEmailBtn.innerHTML = '재발송';
                    verifyEmailBtn.disabled = false;
                    startPollingVerification(email);
                    showNotification('인증메일이 발송되었습니다.', 'success');
                } else {
                    showError(emailInput, data.message || '이메일 전송에 실패했습니다.');
                    verifyEmailBtn.innerHTML = '인증메일 발송';
                    verifyEmailBtn.disabled = false;
                }
                checkSignupButtonState(); // ✅ 추가
            })
            .catch(error => {
                console.error('Error:', error);
                showError(emailInput, '이메일 전송 중 오류가 발생했습니다.');
                verifyEmailBtn.innerHTML = '인증메일 발송';
                verifyEmailBtn.disabled = false;
                checkSignupButtonState(); // ✅ 추가
            });
        });
    }

    registerEventListeners();
    checkSignupButtonState();
});

if (typeof showNotification === 'undefined') {
    function showNotification(message, type) {
        type = type || 'info';
        
        const notification = document.createElement('div');
        notification.className = 'notification notification-' + type;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        setTimeout(function() {
            notification.classList.add('show');
        }, 100);
        
        setTimeout(function() {
            notification.classList.remove('show');
            setTimeout(function() {
                if (document.body.contains(notification)) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 5000);
    }
    
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
                max-width: 400px; 
                word-wrap: break-word;
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

