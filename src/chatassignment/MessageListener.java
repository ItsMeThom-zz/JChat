/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatassignment;

import chatassignment.ChatMessage;

/**
 *
 * @author K00202323
 * 
 * Inteface to handle message recieved events, sents message from Client to ClientGUI
 */
public interface MessageListener {
    
    void MessageRecieved(ChatMessage msg);
}
