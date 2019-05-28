package org.codingmatters.rest.undertow;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import org.codingmatters.rest.api.RequestDelegate;
import org.codingmatters.rest.api.internal.UriParameterProcessor;
import org.codingmatters.rest.io.headers.HeaderEncodingHandler;
import org.codingmatters.rest.undertow.internal.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by nelt on 4/27/17.
 */
public class UndertowRequestDelegate implements RequestDelegate {

    static private final Logger log = LoggerFactory.getLogger(UndertowRequestDelegate.class);

    private final HttpServerExchange exchange;
    private Map<String, Map<String, List<String>>> uriParamsCache = new HashMap<>();
    private Map<String, List<String>> queryParamsCache = null;
    private Map<String, List<String>> headersCache = null;

    private final AtomicReference<RequestBody> body = new AtomicReference<>(null);

    public UndertowRequestDelegate(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String path() {
        return this.exchange.getRequestPath();
    }

    @Override
    public Matcher pathMatcher(String regex) {
        log.debug("req={} ; rel={} ; res={}",
                this.exchange.getRequestPath(),
                this.exchange.getRelativePath(),
                this.exchange.getResolvedPath()
        );
        return Pattern.compile(regex).matcher(this.path());
    }

    @Override
    public Method method() {
        String methodString = exchange.getRequestMethod().toString().toUpperCase();
        for (Method method : Method.values()) {
            if(method.name().equals(methodString)) {
                return method;
            }
        }

        return Method.UNIMPLEMENTED;
    }

    @Override
    public InputStream payload() {
        synchronized (this.body) {
            if(this.body.get() == null) {
                if(! this.exchange.isBlocking()) {
                    this.exchange.startBlocking();
                }
                this.body.set(RequestBody.from(exchange));
            }
        }

        return this.body.get().inputStream();
    }

    @Override
    public String contentType() {
        return this.exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
    }

    @Override
    public String absolutePath(String relative) {
        if(relative == null) {
            relative = "";
        }
        while(relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        return String.format("%s://%s/%s",
                exchange.getRequestScheme(),
                exchange.getHostAndPort(),
                relative
        );
    }

    @Override
    public Map<String, List<String>> uriParameters(String pathExpression) {
        if(! this.uriParamsCache.containsKey(pathExpression)) {
            this.uriParamsCache.put(pathExpression, new UriParameterProcessor(this).process(pathExpression));
        }
        return this.uriParamsCache.get(pathExpression);
    }

    @Override
    public synchronized Map<String, List<String>> queryParameters() {
        if(this.queryParamsCache == null) {
            this.queryParamsCache = new HashMap<>();
            for (String name : this.exchange.getQueryParameters().keySet()) {
                if (this.exchange.getQueryParameters().get(name) != null) {
                    this.queryParamsCache.put(name, new ArrayList<>(this.exchange.getQueryParameters().get(name)));
                }
            }
        }
        return this.queryParamsCache;
    }

    @Override
    public synchronized Map<String, List<String>> headers() {
        if(this.headersCache == null) {
            this.headersCache = new HashMap<>();
            for (HeaderValues headerValues : this.exchange.getRequestHeaders()) {
                String headerName = headerValues.getHeaderName().toString();
                List<String> collect = new ArrayList<>( headerValues );
                if( headerName.endsWith( "*" )){
                    headerName = headerName.substring( 0, headerName.length()-1 );
                    collect = headerValues.stream().map( HeaderEncodingHandler::decodeHeader ).collect( Collectors.toList() );
                }
                this.headersCache.putIfAbsent( headerName, new ArrayList<>() );
                this.headersCache.get( headerName ).addAll( collect );
            }
        }
        return this.headersCache;
    }

    @Override
    public void close() throws Exception {
        if(this.body.get() != null) {
            this.body.get().close();
        }
    }
}
