/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChatClient;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat;
import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.audio.ContinuousAudioDataStream;


/**
 *
 * @author K00202323
 * 
 * Background thread to handle hooks to the Connection Audio Input device
 * Captures audio as a byte[] stream and sends it to the server via Client
 */
public class Mic extends Thread{
    
    protected Boolean stopped = false;
    
    protected BufferedOutputStream AudioOut;
    
    protected Client clientParent;
    
    public Mic(Client c)
    {
        stopped = true;
        clientParent = c;
    }
    
//    public Mic(String addr, int port)
//    {
//        stopped = true;
//        try{
//            AudioSocket = new Socket(addr, port);
//            AudioOut = new BufferedOutputStream(AudioSocket.getOutputStream());
//        }
//        catch(IOException e)
//        {
//            System.out.println("AudioSocket couldn't connect!");
//        }
//        
//    }
    
    @Override
    public void run()
    {
        this.stopped = false;
        this.Capture();
    }
    
    public void Capture()
    {
        while(!stopped)
        {
            
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            TargetDataLine microphone;
            //SourceDataLine speakers;
            try {
                microphone = AudioSystem.getTargetDataLine(format);

                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int numBytesRead;
                int CHUNK_SIZE = 1024;
                byte[] data = new byte[microphone.getBufferSize() / 5];
                microphone.start();

                int bytesRead = 0;
//                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
//                speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
//                speakers.open(format);
//                speakers.start();
                while (bytesRead < 100000 && !stopped) {
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead += numBytesRead;
                    // write the mic data to a stream for use later
                    out.write(data, 0, numBytesRead);
                    
                    //write audio bytes to server
                    //AudioOut.write(data, 0, numBytesRead);
                    
                    
                    // write mic data to stream for immediate playback
                    //speakers.write(data, 0, numBytesRead);
                }
                //speakers.drain();
                //speakers.close();
                microphone.close();
                
                System.out.println("Mic Sending Audio To Client..");
                this.clientParent.WriteAudio(out);
                
            } catch (LineUnavailableException e) {
                System.out.println(e.getMessage());
            }
        }     
    }
    
    public synchronized void EndCapture()
    {
        //stop the thread (and thus recording)
        this.stopped = true;
        this.interrupt();
    }
}
