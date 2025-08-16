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
            '        <h4>기술 스택</h4>',
            '        <div class="tech-grid">',
            '          <div class="tech-group">',
            '            <span class="tech-category">Frontend</span>',
            '            <div class="tech-items">',
            '              <span class="tech-item frontend">HTML5</span>',
            '              <span class="tech-item frontend">CSS3</span>',
            '              <span class="tech-item frontend">JavaScript</span>',
            '              <span class="tech-item frontend">Chart.js</span>',
            '            </div>',
            '          </div>',
            '          <div class="tech-group">',
            '            <span class="tech-category">Backend</span>',
            '            <div class="tech-items">',
            '              <span class="tech-item backend">JSP/Servlet</span>',
            '              <span class="tech-item backend">Oracle DB</span>',
            '              <span class="tech-item backend">AI/ML</span>',
            '            </div>',
            '          </div>',
            '        </div>',
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