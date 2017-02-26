
package ChatClient;

import chatassignment.AudioListener;
import chatassignment.ChatMessage;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author thoml
 * 
 * A background thread that hooks to the connected Audio Output device to
 * play incoming audio messages from the server
 */
public class Speaker extends Thread implements AudioListener{
    
    protected byte[] AudioData;
    
    protected Boolean stopped = false;
    protected SourceDataLine speakers;
    protected AudioFormat format;
    public Speaker()
    {
        format = new AudioFormat(8000.0f, 16, 1, true, true);
        
        try {
            
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            speakers.open(format);
            speakers.start();
            
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Speaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
        
    
    @Override
    public void run()
    {
        //Speakrer thread waits until closed
        // can be passed audio to play with Playback

        while(!stopped)
        {
            
        } 
    }
    
    public void Playback(byte[] AudioData)
    {
        //Playback AudioData on the loaded Speaker Line
        
        System.out.println("Speakers playing audio data..");
        int numBytesRead = AudioData.length;
        //int CHUNK_SIZE = 1024;
        int bytesRead = 0;
        //while (bytesRead < 100000 && !stopped) {
            // write data to speaker stream for immediate playback
            System.out.println("Playing.." + bytesRead);
            speakers.write(AudioData, 0, numBytesRead);
            
        //}
        speakers.drain(); //blocking - drains all bytes to speakers before thread can be quit
        
        
    }
    
    public void End()
    {
        this.stopped = true;
        speakers.close();
    }

    @Override
    public void AudioRecieved(ChatMessage msg) {
        //do playback of the messages audio data byte stream
        this.Playback(msg.GetAudioData());
    }
    
}
