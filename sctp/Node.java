import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.security.SecureRandom;

public class Node {

	String nodeId;
	String server;
	int port;
	TreeMap<String, String> initialNodesInfo;
	TreeMap<String, String> nodesKnowledge;
	TreeSet<String> exploredNodes = new TreeSet<String>();
	ArrayList<TreeMap<String, String>> meggaseQueue ;	
	boolean stopProcessing = false;
	//not used
	ArrayList<String> exploredNodeList = new ArrayList<String>();

	

	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public TreeSet<String> getExploredNodes() {
		return exploredNodes;
	}
	public void setExploredNodes(TreeSet<String> exploredNodes) {
		this.exploredNodes = exploredNodes;
	}
	public boolean isStopProcessing() {
		return stopProcessing;
	}
	public void setStopProcessing(boolean stopProcessing) {
		this.stopProcessing = stopProcessing;
	}
	public ArrayList<String> getExploredNodeList() {
		return exploredNodeList;
	}
	public void setExploredNodeList(ArrayList<String> exploredNodeList) {
		this.exploredNodeList = exploredNodeList;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public TreeMap<String, String> getInitialNodesInfo() {
		return initialNodesInfo;
	}
	public void setInitialNodesInfo(TreeMap<String, String> initialNodesInfo) {
		this.initialNodesInfo = initialNodesInfo;
	}
	public TreeMap<String, String> getNodesKnowledge() {
		return nodesKnowledge;
	}
	public void setNodesKnowledge(TreeMap<String, String> nodesKnowledge) {
		this.nodesKnowledge = nodesKnowledge;
	}
	public ArrayList<TreeMap<String, String>> getMeggaseQueue() {
		return meggaseQueue;
	}
	public void setMeggaseQueue(ArrayList<TreeMap<String, String>> meggaseQueue) {
		this.meggaseQueue = meggaseQueue;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public Node(String nodeId, int port) {
		this.nodeId = nodeId;
		this.port = port;
		this.initialNodesInfo = new TreeMap<String, String>();
		this.nodesKnowledge = new TreeMap<String, String>();
		this.meggaseQueue = new ArrayList<TreeMap<String,String>>();
	}

	public Node(String nodeId, int port, String server) {
		this.nodeId = nodeId;
		this.port = port;
		this.server = server;
		this.initialNodesInfo = new TreeMap<String, String>();
		this.nodesKnowledge = new TreeMap<String, String>();
		this.meggaseQueue = new ArrayList<TreeMap<String,String>>();
	}
	public void nodeInitialize() {
		LogToFile.CreateWriteFile(this.nodeId, "Start");

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("config.txt"));
			String line = "";
			int nodeVicinityEntry = 0;
			int nodeInfoEntry = 0;
			while((line=br.readLine())!=null)
			{
				if(line.equals("--Start of Node Vicinity--")) {
					nodeVicinityEntry = 1;
				}
				if(line.equals("--Start of Node Info--")) {
					nodeVicinityEntry = 0;
					nodeInfoEntry = 1;
				}

				if(nodeVicinityEntry == 1) {
					String currentNode[] = line.split("-");
					if(currentNode[0].equals(this.nodeId)) {
						//Adding Current Node to initialNodeInfo List
						this.nodesKnowledge.put(currentNode[0], null);

						//Adding neighbors to initialNodeInfo List 
						String neighbors[] = currentNode[1].split(",");
						for(String neighbor:neighbors) {
							this.initialNodesInfo.put(neighbor, null);
						}
					}	
				}


				if(nodeInfoEntry == 1) {
					String nodesInfo[] = line.split("#");
					String currentScannedNode = nodesInfo[0];

					Set<String> setKey=initialNodesInfo.keySet();
					Iterator<String> it=setKey.iterator();
					while(it.hasNext()){
						String node = it.next();
						if(currentScannedNode.equals(node)) {
							this.initialNodesInfo.put(node, nodesInfo[1]);
							this.nodesKnowledge.put(node, nodesInfo[1]);
						}
					}
					if(currentScannedNode.equals(this.nodeId)) {
						this.nodesKnowledge.put(currentScannedNode, nodesInfo[1]);
					}

				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readNodesFromCinfig() {

	}
	
	public MessageFormat createPayLoad() {
		MessageFormat payload = new MessageFormat();
		payload.setMessageId(nextSessionId());
		payload.setSourceId(this.nodeId);
		payload.setSourcePort(this.port);
		payload.setSourceServer(this.server);
		payload.setNodesKnowledge(this.nodesKnowledge);
		return payload;
	}
	
	public String nextSessionId()
	{
		SecureRandom random = new SecureRandom();
		return new BigInteger(256, random).toString(32);
	}
	
	public boolean isNodeExplored(String nodeId) {
		if(exploredNodes.contains(nodeId)){
			return true;
		} else {
			return false;
		}
	}
	public void addNodeToExploredList(String nodeId) {
		this.exploredNodes.add(nodeId);
	}
}
