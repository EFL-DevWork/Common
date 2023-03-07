package com.thoughtworks.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.BusinessException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
public class KeycloakAuthenticationController {
    @GetMapping("/getKeycloakAccessToken")
    public ResponseEntity<String> getKeycloakAccessToken(@RequestParam Map<String, String> allRequestParams, ModelMap model) throws IOException, BusinessException {
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(allRequestParams.get("host") + "/auth/realms/servicestarterrealm/protocol/openid-connect/token");
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "client_credentials"));
            params.add(new BasicNameValuePair("client_id", allRequestParams.get("client_id")));
            params.add(new BasicNameValuePair("client_secret", allRequestParams.get("client_secret")));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            CloseableHttpResponse response = client.execute(httpPost);
            String jsonString = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonString);
            String token = node.get("access_token").asText();
            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (RuntimeException err) {
            throw new IOException();
        } catch (Exception err) {
            throw new BusinessException(InternalErrorCodes.SERVER_ERROR, InternalErrorCodes.SERVER_ERROR.getDescription());
        }
        finally {
            client.close();
        }
    }
}
