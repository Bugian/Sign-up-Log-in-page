package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private long timestamp;
    private String path;
    private int status;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ErrorResponse(String message, String path, int status) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.path = path;
        this.status = status;
    }
}
