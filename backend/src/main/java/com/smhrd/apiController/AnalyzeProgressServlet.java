package com.smhrd.apiController;

import com.google.gson.JsonObject;
import com.smhrd.gemini.AnalysisJobManager;
import com.smhrd.util.Jsons;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/api/analyze/progress")
public class AnalyzeProgressServlet extends HttpServlet {
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
        JsonObject data = new JsonObject();
        data.addProperty("jobId", jobId);
        data.addProperty("percent", job.percent);
        data.addProperty("status", job.status);
        data.addProperty("message", job.message);

        resp.getWriter().write(Jsons.ok(data));
    }
}