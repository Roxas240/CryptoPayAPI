package com.xemplar.libs.crypto.client;

import com.xemplar.libs.crypto.common.NetworkListener;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohan on 8/22/2017.
 */
public class RequestHandler implements Runnable {
    private final NetworkListener.ClientPaymentListener listener;
    private final CryptoRest rest;
    private final String key;
    private final int id;

    private boolean paid = false, pinned = false;

    RequestHandler(CryptoRest rest, String key, int id, NetworkListener.ClientPaymentListener listener){
        this.listener = listener;
        this.rest = rest;
        this.key = key;
        this.id = id;
    }

    public void run(){
        List<NameValuePair> params = new ArrayList<>();
        while(!pinned){
            params.add(new BasicNameValuePair("id", id + ""));
            int req = rest.doPost("chkpin", params, new NetworkListener.ClientPaymentAdapter() {
                public void onPinReceived(String nil, String pin) {
                    if(!pin.equals("-1")) {
                        listener.onPinReceived(key, pin);
                        pinned = true;
                    }
                }
                public void onPayError(String nil, Exception e) {
                    pinned = true;
                    paid = true;
                    listener.onPayError(key, e);
                }
            });
            if(req != 0){
                pinned = true;
                paid = true;
                listener.onPayError(key, rest.getError());
            }
            params.clear();
            sleep(1000);
        }
        while(!paid){
            params.add(new BasicNameValuePair("id", id + ""));
            int req = rest.doPost("chkpay", params, new NetworkListener.ClientPaymentAdapter() {
                public void onPayReceived(String nil, String txid) {
                    if(txid.equals("1")) {
                        listener.onPayReceived(key, txid);
                        paid = true;
                    }
                }
                public void onPayError(String nil, Exception e) {
                    pinned = true;
                    paid = true;
                    listener.onPayError(key, e);
                }
                public void onConfirmUpdate(String nil, int confirms) {
                    listener.onConfirmUpdate(key, confirms);
                }
            });
            if(req != 0){
                pinned = true;
                paid = true;
                listener.onPayError(key, rest.getError());
            }
            params.clear();
            sleep(1000);
        }
    }

    public void cancel(){
        paid = true;
        pinned = true;
        listener.onPayCanceled(key);
    }

    public int getID(){
        return id;
    }

    public static void sleep(long mills){
        try{
            Thread.sleep(mills);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
