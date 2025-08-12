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

  function validatePassword(password) {
    // 6자 이상, 권장: 문자/숫자 조합
    return password && password.length >= 6;
  }

  function showError(input, message) {
    const wrapper = input.closest('.input-wrapper');
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
      if (v.length < 2) return showError(this, '닉네임은 2자 이상 입력해주세요.');
      clearError(this); showSuccess(this);
    });
    nicknameInput.addEventListener('input', function(){ clearError(this); });
  }

  if (passwordInput) {
    passwordInput.addEventListener('blur', function(){
      if (!validatePassword(this.value)) return showError(this, '비밀번호는 최소 6자 이상이어야 합니다.');
      clearError(this); showSuccess(this);
    });
    passwordInput.addEventListener('input', function(){ clearError(this); });
  }

  if (confirmPasswordInput) {
    confirmPasswordInput.addEventListener('blur', function(){
      if (this.value !== passwordInput.value) return showError(this, '비밀번호가 일치하지 않습니다.');
      clearError(this); showSuccess(this);
    });
    confirmPasswordInput.addEventListener('input', function(){ clearError(this); });
  }

  if (form) {
    form.addEventListener('submit', function(e){
      e.preventDefault();
      let isValid = true;

      if (!validateEmail(emailInput.value.trim())) { showError(emailInput,'올바른 이메일을 입력해주세요.'); isValid=false; }
      if (!nicknameInput.value.trim() || nicknameInput.value.trim().length < 2) { showError(nicknameInput,'닉네임은 2자 이상 입력해주세요.'); isValid=false; }
      if (!validatePassword(passwordInput.value)) { showError(passwordInput,'비밀번호는 최소 6자 이상이어야 합니다.'); isValid=false; }
      if (confirmPasswordInput.value !== passwordInput.value) { showError(confirmPasswordInput,'비밀번호가 일치하지 않습니다.'); isValid=false; }
      if (!agreeInput.checked) { showNotification('약관에 동의해주세요.', 'error'); isValid=false; }

      if (!isValid) return;

      const submitBtn = form.querySelector('.signup-btn');
      const original = submitBtn.innerHTML;
      submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 가입 중...';
      submitBtn.disabled = true;

      setTimeout(() => {
        console.log('Signup attempt:', {
          email: emailInput.value.trim(),
          nickname: nicknameInput.value.trim()
        });
        showNotification('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');
        setTimeout(() => { window.location.href = 'login.html'; }, 1500);
      }, 1500);
    });
  }
});

