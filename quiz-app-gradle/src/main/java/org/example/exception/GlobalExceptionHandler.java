package org.example.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/error")
public class GlobalExceptionHandler extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Throwable throwable = (Throwable) req.getAttribute("jakarta.servlet.error.exception");
        Integer statusCode = (Integer) req.getAttribute("jakarta.servlet.error.status_code");
        String message = throwable != null ? throwable.getMessage() : "Unknown error";

        logger.error("Error occurred: {}", message, throwable);

        ErrorResponse errorResponse = new ErrorResponse(message);
        resp.setStatus(statusCode != null ? statusCode : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getOutputStream(), errorResponse);
    }
}
