package com.xemplar.libs.crypto.server.net.domain;

/**
 * Created by Rohan on 8/21/2017.
 */
public class Request {
    public String amount, code;
    public int id, filled;

    public Request(){}
    public Request(int id, int filled, String amount){
        this.id = id;
        this.filled = filled;
        this.amount = amount;
    }

    public int getId(){
        return id;
    }
    public int getFilled(){
        return filled;
    }
    public String getAmount(){
        return amount;
    }
    public String getCode(){
        return code;
    }

    public void setId(int id){
        this.id = id;
    }
    public void setFilled(int filled){
        this.filled = filled;
    }
    public void setAmount(String amount){
        this.amount = amount;
    }
    public void setCode(String code){
        this.code = code;
    }
}
