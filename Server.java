import java.net.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;


//главный поток, который принимает новые подключени€
public class Server {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        final CopyOnWriteArrayList<CClient> clients = new CopyOnWriteArrayList<CClient>();
        final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<Task>();
        final ConcurrentLinkedQueue<Task> ready = new ConcurrentLinkedQueue<Task>();
        Worker worker = new Worker(tasks, ready, clients);
        new Thread(worker).start();
        TaskListener listener = new TaskListener(clients, tasks);
        new Thread(listener).start();
        Answerer answerer = new Answerer(clients, ready);
        new Thread(answerer).start();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(portNumber));
        serverSocket.configureBlocking(false);
        while(true)
        {
            final SocketChannel client = serverSocket.accept();
            if (client != null) 
            {
                client.configureBlocking(false);
                System.out.println("new client");
                clients.add(new CClient(client, ByteBuffer.allocateDirect(32)));
            }
            clients.removeIf((socketChannel) -> !socketChannel.socket.isOpen());
        } 
    
    }
}

class CClient
{
    public ByteBuffer buf;
    public SocketChannel socket;
    public CClient(SocketChannel clientSocket, ByteBuffer buffer)
    {
        socket = clientSocket;
        buf = buffer;
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

//ѕоток, который принимает задани€ от клиентов
class TaskListener implements Runnable {
    private CopyOnWriteArrayList<CClient> clients;
    private ConcurrentLinkedQueue<Task> tasks;
    public TaskListener(CopyOnWriteArrayList<CClient> c, ConcurrentLinkedQueue<Task> t)
    {
        clients = c;
        tasks = t;
    }
    @Override
    public void run() {
        while(true)
        {

            for(CClient c: clients)
            {
                try
                {
                    {
                        int numbytes = c.socket.read(c.buf);
                        if (numbytes == -1) 
                            clients.remove(c);
                        else if(numbytes==0);
                        else if(numbytes==2) // на клиенте пуста€ строка введена
                        {
                            clients.remove(c);
                        }
                        else
                        {
                            c.buf.flip();
                            if(c.buf.remaining()>2)
                            {
                                byte[] b = new byte[c.buf.remaining()];
                                c.buf.get(b);
                                String task = new String(b); //10'n 13'r'
                                tasks.add(new Task(task, c));
                                System.out.println("listened task: "+task);
                            }
                        }

                    }
                }
                catch (Exception e)
                {
                    clients.remove(c);
                }
                    
            }
        }
    }
}

//ѕоток, который принимает результаты от воркера и возвращает клиентам
class Answerer implements Runnable {
    private CopyOnWriteArrayList<CClient> clients;
    private ConcurrentLinkedQueue<Task> ready;
    public Answerer(CopyOnWriteArrayList<CClient> c, ConcurrentLinkedQueue<Task> r)
    {
        clients = c;
        ready = r;
    }
    @Override
    public void run() {
        while(true)
            if(ready.size() > 0)
            {
                Task t = ready.poll();
                System.out.println("result: "+t.answer);
                t.client.buf.rewind();
                t.client.buf.put(t.answer.getBytes());
                t.client.buf.rewind();
                try{
                    while (t.client.buf.hasRemaining()) {
                        t.client.socket.write(t.client.buf);
                   }
                }
                catch (IOException e) {
                    clients.remove(t.client);
                }
                t.client.buf.compact();
            }
    }
}


//ѕоток, выполн€ющий все вычислени€
class Worker implements Runnable {
    private ConcurrentLinkedQueue<Task> ready;
    private ConcurrentLinkedQueue<Task> tasks;
    private CopyOnWriteArrayList<CClient> clients;
    public Worker(ConcurrentLinkedQueue<Task> t, ConcurrentLinkedQueue<Task> r, CopyOnWriteArrayList<CClient> c)
    {
        ready = r;
        tasks = t;
        clients = c;
    }

    @Override
    public void run() {
        while(true)
        {
            if(tasks.size() > 0)
            {
                Task t = tasks.poll();
                System.out.println("task: "+t.task);
                int sc = 0;
                String result = Double.toString(split_calc(t.task)) +" "+ Integer.toString(clients.size());
                t.answer = result;
                ready.add(t);
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

