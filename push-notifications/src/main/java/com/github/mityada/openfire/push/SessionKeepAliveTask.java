package com.github.mityada.openfire.push;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;

public class SessionKeepAliveTask extends TimerTask {
	private static final Logger LOG = LoggerFactory.getLogger(SessionKeepAliveTask.class);

    private PushNotificationsManager manager;

    public SessionKeepAliveTask() {
        manager = PushNotificationsManager.getInstance();
    }

    @Override
    public void run() {
        int detachTime = SessionManager.getInstance().getSessionDetachTime();
        if (detachTime == -1)
            return;

        long notifyTime = System.currentTimeMillis() - detachTime + 60 * 1000;

        for (ClientSession clientSession : SessionManager.getInstance().getSessions()) {
            if (!(clientSession instanceof LocalClientSession))
                continue;

            LocalClientSession localClientSession = (LocalClientSession) clientSession;
            if (!localClientSession.isDetached())
                continue;

            if (localClientSession.getLastActiveDate().getTime() < notifyTime) {
                LOG.debug("Sending notification to " + localClientSession.getAddress());
                manager.notify(localClientSession.getAddress(), null);
            }
        }
    }
}
