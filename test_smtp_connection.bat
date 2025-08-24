@echo off
echo SMTP 연결 테스트 중...
echo.

REM Gmail SMTP 서버 연결 테스트
echo Gmail SMTP (smtp.gmail.com:587) 연결 테스트:
telnet smtp.gmail.com 587

echo.
echo 연결이 성공하면 220 응답이 표시되어야 합니다.
echo 연결 실패 시 방화벽이나 네트워크 정책을 확인하세요.
pause