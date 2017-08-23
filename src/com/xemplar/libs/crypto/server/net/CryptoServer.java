package com.xemplar.libs.crypto.server.net;
import com.xemplar.libs.crypto.common.NetworkListener;
import com.xemplar.libs.crypto.server.net.domain.Request;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rohan on 8/20/2017.
 */
public class CryptoServer implements Runnable{
    public static int MIN_CONFIRMS = 3;
    public boolean running = false;
    CryptoLink link;

    public CryptoServer(String cryptoAddress, String webAddress, String user, char[] pass, String config){
        this.link = new CryptoLink(cryptoAddress, webAddress, user, pass);
        try {
            link.initialize(config);
        } catch(Exception e){
            e.printStackTrace();
        }
        running = true;
    }

    public void run(){
        while(running){
            searchForRequests();
            setupPaymentHandlers();
            CryptoLink.sleep(1000);
        }
    }

    public volatile List<PaymentHandler> payments = new ArrayList<>();
    public void setupPaymentHandlers(){
        System.out.println("Running Handlers: " + payments.size());
        System.out.println("Request Count: " + requests.size());
        for(Request r : requests){
            payments.add(new PaymentHandler(this, r, new NetworkListener.ServerPaymentListener() {
                public void paymentReceived(PaymentHandler handler, String txID) {
                    System.out.println(handler.getID() + " has paid: " + handler.getAmount());
                    payments.remove(handler);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("id", handler.getID() + ""));
                    params.add(new BasicNameValuePair("txID", txID));

                    CryptoServer.this.link.doPost("setpaid", params, null);
                }

                public void paymentCanceled(PaymentHandler handler) {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("id", handler.getID() + ""));
                    CryptoServer.this.link.doPost("remove", params, null);

                    payments.remove(handler);
                }
            }));
        }
        requests.clear();

        for(PaymentHandler pay : payments){
            if(pay.hasStarted()) continue;
            Thread t = new Thread(pay);
            t.start();
        }
    }

    public volatile List<Request> requests = new ArrayList<>();
    public volatile List<String> codes = new ArrayList<>();
    public void searchForRequests(){
        if(link.initialized()){
            link.findRequests(new NetworkListener.ServerRequestListener() {
                public void requestsFound(Request[] requests) {
                    for(Request r : requests){
                        boolean found = false;
                        for(int i = 0; i < payments.size(); i++){
                            boolean seen = payments.get(i).hasRequest(r);
                            found |= seen;

                            if(seen && r.filled == -1){
                                payments.get(i).cancel();
                            }
                        }
                        if(!found) {
                            CryptoServer.this.requests.add(r);
                            System.out.println("  Request Found: " + r.getId());
                        }
                    }
                }

                public void noRequests() {

                }
            });
        }
    }

    public static void setMinimumConfirms(int confirms){
        MIN_CONFIRMS = confirms;
    }

    public boolean checkCode(String code){
        if(code == null) return false;
        for(String s : codes){
            if(s.equals(code)) return false;
        }
        codes.add(code);
        return true;
    }
}
