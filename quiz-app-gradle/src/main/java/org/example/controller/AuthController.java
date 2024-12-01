package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.*;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class AuthController extends HttpServlet {
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(){
        this.authService = new AuthService();
        this.objectMapper = new ObjectMapper();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        String path = req.getPathInfo();

        try {
            switch (path) {
                case "/login":
                    handleLogin(req, resp);
                    break;
                case "/register":
                    handleRegister(req, resp);
                    break;
                case "/refresh":
                    handleRefreshToken(req, resp);
                    break;
                case "/change-password":
                    handleChangePassword(req, resp);
                    break;
                default:
                    sendError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(req, resp, e);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);

            // Validate request
            try {
                loginRequest.validate();
            } catch (IllegalArgumentException e) {
                LoginResponse errorResponse = LoginResponse.error(e.getMessage());
                sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
                return;
            }

            String token = authService.login(loginRequest);
            LoginResponse response = new LoginResponse(token, loginRequest.getUsername());

            sendResponse(resp, HttpServletResponse.SC_OK, response);
        } catch (AuthException e) {
            LoginResponse errorResponse = LoginResponse.error(e.getMessage());
            sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            SignupRequest registerRequest = objectMapper.readValue(req.getReader(), SignupRequest.class);

            try{
                registerRequest.validate();
            } catch (IllegalArgumentException e) {
                SignupResponse errorResponse = SignupResponse.error(e.getMessage());
                sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, errorResponse);
                return;
            }
            //Daca validarea a trecut, se procedeaza inregistrarea
            authService.register(registerRequest);
            SignupResponse successResponse = SignupResponse.success(registerRequest.getUsername());
            sendResponse(resp, HttpServletResponse.SC_OK, successResponse);
        } catch (AuthException e) {
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            String token = extractToken(req);

            ChangePasswordRequest changePasswordRequest = objectMapper.readValue(req.getReader(), ChangePasswordRequest.class);
            try {
                changePasswordRequest.validate();
            } catch (IllegalArgumentException e){
                sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return;
            }

            Long userId = authService.validateToken(token).getId();

            //change password
            authService.changePassword(
                    userId,
                    changePasswordRequest.getOldPassword(),
                    changePasswordRequest.getNewPassword()
            );

            sendResponse(resp, HttpServletResponse.SC_OK,
                    Map.of("message", "Password changed successfully"));
        } catch (AuthException e) {
            sendError(req, resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private void handleRefreshToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            String token = extractToken(req);
            if (token == null) {
                throw new AuthException("No token provided");
            }

            String newToken = authService.refreshToken(token);
            sendResponse(resp, HttpServletResponse.SC_OK,
                    Map.of("token", newToken));

        } catch (AuthException e) {
            sendError(req, resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        String path = req.getPathInfo();

        try {
            if (path.equals("/validate")) {
                handleValidateToken(req, resp);
            } else {
                sendError(req, resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(req, resp, e);
        }
    }

    private void handleValidateToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            String token = extractToken(req);
            if (token == null) {
                throw new AuthException("No token provided");
            }

            User user = authService.authenticateToken(token);
            sendResponse(resp, HttpServletResponse.SC_OK,
                    Map.of("valid", true,
                            "username", user.getEmail(),
                            "userId", user.getId()));
        } catch (AuthException e) {
            sendError(req, resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private void sendResponse(HttpServletResponse resp, int status, Object data)
            throws IOException {
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), data);
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws IOException {
        ErrorResponse error = new ErrorResponse(message, req.getRequestURI(), status);
        resp.setStatus(status);
        objectMapper.writeValue(resp.getWriter(), error);
    }

    private void handleException(HttpServletRequest req, HttpServletResponse resp, Exception e)
            throws IOException {
        if (e instanceof AuthException) {
            logger.warn("Authentication error: {}", e.getMessage());
            sendError(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else {
            logger.error("Unexpected error in AuthController", e);
            sendError(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred");
        }
    }
}
