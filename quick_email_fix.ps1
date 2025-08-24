# DateGenie 이메일 문제 빠른 진단 및 수정 스크립트
# PowerShell 스크립트로 실행하세요

Write-Host "🔧 DateGenie 이메일 인증 문제 진단 중..." -ForegroundColor Green
Write-Host ""

# 1. 네트워크 연결 테스트
Write-Host "1️⃣ Gmail SMTP 서버 연결 테스트..." -ForegroundColor Yellow
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $tcpClient.Connect("smtp.gmail.com", 587)
    if ($tcpClient.Connected) {
        Write-Host "✅ Gmail SMTP 연결 성공" -ForegroundColor Green
    }
    $tcpClient.Close()
} catch {
    Write-Host "❌ Gmail SMTP 연결 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   해결방법: 방화벽 또는 네트워크 정책 확인" -ForegroundColor Cyan
}

Write-Host ""

# 2. 설정 파일 확인
Write-Host "2️⃣ 이메일 설정 파일 확인..." -ForegroundColor Yellow
$configPath = "projects\src\main\resources\config\email.properties"
if (Test-Path $configPath) {
    Write-Host "✅ email.properties 파일 존재" -ForegroundColor Green
    
    # 설정 내용 읽기
    $config = Get-Content $configPath
    $hasUsername = $config | Select-String "email.username=" | Where-Object { $_ -notmatch "^#" }
    $hasPassword = $config | Select-String "email.password=" | Where-Object { $_ -notmatch "^#" }
    
    if ($hasUsername -and $hasPassword) {
        Write-Host "✅ 사용자명과 패스워드 설정 확인" -ForegroundColor Green
    } else {
        Write-Host "❌ 사용자명 또는 패스워드 미설정" -ForegroundColor Red
    }
} else {
    Write-Host "❌ email.properties 파일 없음" -ForegroundColor Red
}

Write-Host ""

# 3. 환경변수 확인
Write-Host "3️⃣ 환경변수 확인..." -ForegroundColor Yellow
$envUsername = [Environment]::GetEnvironmentVariable("EMAIL_USERNAME")
$envPassword = [Environment]::GetEnvironmentVariable("EMAIL_PASSWORD")

if ($envUsername) {
    Write-Host "✅ EMAIL_USERNAME 환경변수 설정됨: $envUsername" -ForegroundColor Green
} else {
    Write-Host "ℹ️ EMAIL_USERNAME 환경변수 없음 (선택적)" -ForegroundColor Gray
}

if ($envPassword) {
    Write-Host "✅ EMAIL_PASSWORD 환경변수 설정됨" -ForegroundColor Green
} else {
    Write-Host "ℹ️ EMAIL_PASSWORD 환경변수 없음 (선택적)" -ForegroundColor Gray
}

Write-Host ""

# 4. 진단 페이지 링크 제공
Write-Host "4️⃣ 다음 단계 안내" -ForegroundColor Yellow
Write-Host "   🌐 진단 페이지 접속: http://localhost:8081/DateGenie/email-debug.jsp" -ForegroundColor Cyan
Write-Host "   📄 상세 가이드: email_troubleshoot_guide.md" -ForegroundColor Cyan

Write-Host ""
Write-Host "💡 가장 일반적인 해결방법:" -ForegroundColor Green
Write-Host "   1. Google 계정 → 보안 → 2단계 인증 → 앱 패스워드" -ForegroundColor White
Write-Host "   2. 새 앱 패스워드 생성 후 email.properties 업데이트" -ForegroundColor White
Write-Host "   3. 서버 재시작" -ForegroundColor White

Write-Host ""
Write-Host "진단 완료! 🎉" -ForegroundColor Green