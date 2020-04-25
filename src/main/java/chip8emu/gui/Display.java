package chip8emu.gui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;

import chip8emu.emulator.CPU;

public class Display {
	private CPU cpu;
	private long window;
	private Map<Integer, Integer> keyMap;
	
	public Display(CPU cpu) {
		this.cpu = cpu;
		
		GLFWErrorCallback.createPrint(System.err).set();
		
	    if (!GLFW.glfwInit()) {
	        System.err.println("wtf");
	        return;
	    }
	
	    GLFW.glfwDefaultWindowHints();
	    
	    window = GLFW.glfwCreateWindow(640, 480, "CHIP-8 Emulator", 0, 0);
	    GLFW.glfwMakeContextCurrent(window);
	    GLFW.glfwSwapInterval(1);
	    GLFW.glfwShowWindow(window);
	    
	    createKeyMaps();
	    GLFW.glfwSetKeyCallback(window, GLFWKeyCallback.create((window, key, scanCode, action, mods) -> {
	    	if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_RELEASE) {
	    		if (keyMap.containsKey(key)) {
	    			cpu.keyPressed(keyMap.get(key), action == GLFW.GLFW_PRESS);
	    		}
	    	}
	    }));
	    
	    run();
	}
	
	public void run() {
		GL.createCapabilities();
		
		while (!GLFW.glfwWindowShouldClose(window)) {
			cpu.step();
			GLFW.glfwSwapBuffers(window);
	        GLFW.glfwPollEvents();
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
}
