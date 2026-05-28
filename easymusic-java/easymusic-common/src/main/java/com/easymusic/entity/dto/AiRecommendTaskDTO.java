package com.easymusic.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiRecommendTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String currentInput;
    private String nodeAddress;
    private Long requestSeq;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCurrentInput() {
        return currentInput;
    }

    public void setCurrentInput(String currentInput) {
        this.currentInput = currentInput;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public Long getRequestSeq() {
        return requestSeq;
    }

    public void setRequestSeq(Long requestSeq) {
        this.requestSeq = requestSeq;
    }
}
