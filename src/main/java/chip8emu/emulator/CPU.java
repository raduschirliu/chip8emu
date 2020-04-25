package chip8emu.emulator;

import java.util.Random;

public class CPU {
	private short pc;
	private short sp;
	private short stack[];
	private short iRegister;
	private short memory[];
	private short registers[];
	private short opcode;
	private boolean display[][];
	private int delayTimer, soundTimer;
	private OpcodeHandler opcodeHandlers[];
	private Random random;
	
	public CPU() {
		random = new Random();
		stack = new short[16];
		memory = new short[4096];
		registers = new short[16];
		display = new boolean[64][32];
		pc = 200;
		sp = 0;
		
		loadOpcodeHandlers();
		loadROM();
		run();
	}
	
	private void loadOpcodeHandlers() {
		opcodeHandlers = new OpcodeHandler[] {
			// 0xxx
			() -> {
				int lastBit = opcode & 0x000f;
				
				if (lastBit == 0x0) {
					// CLS
					// TODO
				} else if (lastBit == 0xe) {
					// RET
					pc = stack[sp];
					sp--;
				}
			},
			
			// 1xxx
			() -> {
				// JMP
				pc = (short)(opcode & 0x0fff);
			},
			
			// 2xxx
			() -> {
				// CALL addr
				sp++;
				stack[sp] = pc;
				pc = (short)(opcode & 0x0fff);
			},
			
			// 3xxx
			() -> {
				// SE Vx, byte
				int reg = opcode & 0x0f00;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] == value) {
					pc += 2;
				}
			},
			
			// 4xxx
			() -> {
				// SNE Vx, byte
				int reg = opcode & 0x0f00;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] != value) {
					pc += 2;
				}
			},
			
			// 5xxx
			() -> {
				// SE Vx, Vy
				int regX = opcode & 0x0f00;
				int regY = opcode & 0x00f0;
				
				if (registers[regX] == registers[regY]) {
					pc += 2;
				}
			},
			
			// 6xxx
			() -> {
				// LD Vx, byte
				int reg = opcode & 0x0f00;
				short val = (short)(opcode & 0x00ff);
				registers[reg] = val;
			},
			
			// 7xxx
			() -> {
				// ADD Vx, byte
				int reg = opcode & 0x0f00;
				short val = (short)(opcode & 0x00ff);
				registers[reg] += val;
			},
			
			// 8xxx
			() -> {
				// TODO
			},
			
			// 9xxx
			() -> {
				// SNE Vx, Vy
				int regX = opcode & 0xf000;
				int regY = opcode & 0x0f00;
				
				if (registers[regX] != registers[regY]) {
					pc += 2;
				}
			},
			
			// Axxx
			() -> {
				// LD I, addr
				iRegister = (short)(opcode & 0x0fff);
			},
			
			// Bxxx
			() -> {
				// JP V0, addr
				short offset = (short)(opcode & 0x0fff);
				pc = (short)(registers[0] + offset);
			},
			
			// Cxxx
			() -> {
				// RND Vx, byte
				int reg = opcode & 0x0f00;
				short val = (short)(opcode & 0x00ff);
				short rand = (short)random.nextInt(256);
				
				registers[reg] = (short)(reg & val);
			},
			
			// Dxxx
			() -> {
				// DRW Vx, Vy, nibble
				int size = opcode & 0x000f;
				int xPos = opcode & 0x0f00;
				int yPos = opcode & 0x00f0;
				
				// TODO
			},
			
			
			// Exxx
			() -> {
				// TODO
			},
			
			// Fxxx
			() -> {
				// TODO
			}
		};
	}
	
	private void loadROM() {
		ROMReader reader = new ROMReader("roms/pong.ch8");
		
		short data;
		int i = 200;
		
		while ((data = reader.nextByte()) != -1) {
			memory[i] = data;
			i++;
		}
		
		reader.close();
		System.out.println("Loaded ROM");
	}
	
	public void run() {
		opcode = memory[pc];
		opcode <<= 8;
		opcode += memory[pc + 1];
		
		int leading = (opcode >> 12) & 0xf;
		opcodeHandlers[leading].run();
		
		pc += 2;
	}
}
