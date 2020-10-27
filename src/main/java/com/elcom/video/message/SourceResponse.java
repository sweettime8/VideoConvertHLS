/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.video.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class SourceResponse {

    private String source;

    public SourceResponse() {
    }
    
    public SourceResponse(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ResponseMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
