package com.xemplar.libs.crypto.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.xemplar.libs.crypto.common.NetworkListener;
import javafx.scene.image.Image;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rohan on 8/22/2017.
 */
public final class CryptoClient {
    private volatile RequestHandler handler;
    private Thread requestThread;
    private final String cryptoAddress;
    private CryptoRest rest;
    private final int port;

    /**
     * Constructor for the CryptoClient interface, allows dev to send payment requests.
     * @param cryptoAddress The address in your wallet that you wish to receive funds to.
     * @param webAddress The web address of your server's page that handles payment.
     * @param user The username of your mysql or equivalent server.
     * @param pass The password of your mysql or equivalent server.
     * @param enckey The encryption password set in your crypto.php or crypto.php
     */
    public CryptoClient(String cryptoAddress, String webAddress, String user, char[] pass, char[] enckey){
        this.cryptoAddress = cryptoAddress;
        this.port = 80;

        this.rest = new CryptoRest(user, pass,enckey, webAddress);
    }

    /**
     * Method to get the JavaFX Image QR code for easy input of address
     * @param width width of the image
     * @param height height of the image
     * @return Java FX Image
     * @throws IOException Either when there is an encoding error or java's reserved memory is overwritten.
     * @throws WriterException When ZXING encounters an error.
     */
    public Image getQR(int width, int height) throws IOException, WriterException{
        String charset = "UTF-8";
        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitMatrix matrix = new MultiFormatWriter().encode(new String(cryptoAddress.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height, hintMap);
        MatrixToImageWriter.writeToStream(matrix, "png", stream);
        stream.flush();

        byte[] data = stream.toByteArray();
        stream.close();

        return new Image(new ByteArrayInputStream(data));
    }

    /**
     * Makes a payment request to the specified URL in the constructor.
     * @param key Key for request callback.
     * @param name Notes entry on database.
     * @param cost Cost of that Item in whole units of coin.
     * @param listener Callback for when requests are paid for.
     * @return Request was submitted or not.
     */
    public boolean makeRequest(final String key, String name, BigDecimal cost, final NetworkListener.ClientPaymentListener listener){
        if(handler != null) return false;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("amount", cost.toPlainString()));
        params.add(new BasicNameValuePair("notes", name));

        int res = rest.doPost("addrequ", params, new NetworkListener.ClientRequestListener() {
            public void onRequestSent(int requestID) {
                handler = new RequestHandler(rest, key, requestID, listener);
                requestThread = new Thread(handler);
                requestThread.start();
            }
        });

        return res == 0;
    }

    /**
     * Makes a payment request to the specified URL in the constructor.
     * @param key Key for request callback
     * @param cost Cost of that Item in whole units of coin.
     * @param listener Callback for when requests are paid for.
     * @return Request was submitted or not.
     */
    public boolean makeRequest(final String key, BigDecimal cost, final NetworkListener.ClientPaymentListener listener){
        if(handler != null) return false;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("amount", cost.toPlainString()));
        params.add(new BasicNameValuePair("notes", "nil"));

        int res = rest.doPost("addrequ", params, new NetworkListener.ClientRequestListener() {
            public void onRequestSent(int requestID) {
                handler = new RequestHandler(rest, key, requestID, listener);
                requestThread = new Thread(handler);
                requestThread.start();
            }
        });

        return res == 0;
    }

    public boolean cancelRequest(final String key){
        if(handler == null) return false;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("id", handler.getID() + ""));

        int res = rest.doPost("cancel", params, new NetworkListener.ClientPaymentAdapter());

        handler.cancel();

        return res == 0;
    }
}
