import java.net.*;
import java.io.*;

class ServerThread implements Runnable {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int portNumber;
    public ServerThread(ServerSocket server, Socket client, int port)
    {
        serverSocket = server;
        clientSocket = client;
        portNumber = port;
    }
    private double split_calc(String line)
    {
        String[] lines = line.split(" ");
        double a1 = Double.parseDouble(lines[0]);
        char op = lines[1].charAt(0);
        double a2 = Double.parseDouble(lines[2]);
        return calculate(a1, a2, op);
    }
    private double calculate(double arg1, double arg2, char op)
    {  
        switch(op)
        {
            case '+': return arg1+arg2;
            case '-': return arg1-arg2;
            case '*': return arg1*arg2;
            case '/': return arg1/arg2;
        }
        return 0;

    }
    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            out = new PrintWriter(clientSocket.getOutputStream(), true); 
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                out.println(Double.toString(split_calc(inputLine)));
            }
        }
         catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                out.close();
                in.close();
                serverSocket.close();
                clientSocket.close();  
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            
        }
    }
}

public class Server {
    public static void main(String[] args) throws IOException {
         
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        while(true)
	    {
            ServerSocket serverSocket =
                new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread(new ServerThread(serverSocket, clientSocket, portNumber));
            t.start();    
            portNumber++;
        } 
        
    }
}