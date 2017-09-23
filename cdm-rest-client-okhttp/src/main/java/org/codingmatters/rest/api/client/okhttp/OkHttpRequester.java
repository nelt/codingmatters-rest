package org.codingmatters.rest.api.client.okhttp;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.codingmatters.rest.api.client.Requester;
import org.codingmatters.rest.api.client.ResponseDelegate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class OkHttpRequester implements Requester {

    private final OkHttpClient client;
    private final String baseUrl;

    private String path = "/";
    private final TreeMap<String, String[]> parameters = new TreeMap<>();
    private final TreeMap<String, String> headers = new TreeMap<>();


    public OkHttpRequester(OkHttpClient client, String url) {
        this.client = client;
        this.baseUrl = url;
    }

    @Override
    public ResponseDelegate get() throws IOException {
        Request request = this.prepareRequestBuilder().get().build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    public ResponseDelegate post(String contentType, byte[] body) throws IOException {
        Request request = this.prepareRequestBuilder().post(RequestBody.create(MediaType.parse(contentType), body)).build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    @Override
    public ResponseDelegate put(String contentType, byte[] body) throws IOException {
        Request request = this.prepareRequestBuilder().put(RequestBody.create(MediaType.parse(contentType), body)).build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    @Override
    public ResponseDelegate patch(String contentType, byte[] body) throws IOException {
        Request request = this.prepareRequestBuilder().patch(RequestBody.create(MediaType.parse(contentType), body)).build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    @Override
    public ResponseDelegate delete() throws IOException {
        Request request = this.prepareRequestBuilder().delete().build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    @Override
    public ResponseDelegate delete(String contentType, byte[] body) throws IOException {
        Request request = this.prepareRequestBuilder().delete(RequestBody.create(MediaType.parse(contentType), body)).build();
        return new OkHttpResponseDelegate(this.client.newCall(request).execute());
    }

    @Override
    public Requester parameter(String name, String[] value) {
        this.parameters.put(name, value);
        return this;
    }

    @Override
    public Requester parameter(String name, Iterable<String> value) {
        if(value != null) {
            LinkedList<String> params = new LinkedList<>();
            for (String v : value) {
                params.add(v);
            }

            return this.parameter(name, params.toArray(new String[params.size()]));
        }
        return this.parameter(name, new String[0]);
    }

    public Requester header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Requester path(String path) {
        this.path = path;
        return this;
    }

    protected String path() {
        return path;
    }
    protected TreeMap<String, String[]> parameters() {
        return parameters;
    }
    protected TreeMap<String, String> headers() {
        return headers;
    }



    private Request.Builder prepareRequestBuilder() throws UnsupportedEncodingException {
        String url = this.baseUrl + this.path();

        boolean first = true;
        for (Map.Entry<String, String[]> queryParameterEntry : this.parameters().entrySet()) {
            for (String queryParameterValue : queryParameterEntry.getValue()) {
                if(first) {
                    url += "?";
                    first = false;
                } else {
                    url += "&";
                }
                url += this.encode(queryParameterEntry.getKey());
                url += "=";
                url += queryParameterEntry.getValue() != null ? this.encode(queryParameterValue) : "null";
            }
        }

        Request.Builder result = new Request.Builder().url(url);

        for (Map.Entry<String, String> headerEntry : this.headers().entrySet()) {
            result.header(headerEntry.getKey(), headerEntry.getValue());
        }

        return result;
    }

    private String encode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }
}
