package com.smhrd.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smhrd.model.EmailDAO;

@WebServlet("/EmailVerification")
public class EmailVerification extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        System.out.println("=== EmailVerification 서블릿 호출됨 ===");
        
        // 인코딩 설정
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        
        try {
            // 1. URL 파라미터에서 토큰 받기
            String token = request.getParameter("token");
            System.out.println("받은 인증 토큰: " + token);
            
            // 토큰 유효성 검사
            if (token == null || token.trim().isEmpty()) {
                System.out.println("ERROR: 토큰이 비어있음");
                sendFailureResponse(response);
                return;
            }
            
            // 2. EmailDAO로 토큰 유효성 확인
            System.out.println("EmailDAO 객체 생성 시도...");
            EmailDAO emailDAO = new EmailDAO();
            System.out.println("EmailDAO 객체 생성 성공!");
            
            // 2-1. 토큰이 존재하고 만료되지 않았는지 확인
            System.out.println("isTokenValid 메서드 호출 시도...");
            boolean isTokenValid = emailDAO.isTokenValid(token);
            System.out.println("isTokenValid 메서드 호출 완료: " + isTokenValid);
            
            if (!isTokenValid) {
                System.out.println("ERROR: 토큰이 유효하지 않거나 만료됨");
                sendFailureResponse(response);
                return;
            }
            
            // 토큰 유효성 확인 후 이메일 정보 가져오기
            String verifiedEmail = emailDAO.getEmailByToken(token);
            System.out.println("디버깅: 조회된 이메일 주소 -> " + verifiedEmail);
            
            if (verifiedEmail == null) {
                System.out.println("디버깅 ERROR: 토큰에 해당하는 이메일 정보가 데이터베이스에 없습니다.");
                sendFailureResponse(response);
                return;
            }
     
            // 2-2. 이미 인증된 이메일인지 확인
            boolean alreadyVerified = emailDAO.isEmailVerified(verifiedEmail);
            System.out.println("이미 인증 여부: " + alreadyVerified);
            
            if (alreadyVerified) {
                System.out.println("INFO: 이미 인증된 이메일");
                sendAlreadyVerifiedResponse(response, verifiedEmail);
                return;
            }

            // 3. 이메일 인증 완료 처리
            boolean verificationSuccess = emailDAO.verifyEmailToken(token);
            System.out.println("인증 처리 결과: " + verificationSuccess);
            
            if (verificationSuccess) {
                System.out.println("SUCCESS: 이메일 인증 완료");
                sendSuccessResponse(response, verifiedEmail);
            } else {
                System.out.println("ERROR: 인증 처리 실패");
                sendFailureResponse(response);
            }
            
        } catch (Exception e) {
            System.out.println("=== EXCEPTION 발생 ===");
            System.out.println("에러 메시지: " + e.getMessage());
            e.printStackTrace();
            sendFailureResponse(response);
        }
    }
    
    // 성공 응답
    private void sendSuccessResponse(HttpServletResponse response, String verifiedEmail) throws IOException {
    	StringBuilder htmlResponse = new StringBuilder();
    	   htmlResponse.append("<!DOCTYPE html>");
    	   htmlResponse.append("<html>");
    	   htmlResponse.append("<head><meta charset='UTF-8'></head>");
    	   htmlResponse.append("<body>");
    	   htmlResponse.append("<script>");
    	   htmlResponse.append("alert('이메일 인증이 완료되었습니다!');");
    	   htmlResponse.append("try {");
    	   htmlResponse.append("  if (window.opener && !window.opener.closed) {");
    	   htmlResponse.append("    window.opener.postMessage({ type: 'emailVerified', email: '" + verifiedEmail + "' }, '*');");
    	   htmlResponse.append("    window.opener.focus();");
    	   htmlResponse.append("    window.close();");
    	   htmlResponse.append("  } else {");
    	   htmlResponse.append("    window.close();");
    	   htmlResponse.append("  }");
    	   htmlResponse.append("} catch(e) {");
    	   htmlResponse.append("  window.close();");
    	   htmlResponse.append("}");
    	   htmlResponse.append("</script>");
    	   htmlResponse.append("</body></html>");
        
        PrintWriter out = response.getWriter();
        out.write(htmlResponse.toString());
        out.flush();
        out.close();
        
        System.out.println("성공 응답 전송 완료");
    }
    
    // 실패 응답
    private void sendFailureResponse(HttpServletResponse response) throws IOException {
    	   StringBuilder htmlResponse = new StringBuilder();
    	   htmlResponse.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><script>");
    	   htmlResponse.append("alert('인증에 실패했습니다.');");
    	   htmlResponse.append("try {");
    	   htmlResponse.append("  if (window.opener && !window.opener.closed) {");
    	   htmlResponse.append("    window.opener.location.reload();");  // 부모 창 새로고침
    	   htmlResponse.append("    window.close();");  // 인증 창 닫기
    	   htmlResponse.append("  } else {");
    	   htmlResponse.append("    window.location.href = 'signup.html';");  // 부모 창이 없으면 현재 창을 signup.html로
    	   htmlResponse.append("  }");
    	   htmlResponse.append("} catch(e) {");
    	   htmlResponse.append("  if (window.opener && !window.opener.closed) {");
    	   htmlResponse.append("    window.opener.location.reload();");  // 부모 창 새로고침
    	   htmlResponse.append("  }");
    	   htmlResponse.append("  window.close();");  // 인증 창 닫기
    	   htmlResponse.append("}");
    	   htmlResponse.append("</script></body></html>");
        
        PrintWriter out = response.getWriter();
        out.write(htmlResponse.toString());
        out.flush();
        out.close();
        
        System.out.println("실패 응답 전송 완료");
    }
    
    // 이미 인증된 이메일 응답
    private void sendAlreadyVerifiedResponse(HttpServletResponse response, String email) throws IOException {
        StringBuilder htmlResponse = new StringBuilder();
        
        htmlResponse.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><script>");
        htmlResponse.append("alert('이미 인증된 이메일입니다.');");
        htmlResponse.append("try {");
        htmlResponse.append("  if (window.opener && !window.opener.closed) {");
        htmlResponse.append("    window.opener.postMessage({ type: 'emailAlreadyVerified', email: '" + email + "' }, '*');");
        htmlResponse.append("    window.opener.focus();");
        htmlResponse.append("    window.close();");
        htmlResponse.append("  } else {");
        htmlResponse.append("    window.close();");
        htmlResponse.append("  }");
        htmlResponse.append("} catch(e) {");
        htmlResponse.append("  window.close();");
        htmlResponse.append("}");
        htmlResponse.append("</script></body></html>");
        
        PrintWriter out = response.getWriter();
        out.write(htmlResponse.toString());
        out.flush();
        out.close();
        
        System.out.println("이미 인증된 이메일 응답 전송: " + email);
    }
}





//package com.smhrd.controller;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import com.smhrd.model.EmailDAO;
//
//@WebServlet("/EmailVerification")
//public class EmailVerification extends HttpServlet {
//    private static final long serialVersionUID = 1L;
//
//    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        
//        System.out.println("=== EmailVerification 서블릿 호출됨 ===");
//        
//        // 인코딩 설정
//        request.setCharacterEncoding("UTF-8");
//        response.setContentType("text/html; charset=UTF-8");
//        
//        try {
//            // 1. URL 파라미터에서 토큰 받기
//            String token = request.getParameter("token");
//            System.out.println("받은 인증 토큰: " + token);
//            
//            // 토큰 유효성 검사
//            if (token == null || token.trim().isEmpty()) {
//                System.out.println("ERROR: 토큰이 비어있음");
//                sendFailureResponse(response);
//                return;
//            }
//            
//            // 2. EmailDAO로 토큰 유효성 확인
//            System.out.println("EmailDAO 객체 생성 시도...");
//            EmailDAO emailDAO = new EmailDAO();
//            System.out.println("EmailDAO 객체 생성 성공!");
//            
//            // 2-1. 토큰이 존재하고 만료되지 않았는지 확인
//            System.out.println("isTokenValid 메서드 호출 시도...");
//            boolean isTokenValid = emailDAO.isTokenValid(token);
//            System.out.println("isTokenValid 메서드 호출 완료: " + isTokenValid);
//            
//            if (!isTokenValid) {
//                System.out.println("ERROR: 토큰이 유효하지 않거나 만료됨");
//                sendFailureResponse(response);
//                return;
//            }
//            
//            // 토큰 유효성 확인 후 이메일 정보 가져오기
//            String verifiedEmail = emailDAO.getEmailByToken(token);
//            System.out.println("디버깅: 조회된 이메일 주소 -> " + verifiedEmail);
//            
//            if (verifiedEmail == null) {
//                System.out.println("디버깅 ERROR: 토큰에 해당하는 이메일 정보가 데이터베이스에 없습니다.");
//                sendFailureResponse(response);
//                return;
//            }
//     
//            // 2-2. 이미 인증된 이메일인지 확인
//            boolean alreadyVerified = emailDAO.isEmailVerified(verifiedEmail);
//            System.out.println("이미 인증 여부: " + alreadyVerified);
//            
//            if (alreadyVerified) {
//                System.out.println("INFO: 이미 인증된 이메일");
//                sendAlreadyVerifiedResponse(response, verifiedEmail);
//                return;
//            }
//
//            // 3. 이메일 인증 완료 처리
//            boolean verificationSuccess = emailDAO.verifyEmailToken(token);
//            System.out.println("인증 처리 결과: " + verificationSuccess);
//            
//            if (verificationSuccess) {
//                System.out.println("SUCCESS: 이메일 인증 완료");
//                sendSuccessResponse(response, verifiedEmail);
//            } else {
//                System.out.println("ERROR: 인증 처리 실패");
//                sendFailureResponse(response);
//            }
//            
//        } catch (Exception e) {
//            System.out.println("=== EXCEPTION 발생 ===");
//            System.out.println("에러 메시지: " + e.getMessage());
//            e.printStackTrace();
//            sendFailureResponse(response);
//        }
//    }
//    
//    // 성공 응답
//    private void sendSuccessResponse(HttpServletResponse response, String verifiedEmail) throws IOException {
//        StringBuilder htmlResponse = new StringBuilder();
//        htmlResponse.append("<!DOCTYPE html>");
//        htmlResponse.append("<html>");
//        htmlResponse.append("<head><meta charset='UTF-8'></head>");
//        htmlResponse.append("<body>");
//        htmlResponse.append("<script>");
//        htmlResponse.append("alert('이메일 인증이 완료되었습니다!');");
//        htmlResponse.append("try {");
//        htmlResponse.append("  if (window.opener && !window.opener.closed) {");
//        htmlResponse.append("    window.opener.postMessage({");
//        htmlResponse.append("      type: 'emailVerified',");
//        htmlResponse.append("      email: '" + verifiedEmail + "'");
//        htmlResponse.append("    }, '*');");
//        htmlResponse.append("    window.close();");
//        htmlResponse.append("  } else {");
//        htmlResponse.append("    window.location.href = 'signup.html?verified=true&email=' + encodeURIComponent('" + verifiedEmail + "');");
//        htmlResponse.append("  }");
//        htmlResponse.append("} catch(e) {");
//        htmlResponse.append("  window.location.href = 'signup.html?verified=true&email=' + encodeURIComponent('" + verifiedEmail + "');");
//        htmlResponse.append("}");
//        htmlResponse.append("</script>");
//        htmlResponse.append("</body></html>");
//        
//        PrintWriter out = response.getWriter();
//        out.write(htmlResponse.toString());
//        out.flush();
//        out.close();
//        
//        System.out.println("성공 응답 전송 완료");
//    }
//    
//    // 실패 응답
//    private void sendFailureResponse(HttpServletResponse response) throws IOException {
//        StringBuilder htmlResponse = new StringBuilder();
//        htmlResponse.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><script>");
//        htmlResponse.append("alert('인증에 실패했습니다.');");
//        htmlResponse.append("try {");
//        htmlResponse.append("  if (window.opener && !window.opener.closed) {");
//        htmlResponse.append("    window.opener.focus();");
//        htmlResponse.append("    window.close();");
//        htmlResponse.append("  } else {");
//        htmlResponse.append("    window.location.href = 'signup.html';");
//        htmlResponse.append("  }");
//        htmlResponse.append("} catch(e) { window.location.href = 'signup.html'; }");
//        htmlResponse.append("</script></body></html>");
//        
//        PrintWriter out = response.getWriter();
//        out.write(htmlResponse.toString());
//        out.flush();
//        out.close();
//        
//        System.out.println("실패 응답 전송 완료");
//    }
//    
//    // 이미 인증된 이메일 응답
//    private void sendAlreadyVerifiedResponse(HttpServletResponse response, String email) throws IOException {
//        StringBuilder htmlResponse = new StringBuilder();
//        htmlResponse.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body><script>");
//        htmlResponse.append("alert('이미 인증된 이메일입니다.');");
//        htmlResponse.append("try {");
//        htmlResponse.append("  if (window.opener && !window.opener.closed) {");
//        htmlResponse.append("    window.opener.postMessage({");
//        htmlResponse.append("      type: 'emailAlreadyVerified',");
//        htmlResponse.append("      email: '" + email + "'");
//        htmlResponse.append("    }, '*');");
//        htmlResponse.append("    window.close();");
//        htmlResponse.append("  } else {");
//        htmlResponse.append("    window.location.href = 'signup.html?verified=true&email=' + encodeURIComponent('" + email + "');");
//        htmlResponse.append("  }");
//        htmlResponse.append("} catch(e) {");
//        htmlResponse.append("  window.location.href = 'signup.html?verified=true&email=' + encodeURIComponent('" + email + "');");
//        htmlResponse.append("}");
//        htmlResponse.append("</script></body></html>");
//        
//        PrintWriter out = response.getWriter();
//        out.write(htmlResponse.toString());
//        out.flush();
//        out.close();
//        
//        System.out.println("이미 인증된 이메일 응답 전송: " + email);
//    }
//}