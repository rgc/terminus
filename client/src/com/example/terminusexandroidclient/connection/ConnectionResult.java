package com.example.terminusexandroidclient.connection;

public class ConnectionResult
{
	public enum ConnectionStatus
	{
		Success,
		UnknownHost,
		TimeOut,
		IOError
	}
	
	public ConnectionStatus status;
	public Exception exception;
	
	public ConnectionResult(ConnectionStatus status, Exception e)
	{
		this.status = status;
		this.exception = e;
	}
}