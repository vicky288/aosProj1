import java.io.Serializable;
import java.util.TreeMap;


public class MessageFormat implements Serializable{
	private String messageId;
	private String sourceServer;
	private String destServer;
	private String sourceId;
	private String destId;
	private int sourcePort;
	private int destPort;
	private boolean isAcknowledged = false;
	TreeMap<String, String> nodesKnowledge;
	
	
	public String getSourceServer() {
		return sourceServer;
	}
	public void setSourceServer(String sourceServer) {
		this.sourceServer = sourceServer;
	}
	public String getDestServer() {
		return destServer;
	}
	public void setDestServer(String destServer) {
		this.destServer = destServer;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getDestId() {
		return destId;
	}
	public void setDestId(String destId) {
		this.destId = destId;
	}
	public int getDestPort() {
		return destPort;
	}
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public int getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	public boolean isAcknowledged() {
		return isAcknowledged;
	}
	public void setAcknowledged(boolean isAcknowledged) {
		this.isAcknowledged = isAcknowledged;
	}
	public TreeMap<String, String> getNodesKnowledge() {
		return nodesKnowledge;
	}
	public void setNodesKnowledge(TreeMap<String, String> nodesKnowledge) {
		this.nodesKnowledge = nodesKnowledge;
	}

	
}
