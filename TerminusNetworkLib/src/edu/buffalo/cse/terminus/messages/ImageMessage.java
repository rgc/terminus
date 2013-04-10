package edu.buffalo.cse.terminus.messages;

public class ImageMessage extends TerminusMessage 
{
	protected byte[] image;
	
	public ImageMessage() 
	{
		this("");
	}

	public ImageMessage(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_IMAGE;
	}
	
	public byte[] getImage()
	{
		return this.image;
	}
	
	public void setImage(byte[] img)
	{
		this.image = img;
	}
}
