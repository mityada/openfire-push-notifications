package com.github.mityada.openfire.push;

import org.dom4j.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.IQHandlerInfo;

import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

public class IQPushNotificationsHandler extends IQHandler {
    private static final Logger LOG = LoggerFactory.getLogger(IQPushNotificationsHandler.class);

    private IQHandlerInfo info;

    private PushNotificationsManager manager;

    public IQPushNotificationsHandler() {
        super("Push Notifications Handler");
        info = new IQHandlerInfo("enable", "urn:xmpp:push:0");

        manager = PushNotificationsManager.getInstance();
    }

    @Override
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        LOG.debug("handleIQ " + packet);

        if (packet.getType().equals(IQ.Type.set)) {
            Element childElement = packet.getChildElement();
            String elementName = childElement.getName();

            String jid = childElement.attributeValue("jid");
            if (jid == null) {
                IQ reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.not_acceptable);
                return reply;
            }

            JID pushJID = new JID(null, jid, null, true);

            String node = childElement.attributeValue("node");
            if (node == null && "enable".equals(elementName)) {
                IQ reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.feature_not_implemented);
                return reply;
            }

            if ("enable".equals(elementName)) {
                manager.enable(packet.getFrom(), pushJID, node, null);

                IQ reply = IQ.createResultIQ(packet);
                return reply;
            } else if ("disable".equals(elementName)) {
                manager.disable(packet.getFrom(), pushJID, node);

                IQ reply = IQ.createResultIQ(packet);
                return reply;
            } else {
                IQ reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.not_acceptable);
                return reply;
            }
        } else {
            IQ reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.not_acceptable);
            return reply;
        }
    }

    @Override
    public IQHandlerInfo getInfo() {
        return info;
    }
}
