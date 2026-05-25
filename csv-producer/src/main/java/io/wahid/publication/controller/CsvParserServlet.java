package io.wahid.publication.controller;

import io.wahid.publication.CsvMain;
import io.wahid.publication.exception.BatchProcessingException;
import io.wahid.publication.service.CsvInputStreamProvider;
import io.wahid.publication.util.*;
import io.wahid.publication.service.CsvProcessor;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = "/api/csv/parser/initiate")
public class CsvParserServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CsvParserServlet.class.getName());
    private static final Pattern JOB_STATUS_PATTERN = Pattern.compile("/csv/job/status/([0-9a-fA-F-]{36})");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setJsonContentType(resp);
        route("GET", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setJsonContentType(resp);
        route("POST", req, resp);
    }

    private void route(String method, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String path = req.getPathInfo();
        Matcher m = JOB_STATUS_PATTERN.matcher(path);
        switch (method) {
            case "GET":
                if (m.matches()) {
                    String uuid = m.group(1);
                    getJobStatus(resp, uuid);
                }
                break;

            case "POST":
                if ("/csv/parser/initiate".equals(path)) {
                    LOGGER.log(Level.INFO, "Initiating parse job!");
                    runCsvJob(resp);
                }
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getJobStatus(HttpServletResponse resp, String jobId) throws IOException {
        if (JobRegistry.get(jobId) != null) {
            Map<String, String> status = Map.of("jobId", jobId,
                    "status", JobRegistry.get(jobId).name(),
                    "jobType", JobType.PARSER.name());
            resp.getWriter().write(JsonUtil.toJson(status));
        } else {
            LOGGER.log(Level.INFO,"No job found with id -> {0}", jobId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void runCsvJob(HttpServletResponse resp) throws IOException {
        String jobId = UUID.randomUUID().toString();
        LOGGER.log(Level.INFO,"Creating CSV parsing job: {0}", jobId);
        JobRegistry.create(jobId);
        PublicationServiceExecutor.submit(() -> {
            Instant start = Instant.now();
            try {
                LOGGER.log(Level.INFO,"Starting CSV parsing job: {0}", jobId);
                fileRead();
                JobRegistry.update(jobId, JobStatus.RUNNING);
                new CsvProcessor().startParsingCsv();
                Instant end = Instant.now();
                LOGGER.log(Level.INFO, "CSV parsing completed for job: {0}", jobId);
                LOGGER.log(Level.INFO, "Total time -> {0} seconds", Duration.between(start, end).toSeconds());
                JobRegistry.update(jobId, JobStatus.SUCCESS);
            } catch (Exception e) {
                JobRegistry.update(jobId, JobStatus.FAILED);
                throw new BatchProcessingException("Error in CSV job " + jobId + ": " + e.getMessage(), e);
            } finally {
                try {
                    Thread.sleep(3000);
                    JobRegistry.update(jobId, JobStatus.IDLE);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Job Closing error:" + jobId, e);
                    throw new RuntimeException(e);
                }
            }
        });
        resp.setStatus(202);
        Map<String, String> status = Map.of("jobId", jobId,
                "status", JobRegistry.get(jobId).name(),
                "jobType", JobType.PARSER.name());
        resp.getWriter().write(JsonUtil.toJson(status));
    }

    private void setJsonContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    private void fileRead() {
        String path = "data/autoren.csv";
        try (InputStream is = CsvMain.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("From servlet Classpath resource not found: " + path);
            } else {
                System.out.println("input length -> " + is.available());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Classpath resource not found: {0}", path);
        }
    }
}
