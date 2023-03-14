package jacamo.web;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.curator.framework.CuratorFramework;

import com.google.gson.Gson;

import jacamo.rest.JCMRest;
import jacamo.rest.config.RestAgArch;
import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.runtime.RuntimeServicesFactory;
//import jason.infra.centralised.BaseCentralisedMAS;

public class WebAgArch extends RestAgArch {

    private static final long serialVersionUID = 1L;
    
    CuratorFramework      zkClient = null;
    Client                restClient = null;
    
    private Queue<Message>     mboxweb    = new ConcurrentLinkedQueue<>();

    @SuppressWarnings("unchecked")
    @Override
    public void sendMsg(Message m) throws Exception {
        super.sendMsg(m);
        //Todo: Fix BaseCentralisedMAS            
        /*
        try {
            super.sendMsg(m);
            AgArch a = BaseCentralisedMAS.getRunner().getAg(m.getReceiver()).getFirstAgArch();
            while (a != null) {
                if (a.getClass().equals(WebAgArch.class)) {
                    ((WebAgArch)a).receiveMsg(m);
                    break;
                }
                a = BaseCentralisedMAS.getRunner().getAg(m.getReceiver()).getNextAgArch();
            }
            return;
        } catch (ReceiverNotFoundException e) {
            try {
                String adr = null;

                if (m.getReceiver().startsWith("http")) {
                    adr = m.getReceiver();
                } else if (zkClient != null) {
                    try {
                        // try ZK inbox meta data
                        byte[] lmd = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver()+"/"+JCMRest.JaCaMoZKMDNodeId);
                        Map<String,String> md = new Gson().fromJson(new String(lmd), Map.class);
                        adr = md.get("inbox");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    if (adr == null) {
                        // try ZK agent data
                        byte[] badr = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver());
                        if (badr != null)
                            adr = new String(badr);
                    }
                }

                // try to send the message by REST API
                if (adr != null) {
                    // do POST
                    if (adr.startsWith("http")) {
                        restClient
                                  .target(adr)
                                  .request(MediaType.APPLICATION_XML)
                                  .accept(MediaType.TEXT_PLAIN)
                                  .post(
                                        //Entity.xml( new jacamo.rest.Message(m)), String.class);
                                        Entity.json( new Gson().toJson(new jacamo.rest.util.Message(m))));
                    }
                } else {
                    throw e;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw e;
            }
        }
*/            
    }
    
    public void receiveMsg(Message m) {
        mboxweb.offer(m);
    }
    
    @Override
    public void checkMail() {
        super.checkMail();

        Message im = mboxweb.poll();
        while (im != null) {
            Websockets.broadcast(".send" + im);
            im = mboxweb.poll();
        }
    }
    
    @Override
    public void broadcast(jason.asSemantics.Message m) throws Exception {
        for (String agName: RuntimeServicesFactory.get().getAgentsNames()) {
            if (!agName.equals(this.getAgName())) {
                jason.asSemantics.Message newm = m.clone();
                newm.setReceiver(agName);
                getFirstAgArch().sendMsg(newm);
            }
        }
    }
}
