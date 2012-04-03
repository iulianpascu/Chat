import java.io.*;
import java.net.*;

class ServerThread implements Runnable{
	private Socket client=null; 
	private WaitingServer server=null;
	DataInputStream input=null;
	DataOutputStream output=null;
	String nume=null;
	private volatile Thread thread=null;
	
	ServerThread(Socket _client,WaitingServer _server){
		client=_client;
		server=_server;
		open();
		nume=client.getRemoteSocketAddress().toString();	
	}
	
	private void open(){
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
	
	
	public void chatAccepted(){
		thread=new Thread(this);
		thread.start();
	}

	
	public void run(){
		askForAlias();
		while (thread!=null){
			 try {
				server.manage(input.readUTF(),nume);
			} catch (IOException e) {
				server.remove(nume);
				//System.out.println(name);
			}
		 }
	}
	
	public void stop(){
		thread=null;
		System.out.println("Userul "+nume+" s-a deconectat");
			try {
		input.close();
		output.close();
			} catch (IOException e) {}
		
	}
	
	private void askForAlias(){
		try {
			//cer alias
			String newName;
			while(server.alias.containsKey(newName=input.readUTF().trim()) 
					||server.keyWord(newName))
				send("Alias ocupat");
			server.alias.put(newName,this);
			System.out.println("Userul " +newName+ " s-a alaturat distractiei");
			send("Te numesti: "+ newName);
			nume=newName;
		} catch (IOException e) {
			System.out.println("cineva nu si-a putut alege alias");
			stop();
			server.clientiConectati--;
			//daca err -> deconectam clientul 
		}
		
	}
	
	public void send(String message){
		try {
			output.writeUTF(message);
			output.flush();
		} catch (IOException e) {		
		}
	}
}