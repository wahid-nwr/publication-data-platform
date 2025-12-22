package io.wahid.publication.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.NoResultException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class GlobalExceptionFilter implements Filter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch (ResourceNotFoundException e) {
            writeError((HttpServletResponse) response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND",
                    e.getMessage());
        } catch (NoResultException e) {
            writeError((HttpServletResponse) response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND",
                    "Resource not found");
        } catch (IllegalArgumentException e) {
            writeError((HttpServletResponse) response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "BAD_REQUEST",
                    e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // log properly in real apps
            writeError((HttpServletResponse) response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR",
                    "Unexpected server error");
        }
    }

    private void writeError(
            HttpServletResponse resp,
            int status,
            String code,
            String message
    ) throws IOException {

        resp.setStatus(status);
        resp.setContentType("application/json");

        mapper.writeValue(
                resp.getOutputStream(),
                new ErrorResponse(code, message)
        );
    }
}
