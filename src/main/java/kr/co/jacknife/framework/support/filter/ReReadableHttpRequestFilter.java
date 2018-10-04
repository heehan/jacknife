package kr.co.jacknife.framework.support.filter;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReReadableHttpRequestFilter  extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accept = request.getHeader("accept");
//        if (accept != null && ( accept.contains("application/json") || accept.contains("application/xml")  ) )
            filterChain.doFilter(new RequestWrapper(request), response );
//        else
//            filterChain.doFilter(request,response);
    }

    public class ResponseWrapper extends HttpServletResponseWrapper
    {
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }

    public class RequestWrapper extends HttpServletRequestWrapper {
        private byte[] bytes;
        private String requestBody;

        public byte[] toByteArray(){
            return bytes;
        }

        public RequestWrapper(HttpServletRequest request) throws IOException{
            super(request);
            InputStream in = super.getInputStream();
            bytes = IOUtils.toByteArray(in);
            if (request.getContentType() == null
                    || request.getContentType().indexOf(MediaType.MULTIPART_FORM_DATA_VALUE) < 0)
            {
                requestBody = new String(bytes);
            }
            else {
                requestBody = "---- MULTIPART ----";
            }
        }
        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            return  new ServletImpl(bis);
        }
        public String getRequestBody() {
            return this.requestBody;
        }
        class ServletImpl extends ServletInputStream{
            private InputStream is;
            public ServletImpl(InputStream bis){
                is = bis;
            }
            @Override
            public int read() throws IOException {
                return is.read();
            }
            @Override
            public int read(byte[] b) throws IOException {
                return is.read(b);
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        }
    }

}
