/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatClient;

import chatassignment.AudioListener;
import chatassignment.MessageListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatassignment.ChatMessage;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author K00202323
 * 
 * Client Thread, handles connection to server and sending/recieveing messages
 * 
 * uses events to inform ClientGUI when it recieves messages or audio
 */



public class Client extends Thread implements MessageListener, AudioListener{
    
    private Socket Socket = null;
    
    
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    private Boolean exit = false;

    private MessageListener Listener;
    
    private AudioListener AudioListener;
    
    private String USERNAME = "Anon";
    
    public Boolean CONNECTED = false;
    
    Client(String addr, int port, String usr) throws IOException
    {
        try {
            this.Socket = new Socket(addr, port);
            
            getStreams();
            this.exit = false;
            this.CONNECTED = true;
            this.USERNAME = usr;
            
            this.WriteLogin(this.USERNAME);
            
        } catch (IOException ex) {
            throw ex;
            
        }
    }
    
    
    Client(Socket socket)
    {
        this.Socket = socket;
    }
    
    public void setListener(MessageListener l)
    {
        //Set listener for message recieved events
        this.Listener = l;
    }
    
    public void setAudioListener(AudioListener l)
    {
        //set listener for audio recieved events
        this.AudioListener = l;
    }
    
    private void getStreams()
    {
        //get the input and output streams from the socket connected to server
        
        try
        {
            this.out=new ObjectOutputStream(this.Socket.getOutputStream());
            this.in=new ObjectInputStream(this.Socket.getInputStream());
            
            
            
        }catch(IOException e)
        {
            System.out.println("Couldnt get socket streams!");
        }
    }
    
    private void WriteLogin(String username)
    {
       //tell the server we have connected (Server notes this and informs clients)
        
       ChatMessage m = new ChatMessage(ChatMessage.MessageType.CONNECT, username, " joined."); 
        
       try {
            
            
            this.out.writeObject(m);
            this.out.flush();
            
        } catch (IOException ex) {
            System.out.println("Couldn't write message to server!");
            this.CONNECTED = false;
        } 
        
    }
    
    public synchronized void WriteMessage(String msg)
    {
        //writes strings to a chatmessage object and sends to server
        //sync'd to prevent doubling
        try {
            ChatMessage m = new ChatMessage(ChatMessage.MessageType.MESSAGE, this.USERNAME, msg);
            
            this.out.writeObject(m);
            this.out.flush();
            
        } catch (IOException ex) {
            System.out.println("Couldn't write message to server! (Is connection down?)");
            this.CONNECTED = false;
        }
    }
    
    public synchronized void WriteAudio(ByteArrayOutputStream out)
    {
        //writes audiodata to a chatmessage object and sends it to the server
        
        byte[] audiodata = out.toByteArray();
        try {
            
            System.out.println("Client Writing audio to server..");
            ChatMessage m = new ChatMessage(ChatMessage.MessageType.AUDIO, this.USERNAME, audiodata);
            
            this.out.writeObject(m);
            this.out.flush();
            
        } catch (IOException ex) {
            System.out.println("Couldn't write message to server! (Is connection down?)");
            this.CONNECTED = false;
        }
        
    }
    
    @Override
    public void run()
    {
        //thread.run() override
        //try recieve incoming message objects
        
        try
        {
            while(!exit)
            {
                ChatMessage msg = (ChatMessage) this.in.readObject();
                if(msg.TYPE == ChatMessage.MessageType.AUDIO)
                {
                    AudioRecieved(msg);
                }
                else
                {
                   MessageRecieved(msg); 
                }
                
            }
            
        }
        catch(IOException e)
        {
            this.CONNECTED = false;
            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public void MessageRecieved(ChatMessage msg)
    {
        //tell the listener (The form that created me) I got a message
        if (this.Listener != null) 
        {
            this.Listener.MessageRecieved(msg);
        }
    }
    
    public void AudioRecieved(ChatMessage msg)
    {
        //we recieved audio, inform the event listener
        if (this.AudioListener != null) 
        {
            this.AudioListener.AudioRecieved(msg);
        }
        
    }
    
    public void UpdateUsername(String newname)
    {
        this.USERNAME = newname;
    }
    
    
    public synchronized void Exit()
    {
        //shutdown the thread safely
        //sync'd to prevent doubling
        this.exit = false;
    }
    

   
    
    
}
