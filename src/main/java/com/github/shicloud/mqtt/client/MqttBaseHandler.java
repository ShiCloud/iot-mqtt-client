package com.github.shicloud.mqtt.client;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Date;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.FutureConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.Promise;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.fusesource.mqtt.client.Tracer;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shicloud.mqtt.client.config.ClientConfig;

public abstract class MqttBaseHandler {

	private static Logger logger = LoggerFactory.getLogger(MqttBaseHandler.class);

	private ClientConfig properties;

	private String className = this.getClass().getName();

	public static String getLocalHostIp() {
		String hostIp = null;
		// 根据网卡取本机配置的IP
		InetAddress inet = null;
		try {
			inet = InetAddress.getLocalHost();

			hostIp = inet.getHostName() + "-" + inet.getHostAddress();
		} catch (UnknownHostException e) {
			logger.debug("getHostIp error", e);
		}
		return hostIp;
	}

	public abstract void processInput(UTF8Buffer topic, Buffer payload) ;


	private CallbackConnection callbackConnection;

	private FutureConnection connection;

	public void init(ClientConfig properties, Topic[] topics,String clientId,boolean cleanSession) {
		String[] split = className.split("\\.");
		this.className = split[split.length-1];
		this.properties = properties;

		if (clientId == null || clientId.equals("")) {
			clientId = String.valueOf(new Date().getTime());
		}

		MyMqtt mqtt = new MyMqtt();

		mqtt.setClientId(clientId);
		try {
			mqtt.setHost(this.properties.getUrl());
		} catch (URISyntaxException ee) {
			logger.debug("init setHost failure ", ee);
		}
		mqtt.setUserName(this.properties.getUsername());
		mqtt.setPassword(this.properties.getPassword());
		mqtt.setCleanSession(cleanSession);
		mqtt.setReconnectAttemptsMax(this.properties.getReconnectAttemptsMax());
		mqtt.setReconnectDelay(this.properties.getReconnectDelay());
		mqtt.setKeepAlive(this.properties.getKeepAlive());
		mqtt.setReconnectDelay(this.properties.getReconnectDelay());

		mqtt.setTracer(new Tracer() {
			@Override
			public void onReceive(MQTTFrame frame) {
				logger.debug(className + " Tracer recv: " + frame);
			}

			@Override
			public void onSend(MQTTFrame frame) {
				logger.debug(className + " Tracer send: " + frame);
			}

			@Override
			public void debug(String message, Object... args) {
				logger.debug(String.format(className + " Tracer debug: " + message, args));
			}
		});

		callbackConnection = mqtt.callbackConnection();

		connection = mqtt.futureConnection(callbackConnection);

		Future<Void> connectFuture = connection.connect();
		connectFuture.then(new Callback<Void>() {
			@Override
			public void onSuccess(Void value) {
				logger.debug("connect success");
			}

			@Override
			public void onFailure(Throwable value) {
				logger.debug("connect failure " + value);
			}
		});
		try {
			connectFuture.await();
		} catch (Exception e) {
			logger.error("connect error::" + e);
		}
		// 处理主题
		subscribe(topics);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				Future<Void> disconnectFuture = connection.disconnect();
				disconnectFuture.then(new Callback<Void>() {
					@Override
					public void onSuccess(Void value) {
						logger.debug("shutdownHook success");
					}

					@Override
					public void onFailure(Throwable value) {
						logger.debug("shutdownHook failure " + value);
					}
				});
				try {
					disconnectFuture.await();
				} catch (Exception e) {
					logger.debug("disconnect failure ", e);
				}
			}
		}));
	}

	public void send(String topic, byte[] msg, QoS qos, boolean retained) {
		Future<Void> publishFuture = this.connection.publish(topic, msg, qos, retained);
		publishFuture.then(new Callback<Void>() {
			@Override
			public void onSuccess(Void value) {
				logger.debug("send " + topic + " msg: " + new String(msg));
			}

			@Override
			public void onFailure(Throwable value) {
				logger.debug("send " + topic + " msg failure " + value);
			}
		});
		try {
			publishFuture.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void subscribe(Topic[] topics) {
		if (topics == null || topics.length < 1) {
			return;
		}

		Future<byte[]> subscribeFuture = this.connection.subscribe(topics);

		subscribeFuture.then(new Callback<byte[]>() {
			@Override
			public void onSuccess(byte[] values) {
				logger.debug(className + " subscribe success");
			}

			@Override
			public void onFailure(Throwable value) {
				logger.debug(className + " subscribe failure " + value);
			}
		});
		try {
			subscribeFuture.await();
		} catch (Exception e) {
			logger.error(className + " subscribe error::" + e);
		}

		final Promise<Buffer> result = new Promise<Buffer>();

		MqttBaseHandler handler = this;
		
		callbackConnection.listener(new Listener() {
			public void onConnected() {
				logger.debug(className + " listener onConnected");
			}

			public void onDisconnected() {
				logger.debug(className + " listener onDisconnected");
			}

			public void onPublish(UTF8Buffer topic, Buffer payload, Runnable onComplete) {
				logger.debug(className + " listener onPublish " + payload.hex());
				handler.processInput(topic, payload);
				result.onSuccess(payload);
				onComplete.run();
			}

			public void onFailure(Throwable value) {
				logger.debug(className + " listener onFailure: " + value);
				result.onFailure(value);
			}
		});
	}
}
