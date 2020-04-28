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
		
		GLFWErrorCallback.createPrint(System.err).set();
		
	    if (!GLFW.glfwInit()) {
	        System.err.println("GLFW failed to initialize");
	        return;
	    }
	
	    GLFW.glfwDefaultWindowHints();
	    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
	    
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
		
		while (!GLFW.glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			GLFW.glfwPollEvents();
			glLoadIdentity();
			
			cpu.step();
			drawRect(200, 200);
			
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
	
	public void setPixel(int x, int y, boolean state) {
		pixels[x][y] = state;
	}
	
	private void drawRect(int x, int y) {
		glColor3f(1f, 1f, 1f);
		
		int w = 100;
		int h = 100;
		
		glBegin(GL_QUADS);
		glVertex2f(x, y);
		glVertex2f(x + w, y);
		glVertex2f(x + w, y + h);
		glVertex2f(x, y + h);
		glEnd();
	}
}
