package kr.co.jacknife.framework.support.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


/**************************************************************************
 *  捲土重來
 *  author : 윤희한
 *  email  : ryys1993@nate.com , scyun2015@gmail.com
 *  Date   : 2018. 9. 3.
 *************************************************************************/
public class HttpLoggingRequestInterceptorImpl extends HttpLoggingRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private class ClientHttpResponseImpl implements ClientHttpResponse
    {
        private ClientHttpResponse clientHttpResponse = null;
        private String bodyTxt = null;

        ClientHttpResponseImpl(ClientHttpResponse clientHttpResponse)
        {
            this.clientHttpResponse = clientHttpResponse;
            try {
                this.bodyTxt = StreamUtils.copyToString(this.clientHttpResponse.getBody(), Charset.defaultCharset());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return this.clientHttpResponse.getStatusCode();
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return this.clientHttpResponse.getRawStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return this.clientHttpResponse.getStatusText();
        }

        @Override
        public void close() {
            this.clientHttpResponse.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(this.bodyTxt.getBytes());
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.clientHttpResponse.getHeaders();
        }

        public String getBodyTxt()
        {
            return this.bodyTxt;
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uuid = UUID.randomUUID().toString();
        logRequest(uuid, request, body);
        ClientHttpResponse response = execution.execute(request, body);
        ClientHttpResponseImpl _tmp = new ClientHttpResponseImpl(response);
        logResponse(uuid, _tmp);
        return _tmp;
    }


    public void logRequest(String uuid, HttpRequest request, byte[] body) throws IOException {
        log.info("{}===========================request begin================================================", uuid);
        log.info("{}URI         : {}",uuid,  request.getURI());
        log.info("{}Method      : {}",uuid, request.getMethod());
        log.info("{}Headers     : {}",uuid, request.getHeaders());
        log.info("{}Request body: {}",uuid, new String(body, StandardCharsets.UTF_8));
        log.info("{}==========================request end================================================", uuid);
    }

    @Override
    public void logResponse(String uuid, ClientHttpResponse response) throws IOException {
        log.info("{}============================response begin==========================================", uuid);
        log.info("{}Status code  : {}",uuid, response.getStatusCode());
        log.info("{}Status text  : {}",uuid, response.getStatusText());
        log.info("{}Headers      : {}",uuid, response.getHeaders());
        log.info("{}Response body: {}",uuid, ((ClientHttpResponseImpl)response).getBodyTxt());
        log.info("{}=======================response end=================================================", uuid);
    }

}
