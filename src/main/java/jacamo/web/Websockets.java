package jacamo.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Simple websockets implementation
 * By default it is opening http://ip:8026/ws
 * 
 * After launching the server try:
 * $ curl -i -N -H "Upgrade: websocket" http://127.0.0.1:8026/ws/messages
 * 
 * @author cleber 
 */
@ServerEndpoint(value = "/messages")
public class Websockets {

    private final static Set<Session> sessions = new HashSet<>();

    @OnClose
    public void onClose(Session session) {
        System.out.println("{" + session.getId() + "} Session has been closed.");
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.out.println("{" + session.getId() + "} An error has been detected: " + t.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("{" + session.getId() + "} message received: {" + message + "}");
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("{" + session.getId() + "} Session opened.");
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        try {
            if (sessions.size() > 0) {
                for (Session session: sessions) {
                    System.out.println("{" + session.getId() + "} Sending message: " + message);
                    session.getBasicRemote().sendText(message);
                }
            } else {
                System.out.println("There is no active websocket session to send the message: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
