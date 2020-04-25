package chip8emu.gui;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import chip8emu.emulator.CPU;

public class Display {
	private CPU cpu;
	private long window;
	
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
	    
	    run();
	}
	
	public void run() {
		GL.createCapabilities();
		
		while (!GLFW.glfwWindowShouldClose(window)) {
			cpu.run();
			GLFW.glfwSwapBuffers(window);
	        GLFW.glfwPollEvents();
	    }
	}
}
