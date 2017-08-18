package com.xemplar.libs.cryptorpc.client;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.neemre.btcdcli4j.core.NodeProperties;
import com.xemplar.libs.cryptorpc.common.AgentConfigurator;
import com.xemplar.libs.cryptorpc.common.Defaults;
import com.xemplar.libs.cryptorpc.common.Errors;
import com.xemplar.libs.cryptorpc.domain.Block;
import com.xemplar.libs.cryptorpc.util.CollectionUtils;
import com.xemplar.libs.cryptorpc.util.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConfigurator extends AgentConfigurator {
	private static final Logger LOG = LoggerFactory.getLogger(ClientConfigurator.class);
	private String nodeVersion;
	
	@Override
	public Set<NodeProperties> getRequiredProperties() {
		return EnumSet.of(NodeProperties.RPC_PROTOCOL, NodeProperties.RPC_HOST, 
				NodeProperties.RPC_PORT, NodeProperties.RPC_USER, NodeProperties.RPC_PASSWORD, 
				NodeProperties.HTTP_AUTH_SCHEME);
	}
	
	public CloseableHttpClient checkHttpProvider(CloseableHttpClient httpProvider) {
		if (httpProvider == null) {
			LOG.warn("-- checkHttpProvider(..): no preconfigured HTTP provider detected; reverting "
					+ "to library default settings");
			httpProvider = getDefaultHttpProvider();
		}
		return httpProvider;
	}

	public boolean checkNodeHealth(Block bestBlock) {
		long currentTime = System.currentTimeMillis() / 1000;
		if ((currentTime - bestBlock.getTime()) > TimeUnit.HOURS.toSeconds(6)) {
			LOG.warn("-- checkNodeHealth(..): last available block was mined >{} hours ago; please "
					+ "check your network connection", ((currentTime - bestBlock.getTime()) / 3600));
			return false;
		}
		return true;
	}

	private CloseableHttpClient getDefaultHttpProvider() {
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		CloseableHttpClient httpProvider = HttpClients.custom().setConnectionManager(connManager)
				.build();
		return httpProvider;
	}

	public String getNodeVersion(){
		return nodeVersion;
	}
}