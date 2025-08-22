<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.smhrd.util.EmailConfig" %>
<%@ page import="com.smhrd.service.EmailService" %>
<%@ page import="com.smhrd.service.EmailVerificationService" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>이메일 시스템 진단</title>
    <style>
        body { font-family: 'Malgun Gothic', Arial, sans-serif; margin: 20px; line-height: 1.6; }
        .section { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }
        .success { color: #28a745; font-weight: bold; }
        .error { color: #dc3545; font-weight: bold; }
        .warning { color: #ffc107; font-weight: bold; }
        .info { color: #17a2b8; font-weight: bold; }
        pre { background: #333; color: #fff; padding: 10px; border-radius: 5px; overflow-x: auto; }
        .test-form { background: #e3f2fd; padding: 20px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <h1>🔧 DateGenie 이메일 시스템 진단</h1>
    
    <div class="section">
        <h2>1. 이메일 설정 상태</h2>
        <% try { %>
            <p><strong>SMTP Host:</strong> <%= EmailConfig.getSmtpHost() %></p>
            <p><strong>SMTP Port:</strong> <%= EmailConfig.getSmtpPort() %></p>
            <p><strong>SMTP Auth:</strong> <%= EmailConfig.isSmtpAuthEnabled() ? "활성화됨" : "비활성화됨" %></p>
            <p><strong>STARTTLS:</strong> <%= EmailConfig.isStartTlsEnabled() ? "활성화됨" : "비활성화됨" %></p>
            <p><strong>From Address:</strong> <%= EmailConfig.getFromAddress() %></p>
            <p><strong>From Name:</strong> <%= EmailConfig.getFromName() %></p>
            
            <%
                String username = EmailConfig.getUsername();
                String password = EmailConfig.getPassword();
            %>
            
            <p><strong>Username:</strong> 
                <% if (username != null && !username.isEmpty()) { %>
                    <span class="success">✅ 설정됨 (<%= username %>)</span>
                <% } else { %>
                    <span class="error">❌ 설정되지 않음</span>
                <% } %>
            </p>
            
            <p><strong>Password:</strong> 
                <% if (password != null && !password.isEmpty()) { %>
                    <span class="success">✅ 설정됨 (길이: <%= password.length() %>)</span>
                <% } else { %>
                    <span class="error">❌ 설정되지 않음</span>
                <% } %>
            </p>
            
            <p><strong>Debug Mode:</strong> <%= EmailConfig.isDebugMode() ? "활성화됨" : "비활성화됨" %></p>
            
        <% } catch (Exception e) { %>
            <p class="error">❌ 설정 로드 실패: <%= e.getMessage() %></p>
        <% } %>
    </div>
    
    <div class="section">
        <h2>2. 이메일 서비스 설정 테스트</h2>
        <% try { %>
            <% boolean configTest = EmailService.testEmailConfiguration(); %>
            <% if (configTest) { %>
                <p class="success">✅ 이메일 서비스 설정 정상</p>
            <% } else { %>
                <p class="error">❌ 이메일 서비스 설정 오류</p>
                <div class="warning">
                    <strong>⚠️ 가능한 원인:</strong>
                    <ul>
                        <li>SMTP 인증 정보가 올바르지 않음</li>
                        <li>Gmail 앱 패스워드가 만료되었거나 잘못됨</li>
                        <li>네트워크 연결 문제 (방화벽, 프록시)</li>
                        <li>Google 계정의 2단계 인증이 비활성화됨</li>
                    </ul>
                </div>
            <% } %>
        <% } catch (Exception e) { %>
            <p class="error">❌ 이메일 서비스 테스트 실패: <%= e.getMessage() %></p>
        <% } %>
    </div>
    
    <div class="section">
        <h2>3. 이메일 인증 서비스 상태</h2>
        <% try { %>
            <% EmailVerificationService service = new EmailVerificationService(); %>
            <% String status = service.getSystemStatus(); %>
            <pre><%= status %></pre>
        <% } catch (Exception e) { %>
            <p class="error">❌ 이메일 인증 서비스 상태 확인 실패: <%= e.getMessage() %></p>
            <pre class="error"><%= e.toString() %></pre>
        <% } %>
    </div>
    
    <div class="test-form">
        <h2>4. 실제 이메일 발송 테스트</h2>
        <p><strong>⚠️ 주의:</strong> 아래 테스트는 실제 이메일을 발송합니다.</p>
        
        <form method="post" action="email-debug.jsp">
            <label for="testEmail">테스트 이메일 주소:</label><br>
            <input type="email" id="testEmail" name="testEmail" placeholder="your-email@example.com" required style="width: 300px; padding: 5px;"><br><br>
            <button type="submit" name="action" value="testSend" style="background: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">📧 테스트 이메일 발송</button>
        </form>
        
        <% 
            String action = request.getParameter("action");
            String testEmail = request.getParameter("testEmail");
            
            if ("testSend".equals(action) && testEmail != null && !testEmail.isEmpty()) {
        %>
            <hr>
            <h3>📧 테스트 이메일 발송 결과:</h3>
            <% try { %>
                <% boolean testResult = EmailService.sendTestEmail(testEmail, "DateGenie 이메일 테스트", "이메일 시스템이 정상적으로 작동합니다!"); %>
                <% if (testResult) { %>
                    <p class="success">✅ 테스트 이메일 발송 성공!</p>
                    <p>이메일 계정(<%= testEmail %>)을 확인해주세요.</p>
                <% } else { %>
                    <p class="error">❌ 테스트 이메일 발송 실패</p>
                    <p>서버 콘솔 로그를 확인해주세요.</p>
                <% } %>
            <% } catch (Exception e) { %>
                <p class="error">❌ 테스트 이메일 발송 중 예외 발생: <%= e.getMessage() %></p>
                <pre class="error"><%= e.toString() %></pre>
            <% } %>
        <% } %>
    </div>
    
    <div class="section">
        <h2>5. 문제 해결 가이드</h2>
        <div class="info">
            <h3>🔧 일반적인 해결 방법:</h3>
            <ol>
                <li><strong>Gmail 앱 패스워드 확인:</strong>
                    <ul>
                        <li>Google 계정 → 보안 → 2단계 인증 → 앱 패스워드</li>
                        <li>새 앱 패스워드 생성 후 email.properties 업데이트</li>
                    </ul>
                </li>
                <li><strong>방화벽/네트워크 설정:</strong>
                    <ul>
                        <li>smtp.gmail.com:587 포트 허용</li>
                        <li>회사/학교 네트워크에서 SMTP 차단 여부 확인</li>
                    </ul>
                </li>
                <li><strong>Google 계정 설정:</strong>
                    <ul>
                        <li>2단계 인증이 활성화되어 있는지 확인</li>
                        <li>"보안 수준이 낮은 앱 액세스" 대신 앱 패스워드 사용</li>
                    </ul>
                </li>
                <li><strong>서버 재시작:</strong>
                    <ul>
                        <li>설정 변경 후 Tomcat 서버 재시작</li>
                        <li>클래스 파일 및 설정 파일 재로드</li>
                    </ul>
                </li>
            </ol>
        </div>
    </div>
    
    <div class="section">
        <p><small>🕐 생성 시각: <%= new java.util.Date() %></small></p>
        <p><small>📁 설정 파일: /src/main/resources/config/email.properties</small></p>
    </div>
</body>
</html>