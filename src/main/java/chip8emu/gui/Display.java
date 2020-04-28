package chip8emu.gui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

import chip8emu.emulator.CPU;

public class Display {
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	
	private CPU cpu;
	private long window;
	private boolean pixels[][];
	private Map<Integer, Integer> keyMap;
	
	public Display(CPU cpu) {
		this.cpu = cpu;
		cpu.setDisplay(this);
		
		GLFWErrorCallback.createPrint(System.err).set();
		
	    if (!GLFW.glfwInit()) {
	        System.err.println("GLFW failed to initialize");
	        return;
	    }
	
	    GLFW.glfwDefaultWindowHints();
	    
	    window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "CHIP-8 Emulator", 0, 0);
	    pixels = new boolean[64][32];
	    createKeyMaps();
	    
	    GLFW.glfwSetKeyCallback(window, GLFWKeyCallback.create((window, key, scanCode, action, mods) -> {
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
	    
	    GLFW.glfwDestroyWindow(window);
	    GLFW.glfwTerminate();
	    GLFW.glfwSetErrorCallback(null).free();
	}
	
	public void run() {
		GL.createCapabilities();
		
		glClearColor(0f, 0f, 0f, 0f);
		glMatrixMode(GL_PROJECTION);
		glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glDisable(GL_TEXTURE_2D);
		
		while (!GLFW.glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			GLFW.glfwPollEvents();
			glLoadIdentity();
			
			cpu.step();
			
			for (int i = 0; i < pixels.length; i++) {
				for (int j = 0; j < pixels[i].length; j++) {
					if (pixels[i][j]) {
						drawPixel(i, j);
					}
				}
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
	
	public void clear() {
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[i].length; j++) {
				pixels[i][j] = false;
			}
		}
	}
	
	public void setPixel(int x, int y, boolean state) {
		x %= 65;
		y %= 33;
		
		pixels[x][y] = state;
	}
	
	public boolean drawByte(int x, int y, short data) {
		boolean collision = false;
		
		for (int i = 0; i < 8; i++) {
			int bit = data & 0b1;
			int curBit = pixels[x][y] ? 1 : 0;
			int newBit = bit ^ curBit;
			
			
			setPixel(x, y, newBit == 1);
			collision = (newBit == 0 && curBit == 1) || collision;
			data >>= 1;
		}
		
		return collision;
	}
	
	private void drawPixel(int x, int y) {
		glColor3f(1f, 1f, 1f);
		
		float xPos = x * WIDTH / 64f;
		float yPos = y * HEIGHT / 32f;
		
		glBegin(GL_QUADS);
		glVertex2f(xPos, yPos);
		glVertex2f(xPos + 64, yPos);
		glVertex2f(xPos + 64, yPos + 32);
		glVertex2f(xPos, yPos + 32);
		glEnd();
	}
}
