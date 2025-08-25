document.addEventListener('DOMContentLoaded', () => {

    const profileForm = document.getElementById('profileForm');
    const nameInput = document.getElementById('name');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');

    function loadUserInfo() {
        const userInfo = localStorage.getItem('user'); 
        if (userInfo) {
            const user = JSON.parse(userInfo);
            const emailInput = document.getElementById('email');
            if (emailInput) {
                emailInput.value = user.email || '';
            }
            if (nameInput) {
                nameInput.value = user.nickname || '';
            }
        }
    }
    
    function validateNickname(nickname) {
        return /^[A-Za-z0-9_가-힣]{2,20}$/.test((nickname || '').trim());
    }

    function validatePassword(password) {
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
        const wrapper = input.closest('.form-group');
        const existing = wrapper.querySelector('.error-message');
        if (existing) existing.remove();
        
        const msg = document.createElement('div');
        msg.className = 'error-message';
        msg.style.cssText = 'color: #ef4444; font-size: 0.875rem; margin-top: 0.25rem;';
        msg.textContent = message;
        wrapper.appendChild(msg);
        input.style.borderColor = '#ef4444';
    }

    function clearError(input) {
        const wrapper = input.closest('.form-group');
        const existing = wrapper.querySelector('.error-message');
        if (existing) existing.remove();
        input.style.borderColor = '#d1d5db';
    }

    function showSuccess(input) {
        clearError(input);
        input.style.borderColor = '#10b981';
    }

    function validateConfirmPassword() {
        if (passwordInput.value !== confirmPasswordInput.value) {
            showError(confirmPasswordInput, '비밀번호가 일치하지 않습니다.');
            return false;
        }
        showSuccess(confirmPasswordInput);
        return true;
    }
    
    nameInput.addEventListener('blur', () => {
        const nickname = nameInput.value.trim();
        if (nickname && !validateNickname(nickname)) {
            showError(nameInput, '닉네임은 2~20자, 한글/영문/숫자/_만 가능합니다.');
        } else {
            clearError(nameInput);
        }
    });

    passwordInput.addEventListener('blur', () => {
        const password = passwordInput.value;
        if (password && !validatePassword(password)) {
            showError(passwordInput, '비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.');
        } else {
            clearError(passwordInput);
        }
        if (confirmPasswordInput.value) {
            validateConfirmPassword();
        }
    });

    confirmPasswordInput.addEventListener('blur', validateConfirmPassword);
    
    profileForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const nickname = nameInput.value.trim();
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // 비밀번호 필드가 비어있지 않은 경우에만 유효성 검사 수행
        if (password) {
            if (!validatePassword(password)) {
                alert('유효한 비밀번호를 입력해주세요.');
                return;
            }
            if (password !== confirmPassword) {
                alert('비밀번호가 일치하지 않습니다.');
                return;
            }
        }
        
        // 닉네임 필드가 비어있지 않은 경우에만 유효성 검사 수행
        if (nickname && !validateNickname(nickname)) {
            alert('유효한 닉네임을 입력해주세요.');
            return;
        }
        
        // 비밀번호와 닉네임 모두 비어있을 경우, 업데이트 요청을 보내지 않습니다.
        if (!password && !nickname) {
            alert('수정할 정보를 입력해주세요.');
            return;
        }

        const updateData = {
            email: document.getElementById('email').value
        };

        if (password) {
            updateData.pw = password;
        }

        if (nickname) {
            updateData.nickname = nickname;
        }

        try {
            const response = await fetch('/BackEnd/UpdateService', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updateData)
            });
            
            const data = await response.json();

            if (data.success) {
                alert(data.message);
                
                // 로컬 스토리지 데이터 업데이트
                const user = JSON.parse(localStorage.getItem('user'));
                if (password) {
                    user.pw = password;
                }
                if (nickname) {
                    user.nickname = nickname;
                }
                localStorage.setItem('user', JSON.stringify(user));
                
                window.location.href = 'dashboard.html';
            } else {
                alert(data.message);
            }
        } catch (error) {
            console.error('Error:', error);
            alert('네트워크 오류가 발생했습니다. 서버가 올바른 응답을 보내지 않습니다.');
        }
    });

    loadUserInfo();
});
