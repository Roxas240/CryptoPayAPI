package com.xemplar.libs.cryptorpc.jsonrpc.client;

import com.xemplar.libs.cryptorpc.*;
import com.xemplar.libs.cryptorpc.jsonrpc.JsonMapper;
import com.xemplar.libs.cryptorpc.jsonrpc.JsonPrimitiveParser;

import java.util.List;

public interface JsonRpcClient {
	public abstract String execute(String method) throws CryptocoinException, CommunicationException;
	public abstract <T> String execute(String method, T param) throws CryptocoinException, CommunicationException;
	public abstract <T> String execute(String method, List<T> params) throws CryptocoinException,  CommunicationException;
	
	JsonPrimitiveParser getParser();
	JsonMapper getMapper();
	
	void close();
}