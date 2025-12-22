package io.wahid.publication.controller;

import io.wahid.publication.blockchain.BlockchainClient;
import io.wahid.publication.blockchain.BlockchainService;
import io.wahid.publication.exception.BatchProcessingException;
import io.wahid.publication.model.PublicationModel;
import io.wahid.publication.model.web3.PublicationTransaction;
import io.wahid.publication.service.IPublicationManager;
import io.wahid.publication.service.impl.PublicationManagerImpl;
import io.wahid.publication.service.kafka.orchestrator.PublicationOrchestratorApp;
import io.wahid.publication.util.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersisterServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PersisterServlet.class.getName());
    private static final Pattern JOB_STATUS_PATTERN = Pattern.compile("/publication/job/status/([0-9a-fA-F-]{36})");
    private static final Pattern PUBLICATION_STATUS_PATTERN = Pattern.compile("^/publication/verify/(\\d{4}-\\d{4}-\\d{4})$");
    private final transient IPublicationManager manager;
    private final BlockchainService blockchainService;

    public PersisterServlet() throws Exception {
        manager = PublicationManagerImpl.createInitialized();
        BlockchainClient blockchainClient = new BlockchainClient();
        this.blockchainService = new BlockchainService(blockchainClient);
    }

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
        System.out.println("path->" + path);
        System.out.println("building path matchers.");
        Matcher m = JOB_STATUS_PATTERN.matcher(path);
        Matcher verifyMatcher = PUBLICATION_STATUS_PATTERN.matcher(path);
        System.out.println("checking for endpoints from path.");
        System.out.println(verifyMatcher.matches());
        switch (method) {
            case "GET":
                if (m.matches()) {
                    String uuid = m.group(1);
                    getJobStatus(resp, uuid);
                } else if (path.matches("/books/\\d+")) {
                    getBook(resp, path.substring("/books/".length()));
                } else if ("/books/".equals(path)) {
                    getAllBooks(resp);
                } else if ("/magazines/".equals(path)) {
                    getAllMagazines(resp);
                } else if ("/authors/".equals(path)) {
                    getAllAuthors(resp);
                } else if ("/summary/".equals(path)) {
                    getSummary(resp);
                } else if (verifyMatcher.matches()) {
                    String id = verifyMatcher.group(1);
                    verifyBookHash(id, resp);
                }
                break;

            case "POST":
                if ("/publication/initiate".equals(path)) {
                    initiateJob(resp);
                }
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void initiateJob(HttpServletResponse resp) throws IOException {
        String jobId = UUID.randomUUID().toString();
        JobRegistry.create(jobId);
        PublicationServiceExecutor.submit(() -> {
            try {
                Instant start = Instant.now();
                JobRegistry.update(jobId, JobStatus.RUNNING);
                PublicationOrchestratorApp orchestratorApp = new PublicationOrchestratorApp();
                orchestratorApp.run();
                Instant end = Instant.now();
                LOGGER.info("Csv parsing complete.");
                LOGGER.log(Level.INFO, "Total time taken -> {0}  seconds", Duration.between(start, end).toSeconds());
                JobRegistry.update(jobId, JobStatus.SUCCESS);
            } catch (Exception e) {
                JobRegistry.update(jobId, JobStatus.FAILED);
                throw new BatchProcessingException(e.getMessage(), e);
            } finally {
                try {
                    Thread.sleep(3000);
                    JobRegistry.update(jobId, JobStatus.IDLE);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
//        resp.getWriter().write("{\"jobId\": \"" + jobId + "\"}");
        Map<String, String> status = Map.of("jobId", jobId,
                "status", JobRegistry.get(jobId).name(),
                "jobType", JobType.PERSISTER.name());
        resp.getWriter().write(JsonUtil.toJson(status));
    }

    private void getJobStatus(HttpServletResponse resp, String jobId) throws IOException {
        if (JobRegistry.get(jobId) != null) {
            System.out.println("job status->" + JobRegistry.get(jobId));
            Map<String, String> status = Map.of("jobId", jobId,
                    "status", JobRegistry.get(jobId).name(),
                    "jobType", JobType.PERSISTER.name());
            resp.getWriter().write(JsonUtil.toJson(status));
        } else {
            System.out.println("No job found with id -> " + jobId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private void getAllBooks(HttpServletResponse resp) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(manager.getBooks(0, 100)));
    }

    private void getAllMagazines(HttpServletResponse resp) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(manager.getMagazines(0, 100)));
    }

    private void getAllAuthors(HttpServletResponse resp) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(manager.getAuthors(0, 100)));
    }

    private void getBook(HttpServletResponse resp, String id) throws IOException {
        resp.getWriter().write(JsonUtil.toJson(manager.getPublicationById(Long.valueOf(id))));
    }

    private void verifyBookHash(String id, HttpServletResponse resp) {
        PublicationModel publication = manager.getPublicationByISBN(id);
        String canonical = publication.toCanonicalString();
        System.out.println("verifying publication canonical->" + canonical);
        try {
            Boolean status = blockchainService.verifyPublication(id, canonical);
            PublicationTransaction transaction = manager.getWeb3TransactionByIsbn(id);
            System.out.println("status->>>>>>>>" + status);
            Map<String, String> verifyResponse = Map.of("verified", status.toString(),
                    "txHash", transaction.getTransaction());
            resp.getWriter().write(JsonUtil.toJson(verifyResponse));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void getSummary(HttpServletResponse resp) throws IOException {
        long authors = manager.countAuthors();
        long books = manager.countBooks();
        long magazines = manager.countMagazines();
        Map<String, Long> summary = Map.of("authors", authors, "books", books, "magazines", magazines);
        resp.getWriter().write(JsonUtil.toJson(summary));
    }

    private void setJsonContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
