document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('signupForm');
  const emailInput = document.getElementById('email');
  const nicknameInput = document.getElementById('nickname');
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirmPassword');
  const agreeInput = document.getElementById('agree');
  const toggleButtons = document.querySelectorAll('.toggle-password');

  // Toggle password visibility for both fields
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

  // Real-time validation
  if (emailInput) {
    emailInput.addEventListener('blur', function() {
      const v = this.value.trim();
      if (!v) return showError(this, '이메일을 입력해주세요.');
      if (!validateEmail(v)) return showError(this, '올바른 이메일 형식을 입력해주세요.');
      clearError(this); showSuccess(this);
    });
    emailInput.addEventListener('input', function(){ clearError(this); });
  }

  if (nicknameInput) {
    nicknameInput.addEventListener('blur', function(){
      const v = this.value.trim();
      if (!validateNickname(v)) return showError(this, '닉네임은 2~20자, 한글/영문/숫자/_(언더스코어)만 가능합니다.');
      clearError(this); showSuccess(this);
    });
    nicknameInput.addEventListener('input', function(){ clearError(this); });
  }

  if (passwordInput) {
    passwordInput.addEventListener('blur', function(){
      if (!validatePassword(this.value)) return showError(this, '비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.');
      clearError(this); showSuccess(this);
    });
    passwordInput.addEventListener('input', function(){ clearError(this); });
  }

  function validateConfirmPassword(onBlur = false){
    if (!confirmPasswordInput) return;
    const pw = passwordInput ? passwordInput.value : '';
    const cf = confirmPasswordInput.value;
    if (!cf) {
      // 입력 중에는 중립, blur에서만 에러로 안내
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
    clearError(confirmPasswordInput); showSuccess(confirmPasswordInput);
    return true;
  }

  if (confirmPasswordInput) {
    confirmPasswordInput.addEventListener('blur', function(){ validateConfirmPassword(true); });
    confirmPasswordInput.addEventListener('input', function(){ validateConfirmPassword(false); });
  }

  if (passwordInput && confirmPasswordInput) {
    // 비밀번호 변경 시 확인값 상태도 재검증
    passwordInput.addEventListener('input', function(){
      if (confirmPasswordInput.value) validateConfirmPassword(false); else clearError(confirmPasswordInput);
    });
  }

  // 폼 제출 처리 (일반 폼 제출)
  if (form) {
    form.addEventListener('submit', function(e){
      e.preventDefault();
      
      console.log('=== 회원가입 폼 제출 시작 ===');
      
      let isValid = true;

      // 폼 유효성 검사
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

      if (!isValid) {
        console.log('폼 유효성 검사 실패');
        return;
      }

      // 로딩 상태 표시
      const submitBtn = form.querySelector('.signup-btn');
      const originalText = submitBtn.innerHTML;
      submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 가입 중...';
      submitBtn.disabled = true;

      console.log('서버로 폼 제출 시작');
      console.log('전송 데이터:', {
        email: email,
        nickname: nickname,
        password: password ? '****' : null
      });

      // 폼 action과 method 설정
      form.action = '/BackEnd/JoinService';
      form.method = 'POST';
	  
	  showNotification('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');
      
      // 잠시 후 폼 제출 (로딩 애니메이션을 보여주기 위해)
      setTimeout(function() {
        form.submit(); // 일반 폼 제출
      }, 500);
    });
  }
});

// Notification function
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
  
  // Notification styles
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