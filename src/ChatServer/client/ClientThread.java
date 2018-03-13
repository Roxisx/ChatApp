//package ChatServer.client;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private Socket socket;
    private String userName;
    private boolean isAlived;
    private final LinkedList<String> messagesToSend;
    private boolean hasMessages = false;
    
    private PrintWriter serverOutputStream;
    private InputStream serverInputStream;

    public ClientThread(Socket socket, String userName){
        this.socket = socket;
        this.userName = userName;
        messagesToSend = new LinkedList<String>();
    }

    public void addNextMessage(String message){
        synchronized (messagesToSend){
            hasMessages = true;
            messagesToSend.push(message);
        }
    }

    @Override
    public void run(){
        try{
            serverOutputStream = new PrintWriter(socket.getOutputStream(), false);
            serverInputStream = socket.getInputStream();
            Scanner scanner = new Scanner(serverInputStream);
            // BufferedReader userBr = new BufferedReader(new InputStreamReader(userInStream));
            // Scanner userIn = new Scanner(userInStream);
            serverOutputStream.println("New user '" + userName + "' has joined the chat");
            while(!socket.isClosed()){
                if(serverInputStream.available() > 0){
                    if(scanner.hasNextLine()){
                        System.out.println(scanner.nextLine());
                    }
                }
                if(hasMessages){
                    String nextSend = "";
                    synchronized(messagesToSend){
                        nextSend = messagesToSend.pop();
                        hasMessages = !messagesToSend.isEmpty();
                    }
                    serverOutputStream.println(userName + " ("+ new SimpleDateFormat("dd/MM/yyyy HH:ss").format(new Date()) + ") > " + nextSend);
                    serverOutputStream.flush();
                }
            }
            
            //Close & Exit
            serverInputStream.close();
            serverOutputStream.close();
            scanner.close();
            socket.close();
            System.exit(1);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
