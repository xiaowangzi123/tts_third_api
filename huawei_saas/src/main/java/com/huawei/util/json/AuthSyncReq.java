package com.huawei.util.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthSyncReq {
    private String instanceId;
    private String tenantId;
    private String appId;
    private Date currentSyncTime;
    private Date timeStamp;

    private List<HwAuthUser> userList;
}
