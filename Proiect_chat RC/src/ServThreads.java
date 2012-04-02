import java.io.*;
import java.net.*;

class ServerThread implements Runnable{
	private Socket client=null; 
	private WaitingServer server=null;
	DataInputStream input=null;
	DataOutputStream output=null;
	String name=null;
	private volatile Thread thread=null;
	
	ServerThread(Socket _client,WaitingServer _server){
		client=_client;
		server=_server;
		open();
		
	}
	
	public void open(){
		try {
			input = new DataInputStream(new 
			        BufferedInputStream(client.getInputStream()));
			output = new DataOutputStream(new
	                BufferedOutputStream(client.getOutputStream()));
			output.writeUTF("Introduceti un alias: ");
			output.flush();
		} catch (IOException e) {
			System.out.println("Eroare: "+ e.getMessage());
		}
	}
	
	public void setName(String _name){ 
		name=_name;
		System.out.println("Userul " +name+ " s-a alaturat distractiei");
		send("Te numesti: "+ name);
	}
	
	public void chatAccepted(){
		thread=new Thread(this);
		thread.start();
	}

	
	public void run(){
		while (thread!=null){
			 try {
				server.manage(input.readUTF(),name);
			} catch (IOException e) {
				server.remove(name);
				//System.out.println(name);
			}
		 }
	}
	
	public void stop(){
		thread=null;
		System.out.println("Userul "+name+" s-a deconectat");
			try {
		input.close();
		output.close();
			} catch (IOException e) {}
		
	}
	
	public String askForAlias()throws IOException{
		String alias=input.readUTF();
		return alias.trim();
	}
	
	public void send(String message){
		try {
			output.writeUTF(message);
			output.flush();
		} catch (IOException e) {		
		}
	}
}