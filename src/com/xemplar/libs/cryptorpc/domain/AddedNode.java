package com.xemplar.libs.cryptorpc.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddedNode extends Entity {

	@JsonProperty("addednode")
	private String addedNode;
	private Boolean connected;
	private List<PeerNodeOverview> addresses;
}