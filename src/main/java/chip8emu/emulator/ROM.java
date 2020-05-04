package chip8emu.emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

public class ROM {
	private String filePath;
	private String fileName;
	private InputStream input;
	
	public ROM(String filePath) {
		this.filePath = filePath;
		
		fileName = Paths.get(filePath).getFileName().toString();
		
		try {
			input = new FileInputStream(filePath);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "File '" + filePath + "' does not exist", "Error", JOptionPane.OK_OPTION);
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
	
	public String getFileName() {
		return fileName;
	}
}
