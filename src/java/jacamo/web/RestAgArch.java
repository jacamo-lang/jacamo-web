package jacamo.web;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.WatchMode;
import org.apache.zookeeper.CreateMode;

import jason.ReceiverNotFoundException;
import jason.architecture.AgArch;
import jason.asSemantics.Agent;
import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.UnnamedVar;
import jason.infra.centralised.BaseCentralisedMAS;
import jason.infra.centralised.CentralisedRuntimeServices;
import jason.mas2j.ClassParameters;
import jason.runtime.RuntimeServices;
import jason.runtime.Settings;

public class RestAgArch extends AgArch {
    
    CuratorFramework      zkClient = null;
    AsyncCuratorFramework zkAsync = null;
    
    Client                restClient = null;
    
    
    @Override
    public void init() throws Exception {
        //System.out.println("my ag arch init "+getAgName());
        restClient = ClientBuilder.newClient();

        if (JCMRest.getZKHost() != null) {
            zkClient = CuratorFrameworkFactory.newClient(JCMRest.getZKHost(), new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            
            // register the agent in ZK
            if (zkClient.checkExists().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+getAgName()) != null) {
                System.err.println("Agent "+getAgName()+" is already registered in zookeeper!");
                
            } else {
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(JCMRest.JaCaMoZKAgNodeId+"/"+getAgName(), (JCMRest.getRestHost()+"agents/"+getAgName()).getBytes());
                zkAsync  = AsyncCuratorFramework.wrap(zkClient);
            }
        }
    }
    
    @Override
    public void stop() {
        if (zkClient != null)
            zkClient.close();
        if (restClient != null)
            restClient.close();
    }

    RuntimeServices singRTS = null;

    // place DF services based on ZK
    @Override
    public RuntimeServices getRuntimeServices() {
        if (singRTS == null) {
            if (JCMRest.getZKHost() != null) {
                singRTS = new CentralisedRuntimeServices(BaseCentralisedMAS.getRunner()) {
                    @Override
                    public void dfRegister(String agName, String service, String type) {
                        RestAgArch.this.dfRegister(agName, service, type);
                    }               
                    @Override
                    public void dfDeRegister(String agName, String service, String type) {
                        RestAgArch.this.dfDeRegister(agName, service, type);
                    }               
                    @Override
                    public Collection<String> dfSearch(String service, String type) {
                        return RestAgArch.this.dfSearch(service, type);
                    }                               
                    @Override
                    public void dfSubscribe(String agName, String service, String type) {
                        RestAgArch.this.dfSubscribe(agName, service, type);
                    }
                    @Override
                    public String createAgent(String agName, String agSource, String agClass, List<String> archClasses, ClassParameters bbPars, Settings stts, Agent father) throws Exception {
                        // delegate agent creation to RTS defined in JaCaMo Launcher
                        return masRunner.getRuntimeServices().createAgent(agName, agSource, agClass, archClasses, bbPars, stts, father);
                    }
                };
            } else {
                singRTS = super.getRuntimeServices();
            }
        }
        return singRTS;
    }
    
    @Override
    public void sendMsg(Message m) throws Exception {
        try {
            super.sendMsg(m);
            return;
        } catch (ReceiverNotFoundException e) {
            String adr = null;

            if (m.getReceiver().startsWith("http://")) {
                adr = m.getReceiver();
            } else {
                // try ZK
                byte[] badr = zkClient.getData().forPath(JCMRest.JaCaMoZKAgNodeId+"/"+m.getReceiver());
                if (badr != null)
                    adr = new String(badr);
            }
            
            // try by rest to send the message by REST API
            if (adr != null) {
                // do POST
                String r = null;
                if (adr.startsWith("http")) {
                    r = restClient
                              .target(adr)
                              .path("mb")
                              .request(MediaType.APPLICATION_XML)
                              .accept(MediaType.TEXT_PLAIN)
                              .post(Entity.xml( new jacamo.web.Message(m)), String.class);
                }
                if (!"ok".equals(r)) {
                    throw e;
                }
            } else {
                throw e;
            }
        }        
    }
    
    public void dfRegister(String agName, String service, String type) {
        if (type == null) type = "no-type";
        try {
            String node = JCMRest.JaCaMoZKDFNodeId+"/"+service+"/"+getAgName();
            if (zkClient.checkExists().forPath(node) == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(node, type.getBytes());
            } else {
                zkClient.setData().forPath(node, type.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void dfDeRegister(String agName, String service, String type) {
        try {
            zkClient.delete().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service+"/"+getAgName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Collection<String> dfSearch(String service, String type) {
        Set<String> ags = new HashSet<>();
        try {
            if (zkClient.checkExists().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service) != null) { 
                for (String r : zkClient.getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service)) {
                    ags.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ags;
    }
    
    public void dfSubscribe(String agName, String service, String type) {
        try {
            zkAsync.with(WatchMode.successOnly).watched().getChildren().forPath(JCMRest.JaCaMoZKDFNodeId+"/"+service).event().thenAccept(event -> {
                try {
                    //System.out.println("something changed...."+event.getType()+"/"+event.getState());                 
                    // stupid implementation: send them all again and
                    dfSubscribe(agName, service, type); // keep watching
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            // update providers
            Term s = new Atom("df");
            Literal l = ASSyntax.createLiteral("provider", new UnnamedVar(), new StringTermImpl(service));
            l.addSource(s);
            getTS().getAg().abolish(l, new Unifier());
            for (String a: dfSearch(service, type)) {
                l = ASSyntax.createLiteral("provider", new Atom(a), new StringTermImpl(service));
                l.addSource(s);
                getTS().getAg().addBel(l);
            }
                        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
