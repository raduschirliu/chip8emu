package chip8emu;

import chip8emu.emulator.CPU;
import chip8emu.gui.Display;

public class App {
	public static void main(String[] args) {
		CPU cpu = new CPU();
		cpu.loadROM(args[0]);
		new Display(cpu);
	}
}
