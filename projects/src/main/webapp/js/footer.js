/**
 * Footer 관련 JavaScript 기능
 * Date Genie - AI 기반 연애 분석 서비스
 */

"use strict";

(function () {
    // Footer 생성 및 초기화
    function injectFooter() {
        // 이미 footer가 있다면 기존 footer에 기능만 추가
        if (document.querySelector('.site-footer')) {
            initializeFooterFeatures();
            return;
        }

        // footer가 없다면 새로 생성 (대부분의 페이지는 HTML에 직접 포함)
        var footer = document.createElement('footer');
        footer.className = 'site-footer';
        footer.innerHTML = createFooterHTML();

        document.body.appendChild(footer);
        initializeFooterFeatures();
    }

    // Footer HTML 생성 (HTML에 없는 경우를 위한 백업)
    function createFooterHTML() {
        return [
            '<div class="container">',
            '  <div class="footer-content">',
            '    <div class="footer-brand">',
            '      <div class="footer-logo">',
            '        <i class="fas fa-heart"></i>',
            '        <span>Date Genie</span>',
            '      </div>',
            '      <p class="footer-description">AI로 분석하는 연애 전략</p>',
            '      <div class="social-links">',
            '        <a href="https://github.com/2025-SMHRD-IS-CLOUD-3/DateGenie" target="_blank" rel="noopener noreferrer" aria-label="GitHub">',
            '          <i class="fab fa-github"></i>',
            '        </a>',
            '        <a href="https://www.youtube.com/@%EC%A0%95%EA%B4%80%EC%98%81-v2i" target="_blank" rel="noopener noreferrer" aria-label="YouTube">',
            '          <i class="fab fa-youtube"></i>',
            '        </a>',
            '        <a href="https://www.instagram.com/kwanhh25/" target="_blank" rel="noopener noreferrer" aria-label="Instagram">',
            '          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">',
            '            <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>',
            '          </svg>',
            '        </a>',
            '      </div>',
            '    </div>',
            '    <div class="footer-links">',
            '      <div class="footer-section">',
            '        <h4>페이지</h4>',
            '        <ul>',
            '          <li><a href="index.html">홈</a></li>',
            '          <li><a href="#features">기능</a></li>',
            '          <li><a href="#pricing">요금제</a></li>',
            '          <li><a href="#faq">FAQ</a></li>',
            '        </ul>',
            '      </div>',
            '      <div class="footer-section">',
            '        <h4>기능</h4>',
            '        <ul>',
            '          <li><span class="menu-item">마이페이지</span></li>',
            '          <li><span class="menu-item">파일 업로드</span></li>',
            '          <li><span class="menu-item">회원정보 수정</span></li>',
            '          <li><span class="menu-item">설정</span></li>',
            '        </ul>',
            '      </div>',
            '      <div class="footer-section">',
            '        <h4>지원 & 도움말</h4>',
            '        <ul>',
            '          <li><a href="#faq">자주하는 질문</a></li>',
            '          <li><a href="#support">고객센터</a></li>',
            '          <li><a href="#guide">사용 가이드</a></li>',
            '          <li><a href="#contact">문의하기</a></li>',
            '        </ul>',
            '      </div>',
            '    </div>',
            '  </div>',
            '  <div class="footer-bottom">',
            '    <div class="footer-legal">',
            '      <a href="terms.html">이용약관</a>',
            '      <span aria-hidden="true">·</span>',
            '      <a href="privacy.html">개인정보처리방침</a>',
            '      <span aria-hidden="true">·</span>',
            '      <span>© <span id="footerYear"></span> Date Genie. All rights reserved.</span>',
            '    </div>',
            '  </div>',
            '</div>'
        ].join('');
    }

    // Footer 기능 초기화
    function initializeFooterFeatures() {
        updateCopyrightYear();
        addFooterLinkEffects();
    }

    // 저작권 연도 업데이트
    function updateCopyrightYear() {
        var yearElement = document.getElementById('footerYear');
        if (yearElement) {
            yearElement.textContent = new Date().getFullYear();
        }
    }


    // Footer 링크 효과 추가
    function addFooterLinkEffects() {
        // 더 이상 transform 효과를 적용하지 않음
        // CSS에서만 color 변경을 처리
    }

    // Footer 유틸리티 함수들
    window.FooterUtils = {
        // Footer 가시성 확인
        isFooterVisible: function() {
            var footer = document.querySelector('.site-footer');
            if (!footer) return false;
            
            var rect = footer.getBoundingClientRect();
            return rect.top < window.innerHeight && rect.bottom > 0;
        },
        
        // Footer로 스크롤
        scrollToFooter: function() {
            var footer = document.querySelector('.site-footer');
            if (footer) {
                footer.scrollIntoView({ 
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        },
        
        // Footer 통계 업데이트 (향후 API 연동용)
        updateFooterStats: function(stats) {
            console.log('Footer 통계 업데이트:', stats);
            // 향후 실시간 통계 업데이트를 위한 준비
        }
    };

    // DOM이 로드되면 Footer 초기화
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectFooter);
    } else {
        // DOM이 이미 로드된 경우 즉시 실행
        injectFooter();
    }
})();