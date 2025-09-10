package de.luisagrether.util;

import java.io.OutputStream;

/**
 * A string output stream writes the data to a {@linkplain java.lang.String}.
 * 
 * @author LuisaGrether
 */
public class StringOutputStream extends OutputStream {
	
	private StringBuilder builder = new StringBuilder();
	
	/**
	 * Writes the specified byte to this output stream.
	 * 
	 * @param data the data
	 */
	public void write(int data) {
		builder.append((char)data);
	}
	
	/**
	 * Returns a {@linkplain java.lang.String} cotaining the data written to this output stream.
	 * 
	 * @return a string containing the data
	 */
	public String toString() {
		return builder.toString();
	}
	
}
