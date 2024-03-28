package com.api19_4.api19_4.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class BaseResponse {
    private int status;
    private long processDuration;
    private Date responseTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date clientTime;

    private String clientMessageId;

}
