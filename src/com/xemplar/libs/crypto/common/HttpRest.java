package com.xemplar.libs.crypto.common;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

/**
 * Created by Rohan on 8/22/2017.
 */
public abstract class HttpRest {
    protected final HttpClient httpclient;
    protected String url;
    protected String user;
    protected char[] pass;
    protected Exception e;

    public HttpRest(String pubKey, char[] privKey, String url){
        this.httpclient = HttpClients.createDefault();
        this.url = url;
        this.user = pubKey;
        this.pass = privKey;
    }

    public Exception getError(){
        return e;
    }
    public void setURL(String webURL){
        this.url = webURL;
    }
    public int doPost(String method, List<NameValuePair> params, NetworkListener.CryptoService listener){
        params.add(new BasicNameValuePair("user", user));
        params.add(new BasicNameValuePair("pass", new String(pass)));
        params.add(new BasicNameValuePair("method", method));

        return handlePost(method, params, listener);
    }
    protected abstract int handlePost(String method, List<NameValuePair> params, NetworkListener.CryptoService listener);
}
