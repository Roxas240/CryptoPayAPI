package com.xemplar.libs.cryptorpc.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.xemplar.libs.cryptorpc.common.Defaults;
import com.xemplar.libs.cryptorpc.jsonrpc.deserialization.AddressOverviewDeserializer;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AddressOverviewDeserializer.class)
public class AddressOverview extends com.neemre.btcdcli4j.core.domain.Entity {

	private String address;
	private BigDecimal balance;
	private String account;


	public AddressOverview(String address, BigDecimal balance, String account) {
		setAddress(address);
		setBalance(balance);
		setAccount(account);
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance.setScale(Defaults.DECIMAL_SCALE, Defaults.ROUNDING_MODE);
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}