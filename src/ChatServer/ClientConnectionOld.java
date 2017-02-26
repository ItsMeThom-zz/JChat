/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatServer;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;



/**
 *
 * @author K00202323
 */
public class ClientConnectionOld extends Thread{
    
    private Socket Socket; // A connected socket
    private DataInputStream in;
    private DataOutputStream out;
    public volatile Boolean Running;
     
    public ClientConnectionOld(Socket socket)  {
        Socket = socket;
        
        try   {
            
            //get in
            in = new DataInputStream(Socket.getInputStream());
            
            //get out
            out = new DataOutputStream(Socket.getOutputStream());
           
            //shake it all about :p 
            Running = true;
        }
        
        catch(IOException ex) {
            System.err.println(ex);
        }
        
       
        
    }

     @Override
    public void run() {
        try {
            while(true)
            {
                

             String msg = this.in.readUTF();
             System.out.println(msg);

             

            }
        } catch (IOException ex) {
            
        }
        

    }
    
    public void Quit()
    {
        Running = false;
    }
    
}
