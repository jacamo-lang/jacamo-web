package jacamo.web;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;

import javax.ws.rs.core.UriBuilder;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;

import jacamo.platform.DefaultPlatformImpl;
import jason.infra.centralised.BaseCentralisedMAS;


public class JCMRest extends DefaultPlatformImpl {

    public static String JaCaMoZKAgNodeId = "/jacamo/agents";
    public static String JaCaMoZKDFNodeId = "/jacamo/df";
    
    HttpServer restHttpServer = null;

    static URI restServerURI = null;
    
    ServerCnxnFactory zkFactory = null;
    static String zkHost = null;
    static CuratorFramework zkClient;
    
    static public String getRestHost() {
        return restServerURI.toString();
    }
    
    static public String getZKHost() {
        return zkHost;
    }
    
    static {
        confLog4j();
    }
    
    @Override
    public void init(String[] args) throws Exception {
        
        // adds RestAgArch in the configuration of all agents in the project
        /*List<AgentParameters> lags = new ArrayList<>();
        for (AgentParameters ap: project.getAgents()) {
            if (ap.getNbInstances() > 0) {
                lags.add(ap);
                ap.insertArchClass(new ClassParameters(RestAgArch.class.getName()));
            }
        }*/
        
        // adds RestAgArch as default ag arch when using this platform
        BaseCentralisedMAS.getRunner().getRuntimeServices().registerDefaultAgArch(RestAgArch.class.getName());
        
        int restPort = 3280;
        int zkPort   = 2181;
        boolean useZK = false;
        
        if (args.length > 0) {
            String la = "";
            for (String a: args[0].split(" ")) {
                if (la.equals("--restPort"))
                    try {
                        restPort = Integer.parseInt(a);
                    } catch (Exception e) {
                        System.err.println("The argument for restPort is not a number.");
                    }

                if (a.equals("--main")) {
                    useZK = true;
                }
                if (la.equals("--main"))
                    try {
                        zkPort = Integer.parseInt(a);
                    } catch (Exception e) {
                        System.err.println("The argument for restPort is not a number.");
                    }

                if (la.equals("--connect")) {
                    zkHost = a;
                    useZK = true;
                }
                la = a;
            }           
        }
        
        restHttpServer = startRestServer(restPort);
        
        if (useZK) {
            if (zkHost == null) {
                zkFactory  = startZookeeper(zkPort);
                System.out.println("Platform (zookeeper) started on "+zkHost);
            } else {
                System.out.println("Platform (zookeeper) running on "+zkHost);
            }
        }
        
    }
    
    @Override
    public void stop() {
        if (restHttpServer != null)
            try {
                restHttpServer.shutdown();
            } catch (Exception e) {}
        restHttpServer = null;

        if (zkFactory != null)
            try {
                zkFactory.shutdown();
            } catch (Exception e) {}
        zkFactory = null;

        if (zkClient != null)
            zkClient.close();
    }
    
    static void confLog4j() {
        try {
            ConsoleAppender console = new ConsoleAppender(); //create appender
            //configure the appender
            String PATTERN = "%d [%p|%c|%C{1}] %m%n";
            console.setLayout(new PatternLayout(PATTERN)); 
            console.setThreshold(Level.WARN);
            console.activateOptions();
            //add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(console);

            FileAppender fa = new FileAppender();
            fa.setName("FileLogger");
            fa.setFile("log/zk.log");
            fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
            fa.setThreshold(Level.WARN);
            fa.setAppend(true);
            fa.activateOptions();
            
            //add appender to any Logger (here is root)
            Logger.getRootLogger().addAppender(fa);
            //repeat with all other desired appenders
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HttpServer startRestServer(int port) {
        //ResourceConfig config = new RestAppConfig(); //RestAgArch.class);
        //config.registerInstances(new RestImpl());
        //config.addProperties(new HashMap<String,Object>() {{ put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature"); }} );
        try {
            restServerURI = UriBuilder.fromUri("http://"+InetAddress.getLocalHost().getHostAddress()+"/").port(port).build();
            
            // gzip compression configuration
            RestAppConfig rc = new RestAppConfig();
            rc.registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);
            
            // get a server from factory
            HttpServer s = GrizzlyHttpServerFactory.createHttpServer(restServerURI, rc);
            
            // other possiblecontainers:
            //JettyHttpContainerFactory.createServer(baseUri, config);
            //JdkHttpServerFactory.createHttpServer(baseUri, config);
            System.out.println("JaCaMo Rest API is running on "+restServerURI);
            return s;
        } catch (javax.ws.rs.ProcessingException e) {  
            System.out.println("trying next port for web server "+(port+1)+". e="+e);
            return startRestServer(port+1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ServerCnxnFactory startZookeeper(int port) {
        int numConnections = 500;
        int tickTime = 2000;

        try {
            zkHost = InetAddress.getLocalHost().getHostAddress()+":"+port;

            File dir = Files.createTempDirectory("zookeeper").toFile(); 
            ZooKeeperServer server = new ZooKeeperServer(dir, dir, tickTime);
            server.setMaxSessionTimeout(4000);
            
            ServerCnxnFactory factory = new NIOServerCnxnFactory();
            factory.configure(new InetSocketAddress(port), numConnections);
            factory.startup(server); // start the server.   

            // create main nodes
            //client.delete().deletingChildrenIfNeeded().forPath("/jacamo");
            //client.create().forPath("/jacamo");
            getZKClient().create().creatingParentsIfNeeded().forPath(JaCaMoZKAgNodeId);
            getZKClient().create().creatingParentsIfNeeded().forPath(JaCaMoZKDFNodeId);
            //client.close();
            return factory;
        } catch (java.net.BindException e) {
            System.err.println("Cannot start zookeeper, port "+port+" already used!");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static CuratorFramework getZKClient() {
        if (zkClient == null) {
            zkClient = CuratorFrameworkFactory.newClient(getZKHost(), new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
        }
        return zkClient;
    }
}
