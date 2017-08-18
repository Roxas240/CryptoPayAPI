package com.xemplar.libs.cryptorpc.http.client;

import com.xemplar.libs.cryptorpc.http.HttpLayerException;

public interface SimpleHttpClient {
	String execute(String reqMethod, String reqPayload) throws HttpLayerException;
	void close();
}