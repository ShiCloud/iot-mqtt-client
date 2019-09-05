package com.github.shicloud.mqtt.client.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class AbstractConfig {
	
	private Properties p = new Properties();
	
	AbstractConfig(String configPath) {
		if(configPath == null) {
			return;//do nothing
		}
        InputStream is = null;
        try {
            is = new BufferedInputStream (new FileInputStream(configPath));;
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
			configurator.doConfigure(getProp("logBackXmlPath"));
		} catch (JoranException e) {
			e.printStackTrace();
		}
    }
	
	public String getProp(String key) {
        return p.getProperty(key);
    }
}
