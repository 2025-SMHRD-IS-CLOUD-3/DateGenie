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

  if (form) {
    form.addEventListener('submit', function(e){
	  e.preventDefault();
	
	  console.log("2단계: 유효성 검사 시작");	
      
	  let isValid = true;

      if (!validateEmail(emailInput.value.trim())) { showError(emailInput,'올바른 이메일을 입력해주세요.'); isValid=false; }
      if (!validateNickname(nicknameInput.value.trim())) { showError(nicknameInput,'닉네임은 2~20자, 한글/영문/숫자/_(언더스코어)만 가능합니다.'); isValid=false; }
      if (!validatePassword(passwordInput.value)) { showError(passwordInput,'비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.'); isValid=false; }
      if (confirmPasswordInput.value !== passwordInput.value) { showError(confirmPasswordInput,'비밀번호가 일치하지 않습니다.'); isValid=false; }
      if (!agreeInput.checked) { alert('약관에 동의해주세요.'); return;/*isValid=false;*/ }
		
	  /*
      if (!isValid) return;
	  */
	  
	       
	  // preventDefault()로 막았던 기본 제출을 다시 실행
	  form.submit();
    });
  }
});

