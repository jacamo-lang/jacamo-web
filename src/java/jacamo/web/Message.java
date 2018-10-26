package jacamo.web;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Adaptor for Jason Message and Rest Message
 *
 */
@XmlRootElement(name = "message")
public class Message {
	public String performative  = null;
	public String sender   = null;
	public String receiver = null;
	public String content = null;
	public String msgId    = null;
	public String inReplyTo = null;

	public Message() {}
    public Message(String id, String p, String s, String r, String c) {
        msgId = id;
        performative = p;
        sender = s;
        receiver = r;
        content = c;
    }
    
	public Message(jason.asSemantics.Message m) {
		this.performative = m.getIlForce();
		this.sender = m.getSender();
		this.receiver = m.getReceiver();
		this.content = m.getPropCont().toString();
		this.msgId = m.getMsgId();
		this.inReplyTo = m.getInReplyTo();
	}

	jason.asSemantics.Message getAsJasonMsg() {
		jason.asSemantics.Message jm = new jason.asSemantics.Message(performative, sender, receiver, content, msgId);
		jm.setInReplyTo(inReplyTo);
		return jm;
	}

	public String toString() {
        String irt = (inReplyTo == null ? "" : "->"+inReplyTo);
        return "<"+msgId+irt+","+sender+","+performative+","+receiver+","+content+">";
    }
}
