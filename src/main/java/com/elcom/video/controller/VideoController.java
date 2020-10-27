/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.video.controller;

import com.elcom.video.message.RequestMessage;
import com.elcom.video.message.ResponseMessage;
import com.elcom.video.message.SourceResponse;
import com.elcom.video.messaging.rabbitmq.RabbitMQClient;
import com.elcom.video.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.video.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Admin
 */
@RestController
@RequestMapping("/v1.0")
public class VideoController {

    private final Logger LOGGER = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Value("${user.authen.use}")
    private String authenUse;

    @Value("${user.authen.http.url}")
    private String authenHttpUrl;
    
    private static final String BASE_URI  = "https://api.1sk.vn:8405/v1.0/fitness/video/renderComplete"; //product
    //private static final String BASE_URI  = "http://103.21.151.190:8405/v1.0/fitness/video/renderComplete"; //dev test
    private static final String BASE_DIRECT = "/home/truongdx/data/spring-boot/sk365Upload/";
    

    
    @RequestMapping(value = "/compress", method = RequestMethod.POST)
    public ResponseEntity<Object> compressVideo(@RequestBody Map<String, String> bodyMap,
            @RequestHeader Map<String, String> headerMap, HttpServletRequest request) throws URISyntaxException {
        //Authen 
        ResponseMessage response = null;
        if ("http".equalsIgnoreCase(authenUse)) {
            LOGGER.info("Http authen - authorization " + headerMap.get("authorization"));
            // Http -> Call api authen
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", headerMap.get("authorization"));
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);
            // Dữ liệu đính kèm theo yêu cầu.
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.postForObject(authenHttpUrl, requestEntity, ResponseMessage.class);
            LOGGER.info("Http authen response : {}", response != null ? response.toJsonString() : null);
        } else {
            // RPC -> call rpc authen headerMap
            RequestMessage userRpcRequest = new RequestMessage();
            userRpcRequest.setRequestMethod("POST");
            userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
            userRpcRequest.setBodyParam(null);
            userRpcRequest.setUrlParam(null);
            userRpcRequest.setHeaderParam(headerMap);
            LOGGER.info("Call RPC authen - authorization " + headerMap.get("authorization"));
            LOGGER.info("RequestMessage userRpcRequest : " + userRpcRequest.toJsonString());
            String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                    RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
            LOGGER.info("RPC authen response : {}", result);
            if (result != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    response = mapper.readValue(result, ResponseMessage.class);
                } catch (JsonProcessingException ex) {
                    LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                }
            }
        }

        if (response != null && response.getStatus() == HttpStatus.OK.value()) {
            //Process compress video
            String path = BASE_DIRECT + bodyMap.get("source");
            LOGGER.info("- Path : " + path);
            File f = new File(path);
            if (f.exists()) {
                LOGGER.info("- File path : " + BASE_DIRECT + path);
                videoService.compressVideo(path);

                RestTemplate restTemp = new RestTemplate();
                URI uri = new URI(BASE_URI);
                HttpHeaders head = new HttpHeaders();
                head.add("Authorization", headerMap.get("authorization"));
                head.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                head.setContentType(MediaType.APPLICATION_JSON);

                SourceResponse sourceResponse = new SourceResponse(bodyMap.get("source"));
                // Dữ liệu đính kèm theo yêu cầu.
                HttpEntity<SourceResponse> requestEntity = new HttpEntity<>(sourceResponse, head);
                ResponseEntity<String> res = restTemp.postForEntity(uri, requestEntity, String.class);
                LOGGER.info("Http SourceResponse : {}", res != null ? res : null);

                return new ResponseEntity<>("compress success", HttpStatus.OK);
            } else {
                LOGGER.info("file ko ton tai : " + path);
                return new ResponseEntity<>("400", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity(HttpStatus.FORBIDDEN.getReasonPhrase(), HttpStatus.FORBIDDEN);
        }

    }

    @RequestMapping(value = "/converthls", method = RequestMethod.POST)
    public ResponseEntity<Object> convertVideo(@RequestBody Map<String, String> bodyMap,
            @RequestHeader Map<String, String> headerMap, HttpServletRequest request) throws URISyntaxException {
        ResponseMessage response = null;
        if ("http".equalsIgnoreCase(authenUse)) {
            LOGGER.info("Http authen - authorization " + headerMap.get("authorization"));
            // Http -> Callf ("http".equalsIgnoreCase(authenUse)) api authen
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", headerMap.get("authorization"));
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Dữ liệu đính kèm theo yêu cầu.
            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.postForObject(authenHttpUrl, requestEntity, ResponseMessage.class);
            LOGGER.info("Http authen response : {}", response != null ? response.toJsonString() : null);
        } else {
            // RPC -> call rpc authen headerMap
            RequestMessage userRpcRequest = new RequestMessage();
            userRpcRequest.setRequestMethod("POST");
            userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
            userRpcRequest.setBodyParam(null);
            userRpcRequest.setUrlParam(null);
            userRpcRequest.setHeaderParam(headerMap);
            LOGGER.info("Call RPC authen - authorization " + headerMap.get("authorization"));
            LOGGER.info("RequestMessage userRpcRequest : " + userRpcRequest.toJsonString());
            String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                    RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
            LOGGER.info("RPC authen response : {}", result);
            if (result != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    response = mapper.readValue(result, ResponseMessage.class);
                } catch (JsonProcessingException ex) {
                    LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                }
            }
        }
        if (response != null && response.getStatus() == HttpStatus.OK.value()) {
            String path = BASE_DIRECT + bodyMap.get("source");
            LOGGER.info("- File Path : " + path);
            //Process convert HLS video
            File f = new File(path);
            if (f.exists()) {
                LOGGER.info("file ton tai : " + path);
                videoService.convertVideoHls(path);

                RestTemplate restTemp = new RestTemplate();
                URI uri = new URI(BASE_URI);
                HttpHeaders head = new HttpHeaders();
                head.add("Authorization", headerMap.get("authorization"));
                head.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                head.setContentType(MediaType.APPLICATION_JSON);

                SourceResponse sourceResponse = new SourceResponse(bodyMap.get("source"));
                // Dữ liệu đính kèm theo yêu cầu.
                HttpEntity<SourceResponse> requestEntity = new HttpEntity<>(sourceResponse, head);
                ResponseEntity<String> res = restTemp.postForEntity(uri, requestEntity, String.class);

                LOGGER.info("Http SourceResponse : {}", res != null ? res : null);

                return new ResponseEntity<>("convert HLS video success", HttpStatus.OK);
            } else {
                LOGGER.info("file ko ton tai : " + path);
                return new ResponseEntity<>("400", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity(HttpStatus.FORBIDDEN.getReasonPhrase(), HttpStatus.FORBIDDEN);
        }

    }

}
