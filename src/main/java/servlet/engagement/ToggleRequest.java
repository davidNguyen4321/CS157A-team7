package servlet.engagement;

import java.util.Map;

import java.io.IOException;

import servlet.security.ParamSecurity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class ToggleRequest {
    private final Integer targetId;
    private final Integer sessionUserId;

    public ToggleRequest(String parameterString, Integer sessionUserId) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        Map<String, Integer> parameterMap = mapper.readValue(parameterString, new TypeReference<Map<String, Integer>>() {});
 
        ParamSecurity.validateParams("toggle request", parameterMap, new String[]{"targetId"});
        
        this.targetId = parameterMap.get("targetId");
        this.sessionUserId = sessionUserId;
    }

    public int getTargetId() {return targetId; }
    public int getUserId() {return sessionUserId; }
}