package chip8emu;

import chip8emu.emulator.CPU;
import chip8emu.gui.Display;

public class App {
	public static void main(String[] args) {
		CPU cpu = new CPU();
		
		if (args.length > 0) {
			cpu.loadROM(args[0]);
		} else {
			System.out.println("Press TAB to select a ROM to load");
		}
		
		new Display(cpu);
	}
}
