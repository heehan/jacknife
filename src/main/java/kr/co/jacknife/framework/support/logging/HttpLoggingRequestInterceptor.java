package kr.co.jacknife.framework.support.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.UUID;

/**************************************************************************
 *  捲土重來
 *  author : 윤희한
 *  email  : ryys1993@nate.com , scyun2015@gmail.com
 *  Date   : 2018. 9. 3.
 *************************************************************************/
public abstract class HttpLoggingRequestInterceptor implements ClientHttpRequestInterceptor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uuid = UUID.randomUUID().toString();
        logRequest(uuid, request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(uuid, response);
        return response;
    }
    public abstract void logRequest(String uuid, HttpRequest request, byte[] body) throws IOException ;
    public abstract void logResponse(String uuid, ClientHttpResponse response) throws IOException ;
}
