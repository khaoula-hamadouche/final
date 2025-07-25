package com.example.pdfapi;

import java.util.Map;

public class ApiResponse {
    private Map<String, String> fileDetails;

    public Map<String, String> getFileDetails() {
        return fileDetails;
    }

    public void setFileDetails(Map<String, String> fileDetails) {
        this.fileDetails = fileDetails;
    }
}
