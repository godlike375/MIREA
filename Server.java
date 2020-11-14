import java.net.*;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


//главный поток, который принимает новые подключения
public class Server {
    public static Integer counter;
    public static CopyOnWriteArrayList<CClient> list = new CopyOnWriteArrayList<CClient>();
    public static ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<Task>();
    public static ConcurrentLinkedQueue<Task> ready = new ConcurrentLinkedQueue<Task>();
    public static void main(String[] args) throws IOException {
        counter = new Integer(0);
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
         
        Worker worker = new Worker();
        new Thread(worker).start();
        TaskListener listener = new TaskListener();
        new Thread(listener).start();
        Answerer answerer = new Answerer();
        new Thread(answerer).start();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) 
        {
            while(true)
            {
                Socket client = serverSocket.accept();
                System.out.println("new client");
                list.add(new CClient(client));
                System.out.println(list.size());
                synchronized(Server.counter)
                {
                    Server.counter++;
                }
            } 
        }
        catch(Exception e)
        {
            System.out.println(e + "123");
        }
        
    }
}

class CClient
{
    public PrintWriter out;
    public BufferedReader in;
    public Socket socket;
    public CClient(Socket clientSocket)
    {
        try
        {
            out = new PrintWriter(clientSocket.getOutputStream(), true); 
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            socket = clientSocket;
        }
        catch (IOException e) {
            Server.list.remove(this);
            System.out.println("Exception caught client");
            System.out.println(e.getMessage());
            synchronized(Server.counter)
            {
                Server.counter--;
            }
        }
    }
    public void close()
    {
        try{
           out.close();
            in.close(); 
            socket.close(); 
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}

class Task
{
    public String task;
    public String answer;
    public CClient client;
    public Task(String t, CClient c)
    {
        task = t;
        client = c;
    }
}

//Поток, который принимает задания от клиентов
class TaskListener implements Runnable {
    @Override
    public void run() {
        while(true)
            for(CClient c: Server.list)
            {
                try
                {
                    System.out.println("listener "+Integer.toString(Server.list.size()));
                    if (c.in.ready())
                    {
                        String task = c.in.readLine();
                        if (task!=null)
                        {
                            Server.tasks.add(new Task(task, c));
                            System.out.println("listened task: "+task);
                        }
                            
                        else
                        {
                            c.close();
                            Server.list.remove(c);
                            synchronized(Server.counter)
                            {
                                Server.counter--;
                            }
                        }

                    }
                }
                catch (IOException e)
                {
                    Server.list.remove(c);
                    System.out.println("Exception caught TaskListener");
                    System.out.println(e.getMessage());
                    synchronized(Server.counter)
                    {
                        Server.counter--;
                    }
                }
                    
            }
    }
}

//Поток, который принимает результаты от воркера и возвращает клиентам
class Answerer implements Runnable {
    @Override
    public void run() {
        while(true)
            if(Server.ready.size() > 0)
            {
                Task t = Server.ready.poll();
                System.out.println("result: "+t.answer);
                t.client.out.println(t.answer);
            }
    }
}


//Поток, выполняющий все вычисления
class Worker implements Runnable {
    

    @Override
    public void run() {
        while(true)
        {
            if(Server.tasks.size() > 0)
            {
                Task t = Server.tasks.poll();
                System.out.println("task: "+t.task);
                int sc = 0;
                synchronized(Server.counter)
                {
                   sc = Server.counter;
                }
                String result = Double.toString(split_calc(t.task)) +" "+ Integer.toString(sc);
                t.answer = result;
                Server.ready.add(t);
            }
             
        }
                    

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

}

