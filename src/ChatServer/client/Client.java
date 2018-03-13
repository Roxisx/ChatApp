//Place file in ClientServer project at location/path: ChatServer\src\ChatServer\client
//package ChatServer.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String host = "localhost"; //Still to do: Change to PC-Name for cross network communication
    private static final int minPortNumber = 1023;
    private static int portNumber = 6666;
    private static String userName;
    private static String serverHost;
    private static int serverPort;
    private static boolean endClient;

    
    //Input and output streams
    private Scanner clientInputScanner;
    private PrintStream clientOutputStream;

    public static void main(String[] args){
        Client client = new Client(userName, host, portNumber);
        client.startClient();
    }

    //Constructor manual input for username then port number
    private Client(String username, String host, int portnumber){
        endClient = false;
        this.clientInputScanner = new Scanner(System.in);
        this.clientOutputStream = new PrintStream(System.out);
        this.serverHost = host;
        this.userName = updateUsername();
        this.serverPort = updatePort();
    }
       
    // Basic getter and setter methods
    public PrintStream getOutPutStream(){
        return clientOutputStream;
    }
    public Scanner getInputScanner(){
        return clientInputScanner;
    }
    public void endClientThread(){
        Client.endClient = true;
    }
    
    //Loop get username from user anything goes but blank/null
    public final String updateUsername(){
        String input = null;
        while(input == null || input.trim().equals("")){
            clientOutputStream.print("Please input username: ");
            input = clientInputScanner.nextLine();
            if(input.trim().equals("")) {                                           // null, empty, whitespace(s) not allowed.
                System.out.println("Invalid. Please enter again:");
            }
        }
        return input;
    }
    
    /* Loop to get user input for port number greater than 1023 
     * press enter to use default port number
     * type 'Exit' OR 'exit' to end client 
     */
    public int updatePort(){
        String input;
        int port = portNumber;
        System.out.println("Please input port number greater than 1023 or Enter to use default (6666):");
        while(true){
            input = clientInputScanner.nextLine();
            if(input.equals("Exit") || input.equals("exit")){port = 0;break;}
            if(isInteger(input.trim())){
                port = Integer.parseInt(input); break;
            }else if(input.trim().equals("")){  
                break;
            }else{
                System.out.println("Please use digits more than 1023 or Enter for default. Type 'Exit' to leave.");
            }
        }
        return port;
    }
    
    //Check if port input interger greater than 1023
    public static boolean isInteger( String input ) {
        try {
            int value = Integer.parseInt( input );
            if(value > minPortNumber){
                return true;
            }else{
                return false;
            }
        }
        catch( Exception e ) {
            return false;
        }
    }

    /* Run when client starts create socket & thread to connect to server
     * once connected welcome message displayed
     * Loop to check for input and send to server using ClientThread 
     * If throws exception allows user to try using different port number
     * for both 
     */
    private void startClient(){
        try{
            Socket socket = new Socket(serverHost, serverPort);
            Thread.sleep(1000); // waiting for network communicating.

            ClientThread server = new ClientThread(socket, userName);
            Thread thread = new Thread(server);

            clientOutputStream.println("Welcome :" + userName);
            clientOutputStream.println("Local Port Number:" + socket.getLocalPort());
            clientOutputStream.println("Server Address: " + socket.getRemoteSocketAddress());
            clientOutputStream.println("Type 'Exit' to end the Client");

            thread.start();
            while(thread.isAlive() && !endClient){
                if(clientInputScanner.hasNextLine()){
                    String message = clientInputScanner.nextLine();
                    if(message.equals("Exit") || message.equals("exit")){//Type "Exit" to end the client allow time to send final message to server
                        server.addNextMessage(userName + " is Logging off ...");
                        clientOutputStream.println("Logging off ...");
                        endClientThread();
                    }
                    server.addNextMessage(message);
                }
                // NOTE: scan.hasNextLine waits input (in the other words block this thread's process).
                // NOTE: I recommend waiting short time like the following.
                // else {
                //    Thread.sleep(200);
                // }
            }       
            
            Thread.sleep(1000);
            endClient();
        }catch(IOException ex){
            clientOutputStream.println("Connection error! Check if correct Port number used? Error description below: \n" + ex.getMessage() + "\n");
            int port = updatePort();
            Client.portNumber = port; 
            Client.serverPort = port;
            if(port != 0){ startClient();  }
        }catch(InterruptedException ex){
            clientOutputStream.println("Interruption Occured! Check if correct Port number used? Error Description below: \n" + ex.getMessage() + "\n");
            int port = updatePort();
            Client.portNumber = port; 
            Client.serverPort = port;
            if(port != 0){ startClient();  }
        }
        
        endClient();
    }
    
    public void endClient(){
        clientInputScanner.close();
        clientOutputStream.close();
        System.exit(1);
    }
}
