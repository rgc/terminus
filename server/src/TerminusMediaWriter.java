

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class TerminusMediaWriter
{
	IMediaWriter writer;
	long startTime;
    private String outputFilename;

    public TerminusMediaWriter(String filename, byte[] initImage)
    {
    	this(filename, createImageFromBytes(initImage));
    }
    
	public TerminusMediaWriter(String filename, BufferedImage initImage)
	{
		// we take an initial image to determine screen size
		
		outputFilename = "www/media/" + filename + ".mp4";
		
        // let's make a IMediaWriter to write the file.
        writer = ToolFactory.makeWriter(outputFilename);
  
        // one video stream, with id 0 at position 0
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 
                   initImage.getWidth()/2, initImage.getHeight()/2);

        //startTime = System.nanoTime();
        startTime = System.currentTimeMillis()/1000;

        updateMedia(initImage);
        
	}
	
	public void updateMedia(byte[] img) {
		updateMedia(createImageFromBytes(img));
	}
	
	public void updateMedia(BufferedImage screen) {

        // convert to the right image type
        BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);

        // encode the image to stream 0
        writer.encodeVideo(0, bgrScreen, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
	}
	
	public void writeMedia() {
        // tell the writer to close and write the media if  needed
		writer.close();
	}
	
	public String mediaPath() {
		return outputFilename;
	}
	
	public long mediaEpoch() {
		return startTime;
	}
	
	private static BufferedImage createImageFromBytes(byte[] imageData) {
	    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
	    try {
	        return ImageIO.read(bais);
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
    public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
        
        BufferedImage image;

        // if the source image is already the target type, return the source image
        if (sourceImage.getType() == targetType) {
            image = sourceImage;
        }
        // otherwise create a new image of the target type and draw the new image
        else {
            image = new BufferedImage(sourceImage.getWidth(), 
                 sourceImage.getHeight(), targetType);
            image.getGraphics().drawImage(sourceImage, 0, 0, null);
        }

        return image;
        
    }
     
}   