package com.smhrd.apiController;

import com.smhrd.util.Jsons; // Jsons 유틸리티 임포트

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(
    // 파일 업로드 관련 설정 (필요에 맞게 조절)
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 50,
    maxRequestSize = 1024 * 1024 * 50 * 5
)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            Collection<Part> parts = req.getParts();
            List<Map<String, Object>> uploadedFiles = new ArrayList<>();

            for (Part part : parts) {
                if ("files".equals(part.getName()) && part.getSubmittedFileName() != null) {
                    String originalFileName = part.getSubmittedFileName();
                    
                    // 파일 내용을 직접 읽기 (저장하지 않음)
                    byte[] fileBytes = part.getInputStream().readAllBytes();
                    String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
                    
                    // 파일 정보 생성
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("originalName", originalFileName);
                    fileInfo.put("content", fileContent);  // 파일 내용을 직접 포함
                    fileInfo.put("size", fileBytes.length);
                    
                    uploadedFiles.add(fileInfo);
                }
            }

            if (uploadedFiles.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write(Jsons.error("업로드된 파일이 없습니다."));
                return;
            }

            // 성공 응답 (파일 내용 포함)
            resp.getWriter().write(Jsons.ok(uploadedFiles));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(Jsons.error("파일 업로드 중 오류 발생: " + e.getMessage()));
        }   
        
    }
}