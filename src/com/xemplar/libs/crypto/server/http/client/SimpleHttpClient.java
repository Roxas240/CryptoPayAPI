package com.xemplar.libs.crypto.server.http.client;

import com.xemplar.libs.crypto.server.http.HttpLayerException;

public interface SimpleHttpClient {
	String execute(String reqMethod, String reqPayload) throws HttpLayerException;
	void close();
}