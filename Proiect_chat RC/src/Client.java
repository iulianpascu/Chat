import java.io.*;
import java.net.*;
import java.util.Scanner;

class ClientMain{
	static Socket server=null;
	private static Scanner console;
	private static DataInputStream remoteInput=null;
	private static DataOutputStream remoteOutput=null;
	private volatile static Boolean running=true;
	//private static Writer demon=null;
	
	public static void main(String[] args){
		console = new Scanner(System.in);
		String adresa; int port;
		
		if( args.length !=2){
			System.out.println("Introduceti adresa si portul serverului");
			adresa=console.next();
			port=console.nextInt();
			console.close();
		}
		else{
			adresa=args[0];
			port=Integer.parseInt(args[1]);
		}
		
		try {
			server=new Socket(adresa,port);
			openComm();
			//demon=
			new Writer(remoteOutput);
			//demon.start()
			
			//remoteInput - localOutput 
			String vorbe="";
			while(running ){
				try {
					vorbe=remoteInput.readUTF();
					print(vorbe);
				} catch (IOException e) {
					System.out.println("Pierdut legatura cu serverul..");
					stop();
				}
			}
			/*
			if(demon!=null)
				demon.stop();
			*/
		} catch (UnknownHostException e) {
			System.out.println("nu s-o putut conecta " + e.getMessage());
		} catch (IOException e) {
			System.out.println("eroare "+ e.getMessage());
		}
	}
	
	private static void print(String vorbe){
		System.out.println(vorbe);
		if(vorbe.startsWith("SERVER: ok, te-am deconectat"))
				stop();
		if(vorbe.startsWith("SERVER: inchidem pravalia"))
				stop();
	}
	
	private static void openComm(){
		try {
			remoteInput=new DataInputStream(new BufferedInputStream(server.getInputStream()));
			remoteOutput=new DataOutputStream(new BufferedOutputStream(server.getOutputStream()));
		} catch (IOException e) {
			System.out.println("nu pot comunica cu serverul");
			stop();
		}
	}
	
	public static void stop(){
		running=false;
		try {
		if(remoteInput!=null)
			remoteInput.close();
		} catch (IOException e) {}
		try{
		if(remoteOutput!=null)
			remoteOutput.close();
		} catch (IOException e) {}
		//demon=null;
	}
}

class Writer implements Runnable{
	//localInput -- remoteOutput
	private DataOutputStream remoteOutput=null;
	Thread listening=null;
	
	
	Writer(DataOutputStream _remoteOutput){
		remoteOutput=_remoteOutput;
		listening=new Thread(this);
		listening.setDaemon(true);
		listening.start();
	}
	
	public void run(){
		//localInput -- remoteOutput
		Scanner console = new Scanner(System.in);
		String comanda="";
		while(!comanda.equals("FORCEQUIT") && listening!=null){
			comanda=console.nextLine();
			try {
				remoteOutput.writeUTF(comanda);
				remoteOutput.flush();
			} catch (IOException e) {
				stop();
			}
		}
		
		stop();
	}
	
	public void stop(){
		if(listening!=null){
			listening.interrupt();
			listening=null;
		}
		ClientMain.stop();
	}
}