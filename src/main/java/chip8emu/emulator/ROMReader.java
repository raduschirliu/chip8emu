package chip8emu.emulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ROMReader {
	private InputStream input;
	
	public ROMReader(String filePath) {
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
}
