package com.github.mityada.openfire.push;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.util.TaskEngine;

public class PushNotificationsPlugin implements Plugin {
	private static final Logger LOG = LoggerFactory.getLogger(PushNotificationsPlugin.class);

    public static final String NAMESPACE = "urn:xmpp:push:0";

    private IQPushNotificationsHandler iqHandler;
    private PushNotificationsPacketInterceptor interceptor;
    private SessionKeepAliveTask keepAliveTask;

    public PushNotificationsPlugin() {
        iqHandler = new IQPushNotificationsHandler();
        interceptor = new PushNotificationsPacketInterceptor();
        keepAliveTask = new SessionKeepAliveTask();
    }

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        XMPPServer.getInstance().getIQRouter().addHandler(iqHandler);
        XMPPServer.getInstance().getIQDiscoInfoHandler().addServerFeature(NAMESPACE);

        InterceptorManager.getInstance().addInterceptor(interceptor);

        TaskEngine.getInstance().scheduleAtFixedRate(keepAliveTask, 30 * 1000, 30 * 1000);

        LOG.debug("PushNotificationsPlugin initialized");
    }

    @Override
    public void destroyPlugin() {
        XMPPServer.getInstance().getIQDiscoInfoHandler().removeServerFeature(NAMESPACE);
        XMPPServer.getInstance().getIQRouter().removeHandler(iqHandler);

        InterceptorManager.getInstance().removeInterceptor(interceptor);

        TaskEngine.getInstance().cancelScheduledTask(keepAliveTask);

        LOG.debug("PushNotificationsPlugin destroyed");
    }
}
