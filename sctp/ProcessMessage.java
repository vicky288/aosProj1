import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.SealedObject;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;


public class ProcessMessage implements Runnable{
	public static final int MESSAGE_SIZE = 30000;

	private static ProcessMessage instance = null;
	ArrayList<MessageFormat> messageQueue = new ArrayList<MessageFormat>();
	ArrayList<MessageFormat> sentMessageList = new ArrayList<MessageFormat>();

	private ServerSocket serverSocket;
	private Socket clientSocket;
	Node myNodeInfo;
	private boolean receiveThreadTerminationCondition = false;
	
	
	SctpServerChannel sctpServerChannel;	
	
	public boolean isReceiveThreadTerminationCondition() {
		return receiveThreadTerminationCondition;
	}
	public void setReceiveThreadTerminationCondition(
			boolean receiveThreadTerminationCondition) {
		this.receiveThreadTerminationCondition = receiveThreadTerminationCondition;
	}
	//Private Constructor
	private ProcessMessage() {

	}
	//Singleton Class
	public static ProcessMessage getProcessMessage(Node myNodeinfo) {
		if(instance == null) {
			System.out.println("Node Instance is null");
			instance = new ProcessMessage();
			instance.myNodeInfo = myNodeinfo;
		}
		return instance;
	}

	public void run() {
		//System.out.print("-");
		try {
			receiveData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//Methods For Sending Message
	public void initializeClientToSend(String serverName, int portNumber){
		//System.out.println("Client initializing");
		try {
			clientSocket = new Socket(serverName,portNumber);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(MessageFormat message) throws Exception{
		//System.out.println("sending 1 message");
		OutputStream os= clientSocket.getOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(message);
	}

	public void sendToNode(String server, int port, MessageFormat message, int dummy) throws Exception {
		//System.out.println("--Sending to Node--");
		initializeClientToSend(server,port);
		send(message);
		closeClientConnection();
		//System.out.println("--Message Sent to Node--");
	}

	public void sendToNode(String server, int port, MessageFormat message) throws Exception {

		//Buffer to hold messages in byte format
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		int clientPort = port+101;
		try
		{

			byte [] msgToSend = MessageFormat.objectToByteArray(message);
			//Create a socket address for  server at net01 at port 5000
			SocketAddress socketAddress = new InetSocketAddress(server,port);
			//Open a channel. NOT SERVER CHANNEL
			SctpChannel sctpChannel = SctpChannel.open();
			//Bind the channel's socket to a local port. Again this is not a server bind
			sctpChannel.bind(new InetSocketAddress(clientPort));
			//Connect the channel's socket to  the remote server
			sctpChannel.connect(socketAddress);
			//Before sending messages add additional information about the message
			MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
			//convert the string message into bytes and put it in the byte buffer
//			byteBuffer.put(message.getBytes());
			byteBuffer.put(msgToSend);
			//Reset a pointer to point to the start of buffer 
			byteBuffer.flip();
			//Send a message in the channel (byte format)
			sctpChannel.send(byteBuffer,messageInfo);
			sctpChannel.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	
	}
	public void closeClientConnection() {
		
	}
	public void closeClientConnection(int dummy) {
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Methods to receive Data
	public String initializeServerToReceive(int serverPort,int dummy)
	{
		String statusMessage="OK";
		try
		{
			LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Server initializing");
			System.out.println("Server initializing");
			serverSocket = new ServerSocket(myNodeInfo.getPort());
		} 
		catch (IOException e) {
			statusMessage = "TCP Port"+ serverPort +"is occupied.";
			e.printStackTrace();
		}
		return statusMessage;
	}
	
	public String initializeServerToReceive(int serverPort)
	{
		String statusMessage="OK";
		try
		{
			LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Server initializing");
			System.out.println("Server initializing");
			//Open a server channel
			sctpServerChannel = SctpServerChannel.open();
			//Create a socket addess in the current machine at port 5000
			InetSocketAddress serverAddr = new InetSocketAddress(serverPort);
			//Bind the channel's socket to the server in the current machine at port 5000
			sctpServerChannel.bind(serverAddr);
			//Server goes into a permanent loop accepting connections from clients			

		} 
		catch (IOException e) {
			statusMessage = "TCP Port"+ serverPort +"is occupied.";
			e.printStackTrace();
		}
		return statusMessage;
	}
	
	public void receiveData(int dummy) throws IOException, ClassNotFoundException {
		while(!receiveThreadTerminationCondition){
			//System.out.println("+");
			//System.out.println("Start of Receive");
			Socket connectionSocket = serverSocket.accept();
			InputStream is = connectionSocket.getInputStream();
			ObjectInputStream retrieveStream = new ObjectInputStream(is);
			MessageFormat retrievedObject = (MessageFormat) retrieveStream.readObject();
			messageQueue.add(retrievedObject);
			//System.out.println("One Message Received");
			//System.out.println("Total number of messages in the queue->"+messageQueue.size());
//			if(receiveThreadTerminationCondition) {
//				break;
//			}
		}
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}
	
	public void receiveData() throws IOException, ClassNotFoundException {
		while(!receiveThreadTerminationCondition){
			ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			//Listen for a connection to be made to this socket and accept it
			//The method blocks until a connection is made
			//Returns a new SCTPChannel between the server and client
			SctpChannel sctpChannel = sctpServerChannel.accept();
			//Receive message in the channel (byte format) and store it in buf
			//Note: Actual message is in byre format stored in buf
			//MessageInfo has additional details of the message
			MessageInfo messageInfo = sctpChannel.receive(byteBuffer,null,null);
			//Just seeing what gets stored in messageInfo
			System.out.println(messageInfo);
			//Converting bytes to string. This looks nastier than in TCP
			//So better use a function call to write once and forget it :)
			byte [] messageReceived = byteBufferToByteArray(byteBuffer);
			MessageFormat retrievedObject = MessageFormat.byteArraytoObject(messageReceived);			
			messageQueue.add(retrievedObject);
		}
	}
	public byte[] byteBufferToByteArray(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return bufArr;
	}
	
	public void closeServerConnection(int dummy) {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeServerConnection() {
		try {
			sctpServerChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Process The Message Queue
	public Node proceessMessageQueue(Node node) throws Exception {

		//Process Message Queue
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "------------------------Inside proceessMessageQueue()-------------------------------------");
		System.out.println("------------------------Inside proceessMessageQueue()-------------------------------------");
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Size of Message Queue->"+messageQueue.size());
		System.out.println("Size of Message Queue->"+messageQueue.size());
		if(messageQueue.size() != 0) {
			ArrayList<MessageFormat> receviedMsgList = (ArrayList<MessageFormat>) messageQueue.clone();
			messageQueue.clear();
			LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Received Msg List->"+receviedMsgList.size());
			System.out.println("Received Msg List->"+receviedMsgList.size());
			for(MessageFormat message:receviedMsgList) {
				LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Message Received from node->"+message.getSourceId());
				System.out.println("Message Received from node->"+message.getSourceId());
				//Is a acknowledgment Message - done
				boolean isAckMsg = message.isAcknowledged();
				if(isAckMsg) {
					LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "****Message is Acknowledgement");
					System.out.println("****Message is Acknowledgement");
					//If Ack message Remove Corresponding Message from Sent List- done
					removeMessageFromSentList(message.getMessageId());
					//Update current Node's Knowledge -- done
					node = compareKnowledge(message, node);
					
					//Create the payload to send to unexplored nodes
					MessageFormat payload = node.createPayLoad();
					//Send Messages to unexplored nodes in nodesKnowledge
					node = sendNodeKnowledge(node, payload);

				} else { //If not Ack message
					LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "~~~Message is not Acknowledgement");
					System.out.println("~~~Message is not Acknowledgement");
					// compare knowledgeMap --done
					node = compareKnowledge(message, node);
					
					//Add the source to Explored message List
					node.addNodeToExploredList(message.getSourceId());
										
					//Prepare Payload for ack message --done
					message = createPayloadForAckMessage(message, node);
					
					//Send Ack Message with updated knowledge --done
					LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Sending Ack Message");
					System.out.println("Sending Ack Message");
					String serverToSend = message.getDestServer();
					int portToSend = message.getDestPort();
						
					try {
						sendToNode(serverToSend, portToSend, message);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					//Create the payload to send to unexplored nodes
					MessageFormat payload = node.createPayLoad();
					//Send Messages to unexplored nodes in nodesKnowledge
					node = sendNodeKnowledge(node, payload);
				}
			}
		}
		
		if(sentMessageList.size()==0) {
			node.setStopProcessing(true);
		}
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Size of Sent Message List->"+sentMessageList.size());
		System.out.println("Size of Sent Message List->"+sentMessageList.size());
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "------------------------End Of proceessMessageQueue()-------------------------------------");
		System.out.println("------------------------End Of proceessMessageQueue()-------------------------------------");

		return node;
	}

	

	public MessageFormat createPayloadForAckMessage(MessageFormat message, Node node) {
		
		String newSourceId = message.getDestId();
		int newSourcePort = message.getDestPort();
		String newSourceServer = message.getDestServer();
		
		String newDestId = message.getSourceId();
		int newDestPort = message.getSourcePort();
		String newDestServer = message.getSourceServer();

		message.setSourceId(newSourceId);
		message.setSourcePort(newSourcePort);
		message.setSourceServer(newSourceServer);
		message.setDestId(newDestId);
		message.setDestPort(newDestPort);
		message.setDestServer(newDestServer);
		message.setAcknowledged(true);
		message.setNodesKnowledge(node.getNodesKnowledge());
		
		return message;
	}
	
	// RemoveMessage From Sent List - Tested
	public void removeMessageFromSentList(String messageId) {
		int index = 0;
		int removeIndex = 0;
		for(MessageFormat message:sentMessageList) {
			if(message.getMessageId().equals(messageId)) {
				removeIndex = index;
			}
			index++;
		}
		sentMessageList.remove(removeIndex);
	}


	//Compare Node Knowledge with Message Knowledge -- Tested
	public Node compareKnowledge(MessageFormat message, Node node) {
		//System.out.println("Comparing maps");
		//System.out.println(message.getNodesKnowledge().size());
		//System.out.println(node.getNodesKnowledge().size());
		
		TreeMap<String, String> mapFromNeighbor = message.getNodesKnowledge();
		TreeMap<String, String> mapFromNode = node.getNodesKnowledge();
		//System.out.println("-----");
		//System.out.println(mapFromNeighbor.size());
		//System.out.println(mapFromNode.size());
		
	    Set<String> setKeyNode=mapFromNode.keySet();

	    Set<String> setKeyNeighbor=mapFromNeighbor.keySet();
	    Iterator<String> it=setKeyNeighbor.iterator();
	    while(it.hasNext()){
	    	//System.out.println("Iterating neighbor Map");
	    	String nodeId = it.next();
	    	if(!setKeyNode.contains(nodeId)) {
	    		//System.out.println("add new Entry to node map");
	    		mapFromNode.put(nodeId, mapFromNeighbor.get(nodeId));
	    	}
	    }	
	    //Now update knowledgeMap of Node
	    node.setNodesKnowledge(mapFromNode);
		return node;
	}
	
	public Node sendNodeKnowledge(Node nodeInfo, MessageFormat payLoad) {
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "=================Inside sendNodeKnowledge()=======================");
		System.out.println("=================Inside sendNodeKnowledge()=======================");

		TreeMap<String, String> nodesKnowledgeMap = nodeInfo.getNodesKnowledge();
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Nodes Knowledge Map" + nodesKnowledgeMap.size());

		Set<String> setNodes=nodesKnowledgeMap.keySet();
		Iterator<String> iter=setNodes.iterator();
		while(iter.hasNext()){
			String nodeId = iter.next();
			if(!(nodeId.equals(nodeInfo.getNodeId())) && !nodeInfo.isNodeExplored(nodeId)){
				LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Sending NodeKnowledge to node(Not Ack)->"+nodeId);
				System.out.println("Sending NodeKnowledge to node(Not Ack)->"+nodeId);
				String nodeLocation = nodesKnowledgeMap.get(nodeId);
				//System.out.println("Node Location->"+nodeLocation);
				String nodeLocationArr[] = nodeLocation.split("@");
				String nodeServe = nodeLocationArr[0];
				int nodePort = Integer.valueOf(nodeLocationArr[1]);

				payLoad.setDestId(nodeId);
				payLoad.setDestPort(nodePort);
				payLoad.setDestServer(nodeServe);
				try {
					sendToNode(nodeServe, nodePort, payLoad);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				//System.out.println("Message Sent to node ->"+nodeServe +" @ "+nodePort);

				//Add the node id to exploredNodes List
				nodeInfo.addNodeToExploredList(nodeId);

				//Add the message to sentMessageList
				sentMessageList.add(payLoad);
			}
		}
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "=================End of sendNodeKnowledge()=======================");
		System.out.println("=================End of sendNodeKnowledge()=======================");
		return nodeInfo;
	}
	
	//This is the old Method
	public void proceessMessageQueue() throws ClassNotFoundException, IOException {
			while(sentMessageList.size() != 0) {
			//

			//send node knowledge

			//remove the node from sending list

			//wait till 

		}

		if(messageQueue.size()==0) {
			System.out.println("No messages yet received");
		}
		LogToFile.CreateWriteFile(this.myNodeInfo.getNodeId(), "Total number of messages in the queue->"+messageQueue.size());
		System.out.println("Total number of messages in the queue->"+messageQueue.size());
		for(MessageFormat messageReceived:messageQueue) {
			System.out.println("Message in the queue");
			System.out.println("Source Id"+messageReceived.getSourceId());
			System.out.println("Source Port"+messageReceived.getSourcePort());
		}

	}

}
