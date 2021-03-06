package com.github.mityada.openfire.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;

import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.Packet;

public class PushNotificationsPacketInterceptor implements PacketInterceptor {
	private static final Logger LOG = LoggerFactory.getLogger(PushNotificationsPacketInterceptor.class);

    private PushNotificationsManager manager;

    public PushNotificationsPacketInterceptor() {
        manager = PushNotificationsManager.getInstance();
    }

    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) {
        if (incoming || !processed)
            return;

        if (!(session instanceof LocalClientSession))
            return;

        LocalClientSession localClientSession = (LocalClientSession) session;
        if (!localClientSession.isDetached())
            return;

        LOG.debug("Packet for detached client session: " + packet);

        DataForm summary = new DataForm(DataForm.Type.submit);

        FormField formType = summary.addField();
        formType.setVariable("FORM_TYPE");
        formType.addValue("urn:xmpp:push:summary");

        FormField stanza = summary.addField();
        stanza.setVariable("stanza");
        stanza.addValue(packet.toXML());

        manager.notify(localClientSession.getAddress(), summary);
    }
}
