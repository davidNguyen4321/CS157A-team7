package servlet.engagement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import servlet.security.ParamSecurity;
import servlet.security.ServletParamException;

import java.io.IOException;
import java.util.Map;

public class ToggleRequest {
    private final Integer targetId;
    private final Integer sessionUserId;

    public ToggleRequest(String parameterString, Integer sessionUserId) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        Map<String, Integer> parameterMap = mapper.readValue(parameterString, new TypeReference<Map<String, Integer>>() {});
 
        ParamSecurity.validateParams("toggle request", parameterMap, new String[]{"targetId"});
        
        this.targetId = parameterMap.get("targetId");
        this.sessionUserId = sessionUserId;
        
        if (this.targetId.equals(this.sessionUserId)) {
        	throw new ServletParamException(HttpServletResponse.SC_BAD_REQUEST, "Attempt to interact with self detected");
        }
    }

    public int getTargetId() {return targetId; }
    public int getUserId() {return sessionUserId; }
}