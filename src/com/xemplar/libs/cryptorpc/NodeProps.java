package com.xemplar.libs.cryptorpc;

/**
 * Created by Rohan on 8/15/2017.
 */
public enum NodeProps {
    RPC_PROTOCOL("node.rpc.protocol", "http"),
    RPC_HOST("node.rpc.host", "127.0.0.1"),
    RPC_PORT("node.rpc.port", "8332"),
    RPC_USER("node.rpc.user", "user"),
    RPC_PASSWORD("node.rpc.password", "password"),
    HTTP_AUTH_SCHEME("node.http.auth_scheme", "Basic"),
    ALERT_PORT("node.notification.alert.port", "5158"),
    BLOCK_PORT("node.notification.block.port", "5159"),
    WALLET_PORT("node.notification.wallet.port", "5160");

    NodeProps(String key, String val){
        this.defaultValue = val;
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    private final String key;
    private final String defaultValue;
}
