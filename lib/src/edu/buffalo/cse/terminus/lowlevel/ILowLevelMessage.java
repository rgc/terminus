package edu.buffalo.cse.terminus.lowlevel;

public interface ILowLevelMessage
{
	public byte[] getBytes();
	public void loadFromBytes(byte[] msg);
}
