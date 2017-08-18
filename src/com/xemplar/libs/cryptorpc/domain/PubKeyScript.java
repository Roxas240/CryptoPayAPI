package com.xemplar.libs.cryptorpc.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xemplar.libs.cryptorpc.domain.enums.ScriptTypes;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubKeyScript extends SignatureScript {
	private Integer reqSigs;
	private ScriptTypes type;
	private List<String> addresses;

	public PubKeyScript(){}
	public PubKeyScript(int reqSigs, ScriptTypes type, List<String> addresses){
		this.reqSigs = reqSigs;
		this.type = type;
		this.addresses = addresses;
	}
}