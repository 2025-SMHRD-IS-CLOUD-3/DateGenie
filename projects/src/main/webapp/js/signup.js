document.addEventListener('DOMContentLoaded', function() {
  // Check for email verification URL parameters first
  handleEmailVerificationParams();
  
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
      let isValid = true;

      if (!validateEmail(emailInput.value.trim())) { showError(emailInput,'올바른 이메일을 입력해주세요.'); isValid=false; }
      if (!validateNickname(nicknameInput.value.trim())) { showError(nicknameInput,'닉네임은 2~20자, 한글/영문/숫자/_(언더스코어)만 가능합니다.'); isValid=false; }
      if (!validatePassword(passwordInput.value)) { showError(passwordInput,'비밀번호는 8~64자, 대/소문자·숫자·특수문자 중 3종 이상 포함해야 합니다.'); isValid=false; }
      if (confirmPasswordInput.value !== passwordInput.value) { showError(confirmPasswordInput,'비밀번호가 일치하지 않습니다.'); isValid=false; }
      if (!agreeInput.checked) { showNotification('약관에 동의해주세요.', 'error'); isValid=false; }

      if (!isValid) return;

      const submitBtn = form.querySelector('.signup-btn');
      const original = submitBtn.innerHTML;
      submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 가입 중...';
      submitBtn.disabled = true;

      // 실제 백엔드 API 호출
      const formData = new URLSearchParams();
      formData.append('email', emailInput.value.trim());
      formData.append('pw', passwordInput.value);
      formData.append('nickname', nicknameInput.value.trim());
      
      console.log('Signup attempt:', {
        email: emailInput.value.trim(),
        nickname: nicknameInput.value.trim()
      });

      // API 엔드포인트 결정 (fallback 포함)
      const apiEndpoint = window.APP_CONFIG?.apiEndpoint || 'http://localhost:8081/DateGenie';
      const joinUrl = `${apiEndpoint}/JoinService`;
      
      console.log('=== DEBUG INFO [' + new Date().toISOString() + '] ===');
      console.log('window.APP_CONFIG:', window.APP_CONFIG);
      console.log('window.APP_CONFIG?.apiEndpoint:', window.APP_CONFIG?.apiEndpoint);
      console.log('API Endpoint:', apiEndpoint);
      console.log('Join URL:', joinUrl);
      console.log('Cache Bust: SIGNUP_JS_VERSION_2024_08_22_FIXED');
      console.log('==================');
      
      fetch(joinUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: formData
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('네트워크 응답이 올바르지 않습니다.');
        }
        return response.json();
      })
      .then(data => {
        // Reset button
        submitBtn.innerHTML = original;
        submitBtn.disabled = false;
        
        console.log('Server JSON response:', data);
        
        if (data.success) {
          // 회원가입 성공
          if (data.emailSent === true) {
            // 이메일 인증 메일 발송 성공
            showNotification(data.message, 'success');
          } else if (data.emailSent === false) {
            // 이메일 발송 실패했지만 회원가입은 성공
            showNotification(data.message, 'warning');
          } else {
            // 기본 성공 메시지
            showNotification(data.message, 'success');
          }
          
          // 이메일 인증 섹션 표시 (리다이렉트 대신)
          showEmailVerificationSection(emailInput.value.trim());
        } else {
          // 회원가입 실패
          showNotification(data.message, 'error');
        }
      })
      .catch(error => {
        // Reset button
        submitBtn.innerHTML = original;
        submitBtn.disabled = false;
        
        console.error('회원가입 요청 오류:', error);
        showNotification('회원가입 처리 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
      });
    });
  }

  // 이메일 인증 섹션 표시 함수
  function showEmailVerificationSection(email) {
    const signupForm = document.getElementById('signupForm');
    const verificationSection = document.getElementById('emailVerificationSection');
    const verificationEmailSpan = document.getElementById('verificationEmail');
    
    // 회원가입 폼 숨기기
    if (signupForm) {
      signupForm.style.display = 'none';
    }
    
    // 이메일 인증 섹션 표시
    if (verificationSection) {
      verificationSection.style.display = 'block';
    }
    
    // 이메일 주소 표시
    if (verificationEmailSpan) {
      verificationEmailSpan.textContent = email;
    }
    
    // 저장된 이메일 주소
    window.currentVerificationEmail = email;
    
    // 이메일 인증 관련 이벤트 리스너 초기화
    initEmailVerificationHandlers();
  }

  // 이메일 인증 관련 이벤트 핸들러 초기화
  function initEmailVerificationHandlers() {
    const verificationForm = document.getElementById('verificationForm');
    const resendBtn = document.getElementById('resendBtn');
    const backToSignupBtn = document.getElementById('backToSignupBtn');
    const verificationCodeInput = document.getElementById('verificationCode');

    // 인증 코드 검증 폼 제출
    if (verificationForm) {
      verificationForm.addEventListener('submit', function(e) {
        e.preventDefault();
        handleEmailVerification();
      });
    }

    // 인증 코드 재발송
    if (resendBtn) {
      resendBtn.addEventListener('click', function() {
        handleResendEmail();
      });
    }

    // 회원가입으로 돌아가기
    if (backToSignupBtn) {
      backToSignupBtn.addEventListener('click', function() {
        backToSignup();
      });
    }

    // 인증 코드 입력 필드 자동 포맷팅 (6자리 숫자만)
    if (verificationCodeInput) {
      verificationCodeInput.addEventListener('input', function() {
        this.value = this.value.replace(/[^0-9]/g, '').slice(0, 6);
      });

      verificationCodeInput.addEventListener('keypress', function(e) {
        if (!/[0-9]/.test(e.key) && !['Backspace', 'Delete', 'Tab', 'Enter'].includes(e.key)) {
          e.preventDefault();
        }
      });
    }
  }

  // 이메일 인증 처리
  function handleEmailVerification() {
    const verificationCodeInput = document.getElementById('verificationCode');
    const verifyBtn = document.querySelector('.verify-btn');
    
    if (!verificationCodeInput || !window.currentVerificationEmail) {
      showNotification('인증 정보가 올바르지 않습니다.', 'error');
      return;
    }

    const verificationCode = verificationCodeInput.value.trim();
    
    if (!verificationCode || verificationCode.length !== 6) {
      showError(verificationCodeInput, '6자리 인증 코드를 입력해주세요.');
      return;
    }

    // 버튼 로딩 상태
    const originalText = verifyBtn.innerHTML;
    verifyBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 인증 중...';
    verifyBtn.disabled = true;

    // API 엔드포인트 결정
    const apiEndpoint = window.APP_CONFIG?.apiEndpoint || 'http://localhost:8081/DateGenie';
    const verifyUrl = `${apiEndpoint}/VerifyEmailService`;

    // 인증 요청
    const verificationData = new URLSearchParams();
    verificationData.append('email', window.currentVerificationEmail);
    verificationData.append('verificationCode', verificationCode);

    fetch(verifyUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: verificationData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('네트워크 응답이 올바르지 않습니다.');
      }
      return response.json();
    })
    .then(data => {
      // 버튼 복원
      verifyBtn.innerHTML = originalText;
      verifyBtn.disabled = false;
      
      if (data.success) {
        // 인증 성공
        showNotification('이메일 인증이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');
        
        // 로그인 페이지로 리다이렉트
        setTimeout(() => {
          window.location.href = '/DateGenie/login.html';
        }, 2000);
      } else {
        // 인증 실패
        showNotification(data.message || '인증에 실패했습니다. 다시 시도해주세요.', 'error');
        showError(verificationCodeInput, data.message || '잘못된 인증 코드입니다.');
      }
    })
    .catch(error => {
      // 버튼 복원
      verifyBtn.innerHTML = originalText;
      verifyBtn.disabled = false;
      
      console.error('이메일 인증 요청 오류:', error);
      showNotification('인증 처리 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
    });
  }

  // 인증 이메일 재발송
  function handleResendEmail() {
    const resendBtn = document.getElementById('resendBtn');
    const resendText = document.getElementById('resendText');
    
    if (!window.currentVerificationEmail) {
      showNotification('이메일 정보가 올바르지 않습니다.', 'error');
      return;
    }

    // 버튼 비활성화 및 로딩 상태
    resendBtn.disabled = true;
    resendText.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 발송 중...';

    // API 엔드포인트 결정
    const apiEndpoint = window.APP_CONFIG?.apiEndpoint || 'http://localhost:8081/DateGenie';
    const resendUrl = `${apiEndpoint}/ResendVerificationService`;

    // 재발송 요청
    const resendData = new URLSearchParams();
    resendData.append('email', window.currentVerificationEmail);

    fetch(resendUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      },
      body: resendData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('네트워크 응답이 올바르지 않습니다.');
      }
      return response.json();
    })
    .then(data => {
      if (data.success) {
        // 재발송 성공
        showNotification('인증 이메일을 다시 발송했습니다.', 'success');
        startResendCooldown();
      } else {
        // 재발송 실패
        showNotification(data.message || '이메일 재발송에 실패했습니다.', 'error');
        // 버튼 복원
        resendBtn.disabled = false;
        resendText.innerHTML = '인증 코드 재발송';
      }
    })
    .catch(error => {
      console.error('이메일 재발송 요청 오류:', error);
      showNotification('이메일 재발송 중 오류가 발생했습니다.', 'error');
      // 버튼 복원
      resendBtn.disabled = false;
      resendText.innerHTML = '인증 코드 재발송';
    });
  }

  // 재발송 쿨다운 타이머
  function startResendCooldown() {
    const resendBtn = document.getElementById('resendBtn');
    const resendText = document.getElementById('resendText');
    let countdown = 60; // 60초 쿨다운

    const timer = setInterval(() => {
      resendText.textContent = `재발송 가능 (${countdown}초)`;
      countdown--;

      if (countdown < 0) {
        clearInterval(timer);
        resendBtn.disabled = false;
        resendText.innerHTML = '<i class="fas fa-redo"></i> 인증 코드 재발송';
      }
    }, 1000);
  }

  // 회원가입으로 돌아가기
  function backToSignup() {
    const signupForm = document.getElementById('signupForm');
    const verificationSection = document.getElementById('emailVerificationSection');
    const verificationCodeInput = document.getElementById('verificationCode');
    
    // 인증 섹션 숨기기
    if (verificationSection) {
      verificationSection.style.display = 'none';
    }
    
    // 회원가입 폼 표시
    if (signupForm) {
      signupForm.style.display = 'block';
    }
    
    // 인증 코드 입력 필드 초기화
    if (verificationCodeInput) {
      verificationCodeInput.value = '';
      clearError(verificationCodeInput);
    }
    
    // 저장된 이메일 정보 초기화
    window.currentVerificationEmail = null;
    
    showNotification('회원가입 화면으로 돌아왔습니다.', 'info');
  }

  // Handle email verification URL parameters (when user clicks email link)
  function handleEmailVerificationParams() {
    const urlParams = new URLSearchParams(window.location.search);
    const verificationSuccess = urlParams.get('verification_success');
    const verificationError = urlParams.get('verification_error');
    const email = urlParams.get('email');
    const message = urlParams.get('message');

    if (verificationSuccess === 'true') {
      // Email verification successful via link
      if (email) {
        showNotification(`이메일 인증이 완료되었습니다! (${decodeURIComponent(email)}) 로그인 페이지로 이동합니다.`, 'success');
      } else {
        showNotification('이메일 인증이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');
      }
      
      // Redirect to login page after success
      setTimeout(() => {
        window.location.href = '/DateGenie/login.html?verified=true';
      }, 2000);
      
      // Clean URL
      window.history.replaceState({}, document.title, window.location.pathname);
      
    } else if (verificationError) {
      // Email verification failed via link
      let errorMessage = '이메일 인증 중 오류가 발생했습니다.';
      
      switch (verificationError) {
        case 'missing_token':
          errorMessage = '유효하지 않은 인증 링크입니다.';
          break;
        case 'verification_failed':
          errorMessage = message ? decodeURIComponent(message) : '인증 토큰이 유효하지 않거나 만료되었습니다.';
          break;
        case 'server_error':
          errorMessage = '서버 오류가 발생했습니다. 나중에 다시 시도해주세요.';
          break;
      }
      
      showNotification(errorMessage, 'error');
      
      // If there's an email, show the verification section for retry
      if (email) {
        const decodedEmail = decodeURIComponent(email);
        showEmailVerificationSection(decodedEmail);
        showNotification('아래에서 인증 코드를 다시 요청하거나 입력하실 수 있습니다.', 'info');
      }
      
      // Clean URL
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }
});

