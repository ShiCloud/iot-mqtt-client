package com.github.shicloud.mqtt.client.config;

public class ClientConfig extends AbstractConfig{
	
	String url;
	String username;
	String password;
	Short keepAlive;
	Boolean retained;
	Integer reconnectAttemptsMax;
	Integer reconnectDelay;
	String logBackXmlPath;
	
	
	public ClientConfig() {
		super(null);
	}
	
	public ClientConfig(String configPath) {
		super(configPath);
		url = getProp("url");
		username = getProp("username");
		password = getProp("password");
		keepAlive = Short.valueOf(getProp("keepAlive"));
		retained = Boolean.valueOf(getProp("retained"));
		reconnectAttemptsMax = Integer.valueOf(getProp("reconnectAttemptsMax"));
		reconnectDelay = Integer.valueOf(getProp("reconnectDelay"));
		logBackXmlPath = getProp("logBackXmlPath");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Short getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Short keepAlive) {
		this.keepAlive = keepAlive;
	}

	public Boolean getRetained() {
		return retained;
	}

	public void setRetained(Boolean retained) {
		this.retained = retained;
	}

	public Integer getReconnectAttemptsMax() {
		return reconnectAttemptsMax;
	}

	public void setReconnectAttemptsMax(Integer reconnectAttemptsMax) {
		this.reconnectAttemptsMax = reconnectAttemptsMax;
	}

	public Integer getReconnectDelay() {
		return reconnectDelay;
	}

	public void setReconnectDelay(Integer reconnectDelay) {
		this.reconnectDelay = reconnectDelay;
	}

	public String getLogBackXmlPath() {
		return logBackXmlPath;
	}

	public void setLogBackXmlPath(String logBackXmlPath) {
		this.logBackXmlPath = logBackXmlPath;
	}

}
