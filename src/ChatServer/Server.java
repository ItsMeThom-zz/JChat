/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import chatassignment.ChatMessage;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatassignment.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.Inet4Address;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author K00202323
 * 
 * Core Server Thread - 
 * 
 * Handles Connecting clients with ClientThread internal class,
 * Broadcasts ChatMessage objects to each connected client
 */
public class Server extends Thread implements MessageListener{

    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;
    
    //MessageRecived listener (The form)
    private MessageListener Listener;

    //list of Client BufferedWriters for Client Management
    private ArrayList<ChatClient> Clients;
    
    
    //lists for giving random usernames
    protected List<String> AdjectiveList;
    protected List<String> AnimalsList;

    public Server(int port) {
        
        this.serverPort = port;
        Clients = new ArrayList<>();
        
        this.LoadAdjectives();
        this.LoadAnimals();
    }
    
    public void setMessageListener(MessageListener l)
    {
        this.Listener = l;
    }

    @Override
    public void run() {

        openServerSocket();

        while (!isStopped()) {

            Socket clientSocket = null;
            try {
                System.out.println("Awaiting Connection..");
                clientSocket = this.serverSocket.accept();

            } catch (IOException e) {
                if (isStopped()) {

                    System.out.println("Server Stopped.");
                    return;
                }

                throw new RuntimeException(
                        "Error accepting client connection", e);

            }
            //start the ClientConnectionThread
            ChatClient c = new ChatClient(clientSocket);
            Clients.add(c);
            c.start();
            

        }
        //System.out.println("Server Stopped.") ;
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void Shutdown() {
        //Closes the server socket and shuts down the server
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        //opens the Server Socket and waits for connections
        try {
            
            this.serverSocket = new ServerSocket(this.serverPort);
            
        } catch (IOException e) {
            //throw new RuntimeException("Cannot open port " + this.serverPort, e);
            System.out.println("Cannot open port: " + this.serverPort);
        }

    }
    
    
    protected void BroadcastMessage(ChatMessage msg)
    {
        //send ChatMessage object
        for(ChatClient c : this.Clients)
        {
            //Dont send a users audio back to them (Its god damn annoying!)
            if(msg.TYPE == ChatMessage.MessageType.AUDIO)
            {
                if(!msg.User.equals(c.USERNAME)){c.Send(msg);}
            }
            else
            {
                c.Send(msg);
            }
            
        }
        
    }
    
    
    public void MessageRecieved(ChatMessage msg)
    {
        //raise event with the listener that we got a chat message
        if(this.Listener != null) this.Listener.MessageRecieved(msg);
    }
    
    public void KickClient(String username)
    {
        // find and notify the client hes been kicked
        // then shutdown the thread
        for(ChatClient c : Clients)
        {
            if(c.USERNAME.equals(username))
            {
                c.Send(new ChatMessage(ChatMessage.MessageType.MESSAGE, "SERVER", "You have been kicked from the server"));
                BroadcastMessage(new ChatMessage(ChatMessage.MessageType.MESSAGE, "SERVER", username + " was kicked."));
                c.Quit();
                break;
            }
        }
        
    }
    
    public Boolean UsernameCollision(String newname)
    {
        Boolean collision = false;
        if(Clients.isEmpty())
        {
            return false;
        } else {
            for(ChatClient c : Clients)
            {
                if(c.USERNAME != null)
                {
                    if(c.USERNAME.equals(newname)){collision = true; break;}
                }    
            } 
        }

        return collision;
    }
    
    public String GetRandomUsername()
    {
        //generates a random username in the form AdjectiveAnimal (e.g. AngryGoat)
        Random r = new Random();
        String adj = this.AdjectiveList.get(r.nextInt(this.AdjectiveList.size()-1));
        String animal = this.AnimalsList.get(r.nextInt(this.AnimalsList.size()-1));
        
        adj = adj.substring(0, 1).toUpperCase() + adj.substring(1);
        animal = animal.substring(0, 1).toUpperCase() + animal.substring(1);
        return adj.concat(animal);
    }
    
    public void LoadAdjectives()
    {
        //loads the list of random adjectives for generating random usernames
        try (Scanner s = new Scanner(new File("adjectives.txt"))) {
            this.AdjectiveList = new ArrayList<String>();
            
            while (s.hasNextLine()){
                this.AdjectiveList.add(s.nextLine());
            }
        } catch (FileNotFoundException ex) {
            //inform the user we couldnt load the adjectives list
            System.out.println("couldnt load adjectives");
        }
        
    }
    
    public void LoadAnimals()
    {
        //loads the list of random animals for generation random usernames
        try (Scanner s = new Scanner(new File("animals.txt"))) {
            this.AnimalsList = new ArrayList<String>();
            
            while (s.hasNextLine()){
                this.AnimalsList.add(s.nextLine());
            }
        } catch (FileNotFoundException ex) {
            //inform the user we couldnt load the adjectives list
            System.out.println("couldnt load animals");
        }
    }
    
    

    //internal class - ChatClient Thread for handling connected client sockets
    class ChatClient extends Thread {

        private Socket Socket; // A connected socket
        private ObjectInputStream in;
        private ObjectOutputStream out;
        public volatile Boolean Running;

        public String USERNAME = null;
        
        public ChatClient(Socket socket) {
            Socket = socket;

            try {

                //get out
                out = new ObjectOutputStream(Socket.getOutputStream());
                
                //get in
                in = new ObjectInputStream(Socket.getInputStream());

                

                //shake it all about :p 
                Running = true;
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }

        @Override
        public void run() {
            try {
                while (Running) {

                    ChatMessage msg = (ChatMessage) this.in.readObject();
                    if(msg.TYPE == ChatMessage.MessageType.CONNECT) //new user login
                    {
                        
                        if(UsernameCollision(msg.User))
                        {
                            //name exits, generate a random one
                            this.USERNAME = GetRandomUsername();
                            
                            ChatMessage nameupdate = new ChatMessage(ChatMessage.MessageType.NAMECHANGE, this.USERNAME, msg.User + " now known as" + this.USERNAME);
                            
                            this.Send(nameupdate);
                            msg.User = this.USERNAME;
                            MessageRecieved(msg);
                            MessageRecieved(nameupdate);
                            BroadcastMessage(new ChatMessage(ChatMessage.MessageType.MESSAGE, "SERVER", this.USERNAME + " joined\n"));
                        }
                        else{ //unqiue name
                            this.USERNAME = msg.User;
                            MessageRecieved(msg); 
                            BroadcastMessage(msg);
                        }
                       
                        //fire message recieved event
                        // to draw to server GUI
                        
                    }
                    else //send the message object to connected clients
                    {
                        BroadcastMessage(msg);
                    }

                }
            } catch (IOException ex) {
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }

            //disconnection
            if(this.USERNAME != null)
            {
                ChatMessage msg = new ChatMessage(ChatMessage.MessageType.DISCONNECT, this.USERNAME, " DISCONNECTED");
                MessageRecieved(msg); //fire GUI event
                BroadcastMessage(msg); //broadcast to all clients
            }
        }
        
        public synchronized void Send(String msg)
        {
            //Send a string message to the client
            try{
                
                this.out.writeUTF(msg);
                this.out.flush();
            }catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            
            
            
        }
        
        public synchronized void Send(ChatMessage msg)
        {
            //Send a ChatMessage object to the client
            try{
                this.out.writeObject(msg);
                this.out.flush();
            }catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            
            
            
        }

        public void Quit() {
            Running = false;
        }

    }
}
