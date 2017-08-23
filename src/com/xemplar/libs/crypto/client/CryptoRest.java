package com.xemplar.libs.crypto.client;

import com.xemplar.libs.crypto.common.HttpRest;
import com.xemplar.libs.crypto.common.NetworkListener;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

/**
 * Created by Rohan on 8/22/2017.
 */
public class CryptoRest extends HttpRest{
    public CryptoRest(String pubKey, char[] privKey, String url){
        super(pubKey, privKey, url);
    }

    protected int handlePost(String method, List<NameValuePair> params, NetworkListener.CryptoService listener) {
        if(listener instanceof NetworkListener.CryptoServerService) return -5;
        try {
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            params.clear();
            if (entity != null) {
                InputStream instream = entity.getContent();
                StringBuffer resp = new StringBuffer();
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(instream));
                    String output;

                    while ((output = in.readLine()) != null) {
                        resp.append(output + "\n");
                    }
                    in.close();
                } finally {
                    instream.close();
                }

                System.out.println(resp.toString());
                if(method.equals("addrequ")){
                    int id = Integer.parseInt(resp.toString().trim().split(":")[1]);
                    ((NetworkListener.ClientRequestListener)listener).onRequestSent(id);

                    return 0;
                } else if(method.equals("chkpin")){
                    String pin = "-1";
                    try{
                        pin = resp.toString().trim().split(":")[1];
                    } catch(Exception e){

                    }
                    ((NetworkListener.ClientPaymentListener)listener).onPinReceived(null, pin);

                    return 0;
                } else if(method.equals("chkpay")){
                    String filled = "0";
                    try{
                        filled = resp.toString().trim().split(":")[1];
                    } catch(Exception e){

                    }
                    ((NetworkListener.ClientPaymentListener)listener).onPayReceived(null, filled);

                    return 0;
                } else if(method.equals("cancel")){
                    ((NetworkListener.ClientPaymentListener)listener).onPayError(null, null);

                    return 0;
                }
            }
        } catch (ProtocolException e) {
            this.e = e;
            return -1;
        } catch (MalformedURLException e) {
            this.e = e;
            return -2;
        } catch (IOException e) {
            this.e = e;
            return -3;
        }

        return -4;
    }
}
