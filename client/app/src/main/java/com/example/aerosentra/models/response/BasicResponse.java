package com.example.aerosentra.models.response;

public class BasicResponse {

    private boolean success;
    private String message;

    public BasicResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }

}
