import java.io.*;
import java.net.*;
import java.util.*;

class WaitingServer implements Runnable{
	ServerSocket mainSocket=null;
	int MaxClienti=50,clientiConectati=0;
	Boolean[] conectat=new Boolean[MaxClienti];
	Hashtable<String, ServerThread> alias= new Hashtable<String, ServerThread>();
	
	WaitingServer(int port){
		try {
			mainSocket=new ServerSocket(port);
			System.out.println("serverul e deschis. va puteti conecta");
			
		} catch (IOException e) {
			System.out.println("nu am putut crea serverul: "+ e.getMessage());
		}
	}
	
	public void sendMessage(String message,String sender,String receiver){
		String _receiver;
		
		if(receiver.equals("ALL")){
			Enumeration<String> clienti=alias.keys();
			while(clienti.hasMoreElements()){
				_receiver=clienti.nextElement();
				alias.get(_receiver).send(sender + ": " + message);
			}
		}
		else{
			if(alias.containsKey(receiver)){
				alias.get(receiver).send(sender + ": " + message);
			}
			else if(alias.containsKey(sender)){
				alias.get(sender).send("SERVER: userul "+receiver+" nu e conectat");
			}
		}
			
	}
	
	public synchronized void insert(Socket client){
		if(clientiConectati< MaxClienti){
			ServerThread clientNou=new ServerThread(client,this);
			try {
				//cer alias
				String nume;
				while(alias.containsKey(nume= clientNou.askForAlias() ))
					clientNou.send("Alias ocupat");
				clientNou.setName(nume);
				clientNou.chatAccepted();
				alias.put(nume,clientNou);
			} catch (IOException e) {
				System.out.println("cineva nu si-a putut alege alias");
				clientNou.stop();
				clientNou=null;
				clientiConectati--;
				//daca err -> deconectam clientul 
			}
			
		}
		else{
			System.out.println("Serverul suporta deja nr maxim de clienti");
			}
	}
	
	public synchronized void remove(String nume){
		if(alias.containsKey(nume)){
			alias.get(nume).stop();
			alias.remove(nume);
			clientiConectati--;
		}
	}
	
	public void run(){
		Socket soc=null;
		while(true && mainSocket!=null){
			try {
				soc=mainSocket.accept();
				insert(soc);
			} catch (IOException e) {
				System.out.println("eroare conectare client: "+e.getMessage());
			}
		}
	}
	
	public void manage(String message,String sender){
		if(message.equals("QUIT")){
			if(alias.containsKey(sender)){
				alias.get(sender).send("ok, te-am deconectat");
				alias.get(sender).stop();
				alias.remove(sender);
			}
		}
		else if(message.equals("LIST")){
			String lista="";
			Enumeration<String> clienti=alias.keys();
			while(clienti.hasMoreElements())
				lista+=clienti.nextElement()+"\n";
			sendMessage(lista,"LISTA USERi concetati",sender);	
		}
		else if(message.startsWith("NICK ")){
			String newName=message.substring(4).trim();
			if(alias.containsKey(newName))
				sendMessage("Alias ocupat","SERVER",sender);
			else{
				ServerThread aux=alias.get(sender);
				alias.remove(sender);
				aux.name=newName;
				alias.put(newName, aux);
				System.out.println("Userul "+ sender+" rebotezat cu aliasul "+ newName);
			}
				
		}
		else if(message.startsWith("BCAST ")){
			sendMessage(message.substring(6),sender,"ALL");
		}
		else if(message.startsWith("MSG ")){
			String receiver;
			message=message.substring(4).trim();
			receiver=message.substring(0,message.indexOf(" "));
			message=message.substring(message.indexOf(" ")).trim();
			sendMessage(message,sender,receiver);
		}
		else{
			sendMessage("comanda invalida","SERVER",sender);
		}
			
	}
}

class MainServer{
	
	public static void main(String[] string) throws IOException{
		WaitingServer mainWaitingServer=null;
		
		Scanner console=new Scanner(System.in);
		System.out.println("introduceti portul");
		mainWaitingServer=new WaitingServer(console.nextInt());
		
		Thread acceptingThread=new Thread(mainWaitingServer);
		acceptingThread.setDaemon(true);
		acceptingThread.start();
		//mainServer.start();
		
		String s="";
		while(!s.equals("QUIT"))
			s=console.next();
		System.out.println("Serverul a fost inchis");
		
		//mainServer.sendShutDownMessageToClients();
		mainWaitingServer.sendMessage("inchidem pravalia ;)", "SERVER", "ALL");
		
		//mainServer.stop();
	}
}
