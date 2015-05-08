import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;


public class Starter03 {
	public static void main(String[] args) throws Exception {
		long lStartTime = new Date().getTime();
		String nodeId = args[0].trim();
		String server = args[1].trim();
		int port = Integer.valueOf(args[2].trim());
		
		LogToFile.CreateWriteFile(nodeId, "Start Of Node Initialization");
		LogToFile.CreateWriteFile(nodeId, "Node Id is->" + nodeId + " Node Server Is ->" +server+ " Node port is ->"+port);		
		System.out.println( "Node Id is->" + nodeId + " Node Server Is ->" +server+ " Node port is ->"+port);
		Node node = new Node(nodeId ,port, server);
		node.nodeInitialize();

	    System.out.println("######################Printing Initial Knowledge##########################");
	    LogToFile.CreateWriteFile(node.getNodeId(), "######################Printing Initial Knowledge##########################");
	    
	    TreeMap<String, String> nodesKnowledgeMap = node.getNodesKnowledge();
	    Set<String> setNodes=nodesKnowledgeMap.keySet();
	    Iterator<String> iter=setNodes.iterator();
	    while(iter.hasNext()){
	    	String node_Id = iter.next();
	    	LogToFile.CreateWriteFile(node.getNodeId(), "nodeId ->"+node_Id);
	    	System.out.println("nodeId ->"+node_Id);
	    }
	    
	    Collection<String> node_infos=nodesKnowledgeMap.values();
	    Iterator<String> nodeInfo_val=node_infos.iterator();
	    while(nodeInfo_val.hasNext()){
	    	String nodeInfo = nodeInfo_val.next();
	    	LogToFile.CreateWriteFile(node.getNodeId(), "nodeInfo->"+nodeInfo);
	    	System.out.println("nodeInfo->"+nodeInfo);
	    }
	    
	    System.out.println("################################################");

		ProcessMessage server_thread = ProcessMessage.getProcessMessage(node);
		server_thread.initializeServerToReceive(node.getPort());
		Thread serverThread = new Thread(server_thread);
		try {
			Thread.currentThread().sleep(3000);
			serverThread.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		//------Sleep For few seconds so that all the nodes get initialized------
		System.out.println("Sleep mode for Few Seconds to allow other nodes to spawn....");
		LogToFile.CreateWriteFile(nodeId, "Sleep mode for Few Seconds to allow other nodes to spawn....");

		Thread.currentThread().sleep(20000);
		LogToFile.CreateWriteFile(node.getNodeId(), "Out Of Sleep Mode.");
		System.out.println("Out Of Sleep Mode.");
		
		//------ Send Message to initial neighbors by iterating through initialNodesInfo of Node.java-----
		LogToFile.CreateWriteFile(node.getNodeId(), "Sending Initial Knowledge to neighboring nodes");
		System.out.println("Sending Initial Knowledge to neighboring nodes");
		//Create the message to send
		MessageFormat payload = node.createPayLoad();
		//Send Messages to unexplored nodes in nodesKnowledge
		node = server_thread.sendNodeKnowledge(node, payload);
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		while(true) {
			
			if(node.isStopProcessing()) {
				break;
			}
			LogToFile.CreateWriteFile(node.getNodeId(), "Waiting for message....");
			System.out.println("Waiting for message....");
			node = server_thread.proceessMessageQueue(node);
			Thread.currentThread().sleep(5000);
			
			lEndTime = new Date().getTime();
			difference = lEndTime - lStartTime;
			LogToFile.CreateWriteFile(node.getNodeId(), "Time since start up(in milliSecs)...."+difference);
			if (difference > 100000) {
				break;
			}

		}
		
		server_thread.setReceiveThreadTerminationCondition(true);
		//Send A dummy Message To take the thread out of waiting state
		MessageFormat dummyMessage = new MessageFormat();
		server_thread.sendToNode(node.getServer(), node.getPort(), dummyMessage);
		server_thread.closeServerConnection();
		
		LogToFile.CreateWriteFile(node.getNodeId(), "######################Printing Final Knowledge##########################");
	    System.out.println("######################Printing Final Knowledge##########################");

	    TreeMap<String, String> nodesKnowledgeMap_new = node.getNodesKnowledge();
	    Set<String> setNodes_new=nodesKnowledgeMap_new.keySet();
	    Iterator<String> iter_new=setNodes_new.iterator();
	    while(iter_new.hasNext()){
	    	String node_Id = iter_new.next();
	    	LogToFile.CreateWriteFile(node.getNodeId(), "nodeId ->"+node_Id);
	    	System.out.println("nodeId ->"+node_Id);
	    }
	    
	    Collection<String> node_infos_new = nodesKnowledgeMap_new.values();
	    Iterator<String> nodeInfo_vals_new=node_infos_new.iterator();
	    while(nodeInfo_vals_new.hasNext()){
	    	String nodeInfo = nodeInfo_vals_new.next();
	    	LogToFile.CreateWriteFile(node.getNodeId(), "nodeInfo->"+nodeInfo);
	    	System.out.println("nodeInfo->"+nodeInfo);
	    }
	    LogToFile.CreateWriteFile(node.getNodeId(), "################################################");
	    LogToFile.CreateWriteFile(node.getNodeId(), "--Node Discovery Completed--");
	    
	    System.out.println("################################################");
	    System.out.println("--Node Discovery Completed with SCTP Protocol--");
		
	}

}
