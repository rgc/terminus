package edu.buffalo.cse.terminus.messages;

public class AlertMessage extends TerminusMessage 
{
	/*
	 * We can expand this if needed.
	 */
	public static final int ALERT_EVENT = 0;
	
	protected int alertType = ALERT_EVENT;
	protected String location = "";
	protected String nickname = "";
	protected String url = "";
	
	public AlertMessage() 
	{
		super();
		this.type = TerminusMessage.MSG_ALERT;
	}
	
	public AlertMessage(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_ALERT;
	}
	
	public int getAlertType()
	{
		return this.alertType;
	}
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String l)
	{
		location = l;
	}
	
	public String getNickname()
	{
		return nickname;
	}
	
	public void setNickname(String n)
	{
		nickname = n;
	}
	
	public String getURL()
	{
		return url;
	}
	
	public void setURL(String u)
	{
		url = u;
	}
}
