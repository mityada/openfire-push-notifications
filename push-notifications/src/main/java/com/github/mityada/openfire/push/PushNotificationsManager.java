package com.github.mityada.openfire.push;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;

import org.xmpp.component.IQResultListener;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketExtension;

public class PushNotificationsManager implements IQResultListener {
    private static final Logger LOG = LoggerFactory.getLogger(PushNotificationsManager.class);

    private static volatile PushNotificationsManager INSTANCE = null;

    private final Map<JID, Map<JID, Map<String, DataForm>>> services;

    public static PushNotificationsManager getInstance() {
        if (INSTANCE == null) {
            synchronized(PushNotificationsManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PushNotificationsManager();
                }
            }
        }
        return INSTANCE;
    }

    public PushNotificationsManager() {
        services = new HashMap<>();
    }

    public void enable(JID jid, JID pushJID, String node, DataForm publishOptions) {
        if (!services.containsKey(jid)) {
            services.put(jid, new HashMap<>());
        }

        Map<JID, Map<String, DataForm>> jidServices = services.get(jid);
        if (!jidServices.containsKey(pushJID)) {
            jidServices.put(pushJID, new HashMap<>());
        }

        Map<String, DataForm> nodes = jidServices.get(pushJID);
        nodes.put(node, publishOptions);
        LOG.debug("Enabled push notifications for jid " + jid + " service " + pushJID + " node " + node);
    }

    public boolean disable(JID jid, JID pushJID, String node) {
        if (!services.containsKey(jid)) {
            return false;
        }

        Map<JID, Map<String, DataForm>> jidServices = services.get(jid);
        if (!jidServices.containsKey(pushJID)) {
            return false;
        }

        if (node == null) {
            jidServices.remove(pushJID);
            LOG.debug("Disabled push notifications for jid " + jid + " service " + pushJID);
            return true;
        }

        Map<String, DataForm> nodes = jidServices.get(pushJID);
        if (!nodes.containsKey(node)) {
            return false;
        }

        nodes.remove(node);
        LOG.debug("Disabled push notifications for jid " + jid + " service " + pushJID + " node " + node);
        return true;
    }

    public void notify(JID jid, DataForm summary) {
        Map<JID, Map<String, DataForm>> jidServices = services.get(jid);
        if (jidServices == null)
            return;

        for (Map.Entry<JID, Map<String, DataForm>> jidService : jidServices.entrySet()) {
            for (Map.Entry<String, DataForm> node : jidService.getValue().entrySet()) {
                LOG.debug("Sending push notification for jid " + jid + " to service " + jidService.getKey() + " node " + node.getKey());

                IQ iq = new IQ(IQ.Type.set);
                iq.setTo(jidService.getKey());
                iq.setFrom(jid.toBareJID());
                Element notification = iq.setChildElement("pubsub", "http://jabber.org/protocol/pubsub")
                                         .addElement("publish").addAttribute("node", node.getKey())
                                         .addElement("item")
                                         .addElement("notification", "urn:xmpp:push:0");

                if (summary != null) {
                    notification.add(summary.getElement());
                }

                IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
                iqRouter.addIQResultListener(iq.getID(), this);
                iqRouter.route(iq);
            }
        }
    }

    @Override
    public void answerTimeout(String packetId) {
        LOG.debug("Answer timeout for " + packetId);
    }

    @Override
    public void receivedAnswer(IQ packet) {
        LOG.debug("Received answer " + packet);
    }
};
