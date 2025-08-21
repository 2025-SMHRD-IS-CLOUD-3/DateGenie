package com.smhrd.apiController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.smhrd.gemini.AnalysisJobManager;
import com.smhrd.util.Jsons;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/analyze/result")
public class AnalyzeResultServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String jobId = req.getParameter("jobId");
        var job = AnalysisJobManager.get(jobId);

        if (job == null) {
            resp.setStatus(404);
            resp.getWriter().write(Jsons.error("jobId를 찾을 수 없습니다."));
            return;
        }
        if (!"done".equals(job.status)) {
            resp.setStatus(409);
            resp.getWriter().write(Jsons.error("아직 완료되지 않았습니다."));
            return;
        }
        resp.getWriter().write(Jsons.ok(job.result));
    }
}