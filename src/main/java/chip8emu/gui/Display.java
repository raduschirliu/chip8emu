package chip8emu.gui;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.FileDialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import chip8emu.emulator.CPU;

public class Display {
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	
	private long fps, totalFrames, lastCalc;
	private float pixelWidth, pixelHeight;
	private long window;
	private boolean pixels[][];
	private boolean isDebuggerOpen;
	private Map<Integer, Integer> keyMap;
	private CPU cpu;
	private DebuggerWindow debugger;
	
	public Display(CPU cpu) {
		this.cpu = cpu;
		cpu.setDisplay(this);
		
		isDebuggerOpen = false;
		pixelWidth = WIDTH / 64f;
		pixelHeight = HEIGHT / 32f;
		
		GLFWErrorCallback.createPrint(System.err).set();
		
	    if (!GLFW.glfwInit()) {
	        System.err.println("GLFW failed to initialize");
	        return;
	    }
	
	    GLFW.glfwDefaultWindowHints();
	    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL_FALSE);
	    
	    window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "CHIP-8 Emulator", 0, 0);
	    pixels = new boolean[64][32];
	    createKeyMaps();
	    
	    GLFW.glfwSetKeyCallback(window, GLFWKeyCallback.create((window, key, scanCode, action, mods) -> {
	    	if (action == GLFW.GLFW_PRESS) {
		    	if (key == GLFW.GLFW_KEY_GRAVE_ACCENT) {
		    		openDebugger();
		    	} else if (key == GLFW.GLFW_KEY_SPACE) {
		    		cpu.toggleRunning();
		    	} else if (key == GLFW.GLFW_KEY_TAB) {
		    		showROMPicker();
		    	}
	    	}
	    	
	    	if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
	    		if (keyMap.containsKey(key)) {
	    			cpu.keyPressed(keyMap.get(key), action == GLFW.GLFW_PRESS);
	    		}
	    	}
	    }));
	    
	    GLFW.glfwMakeContextCurrent(window);
	    GLFW.glfwSwapInterval(1);
	    GLFW.glfwShowWindow(window);
	    
	    run();
	    
	    if (debugger != null) {
	    	debugger.dispatchEvent(new WindowEvent(debugger, WindowEvent.WINDOW_CLOSING));
	    }
	    
	    GLFW.glfwDestroyWindow(window);
	    GLFW.glfwTerminate();
	    GLFW.glfwSetErrorCallback(null).free();
	    System.exit(0);
	}
	
	public void showROMPicker() {
		FileDialog dialog = new FileDialog((JFrame)null, "Select a ROM to load");
		dialog.setMode(FileDialog.LOAD);
		dialog.setVisible(true);
		File files[] = dialog.getFiles();
		
		if (files != null && files.length > 0) {
			cpu.loadROM(files[0].getAbsolutePath());
			
			if (debugger != null) {
				debugger.romUpdated();
			}
		}
	}
	
	public void openDebugger() {
		if (isDebuggerOpen) {
			return;
		}
		
		isDebuggerOpen = true;
		debugger = new DebuggerWindow(cpu, this);
		debugger.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isDebuggerOpen = false;
			}
		});
	}
	
	public void clear() {
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[i].length; j++) {
				pixels[i][j] = false;
			}
		}
	}
	
	public boolean drawByte(int x, int y, short data) {
		boolean collision = false;
		x %= 64;
		y %= 32;
		
		x = Math.abs(x);
		y = Math.abs(y);
		
		for (int i = 0; i < 8; i++) {
			int bit = data & 0b1;
			int curX = (x + 7 - i) % 64;
			int curBit = pixels[curX][y] ? 1 : 0;
			int newBit = bit ^ curBit;
			
			
			setPixel(x + 7 - i, y, newBit == 1);
			collision = (newBit == 0 && curBit == 1) || collision;
			data >>= 1;
		}
		
		return collision;
	}
	
	public long getFPS() {
		return fps;
	}
	
	private void run() {
		GL.createCapabilities();
		
		glClearColor(0f, 0f, 0f, 0f);
		glMatrixMode(GL_PROJECTION);
		glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glDisable(GL_TEXTURE_2D);
		totalFrames = 0;
		lastCalc = System.currentTimeMillis();
		
		while (!GLFW.glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			GLFW.glfwPollEvents();
			glLoadIdentity();
			
			if (cpu.getRunning()) {
				cpu.step();
			}
				
			for (int i = 0; i < pixels.length; i++) {
				for (int j = 0; j < pixels[i].length; j++) {
					if (pixels[i][j]) {
						drawPixel(i, j);
					}
				}
			}
			
			totalFrames++;
			
			if (System.currentTimeMillis() >= lastCalc + 1000) {
				fps = totalFrames;
				totalFrames = 0;
				lastCalc = System.currentTimeMillis();
			}
			
			if (debugger != null) {
				debugger.update();
			}
			
			GLFW.glfwSwapBuffers(window);
	    }
	}
	
	private void createKeyMaps() {
		keyMap = new HashMap<Integer, Integer>();
		
		keyMap.put(GLFW.GLFW_KEY_1, 0x1);
		keyMap.put(GLFW.GLFW_KEY_2, 0x2);
		keyMap.put(GLFW.GLFW_KEY_3, 0x3);
		keyMap.put(GLFW.GLFW_KEY_4, 0xc);
		
		keyMap.put(GLFW.GLFW_KEY_Q, 0x4);
		keyMap.put(GLFW.GLFW_KEY_W, 0x5);
		keyMap.put(GLFW.GLFW_KEY_E, 0x6);
		keyMap.put(GLFW.GLFW_KEY_R, 0xd);
		
		keyMap.put(GLFW.GLFW_KEY_A, 0x7);
		keyMap.put(GLFW.GLFW_KEY_S, 0x8);
		keyMap.put(GLFW.GLFW_KEY_D, 0x9);
		keyMap.put(GLFW.GLFW_KEY_F, 0xe);
		
		keyMap.put(GLFW.GLFW_KEY_Z, 0xa);
		keyMap.put(GLFW.GLFW_KEY_X, 0x0);
		keyMap.put(GLFW.GLFW_KEY_C, 0xb);
		keyMap.put(GLFW.GLFW_KEY_V, 0xf);
	}
	
	private void setPixel(int x, int y, boolean state) {
		x %= 64;
		y %= 32;
		pixels[x][y] = state;
	}
	
	private void drawPixel(int x, int y) {
		glColor3f(1f, 1f, 1f);
		
		float xPos = x * pixelWidth;
		float yPos = y * pixelHeight;
		
		glBegin(GL_QUADS);
		glVertex2f(xPos, yPos);
		glVertex2f(xPos + pixelWidth, yPos);
		glVertex2f(xPos + pixelWidth, yPos + pixelHeight);
		glVertex2f(xPos, yPos + pixelHeight);
		glEnd();
	}
}
