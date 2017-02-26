
package chatassignment;

import java.io.Serializable;

/**
 *
 * @author K00202323
 * 
 * ChatMessage is a container for messages sent between Client-Server
 * Contains users name, message type and message data
 */


public class ChatMessage implements Serializable{
    //types of message objects
    public static enum MessageType {MESSAGE, CONNECT, DISCONNECT, AUDIO, NAMECHANGE}
    
    
    public MessageType TYPE;
    
    //stores either string bytes, or audio bytes
    public byte[] Message;
    public String User;
    
    
    public ChatMessage(MessageType t, String user, String msg)
    {
        this.TYPE = t;
        this.User = user;
        this.Message = msg.getBytes();
    }
    
    public ChatMessage(MessageType t, String user, byte[] bytes)
    {
        this.TYPE = t;
        this.User = user;
        this.Message = bytes;
    }
    
    public String GetMessage()
    {
        //get the message prop as a string (its text)
        return new String(this.Message);
    }
    
    public byte[] GetAudioData()
    {
        //get raw message bytes (its audio)
        return this.Message;
    }
}
