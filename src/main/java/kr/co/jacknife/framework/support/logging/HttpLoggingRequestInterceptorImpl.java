package kr.co.jacknife.framework.support.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**************************************************************************
 *  <br>捲土重來</br>
 *  author : 윤희한
 *  email  : ryys1993@nate.com , scyun2015@gmail.com
 *  Date   : 2018. 9. 3.
 *************************************************************************/
public class HttpLoggingRequestInterceptorImpl extends HttpLoggingRequestInterceptor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public void logRequest(String uuid, HttpRequest request, byte[] body) throws IOException {
            log.info("===========================request begin================================================");
            log.info("URI         : {}", request.getURI());
            log.info("Method      : {}", request.getMethod());
            log.info("Headers     : {}", request.getHeaders());
            log.info("Request body: {}", new String(body, "UTF-8"));
            log.info("==========================request end================================================");
    }
    public void logResponse(String uuid, ClientHttpResponse response) throws IOException {
            log.info("============================response begin==========================================");
            log.info("Status code  : {}", response.getStatusCode());
            log.info("Status text  : {}", response.getStatusText());
            log.info("Headers      : {}", response.getHeaders());
            log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            log.info("=======================response end=================================================");
    }
}
