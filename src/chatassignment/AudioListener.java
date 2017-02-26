
package chatassignment;

/**
 *
 * @author thoml
 * 
 * Interface for sending AudioRecieved events to the ClientGUI from the Client
 * 
 */
public interface AudioListener {
    
    public void AudioRecieved(ChatMessage msg);
    
}
