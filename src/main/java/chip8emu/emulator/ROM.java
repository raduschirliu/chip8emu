package chip8emu.emulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ROM {
	private String filePath;
	private InputStream input;
	
	public ROM(String filePath) {
		this.filePath = filePath;
		
		try {
			input = new FileInputStream(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public short nextByte() {
		try {
			return (short)input.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void close() {
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getFilePath() {
		return filePath;
	}
}
