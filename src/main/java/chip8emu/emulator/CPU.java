package chip8emu.emulator;

import java.util.Random;

import chip8emu.gui.Display;

public class CPU {
	private short pc;
	private short sp;
	private short stack[];
	private short iRegister, vfRegister;
	private short memory[];
	private short registers[];
	private short opcode;
	private boolean awaitingKey;
	private boolean keys[];
	private int delayTimer, soundTimer;
	private OpcodeHandler opcodeHandlers[];
	private Random random;
	private Display display;
	
	public CPU() {
		random = new Random();
		stack = new short[16];
		memory = new short[4096];
		registers = new short[16];
		keys = new boolean[16];
		awaitingKey = false;
		pc = 200;
		sp = -1;
		
		loadSprites();
		loadOpcodeHandlers();
		loadROM();
	}
	
	public void keyPressed(int keyCode, boolean pressed) {
		keys[keyCode] = pressed;
	}
	
	public void step() {
		if (!awaitingKey) {
			opcode = memory[pc];
			opcode <<= 8;
			opcode += memory[pc + 1];
			
			System.out.println(String.format("%x", opcode));
			
			int leading = (opcode >> 12) & 0xf;
			opcodeHandlers[leading].run();
			
			if (delayTimer > 0) {
				delayTimer--;
			}
			
			if (soundTimer > 0) {
				soundTimer--;
			}
			
			pc += 2;
		}
	}
	
	public void setDisplay(Display display) {
		this.display = display;
	}
	
	private void saveSprite(int startAddress, short[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			memory[startAddress + i] = bytes[i];
		}
	}
	
	private void loadSprites() {
		// "0"
		saveSprite(0, new short[] { 0xf0, 0x90, 0x90, 0x90, 0xf0 });
		
		// "1"
		saveSprite(5, new short[] { 0x20, 0x60, 0x20, 0x20, 0x70 });
		
		// "2"
		saveSprite(10, new short[] { 0xf0, 0x10, 0xf0, 0x80, 0xf0 });
		
		// "3"
		saveSprite(15, new short[] { 0xf0, 0x10, 0xf0, 0x10, 0xf0 });
		
		// "4"
		saveSprite(20, new short[] { 0x90, 0x90, 0xf0, 0x10, 0x10 });
		
		// "5"
		saveSprite(25, new short[] { 0xf0, 0x80, 0xf0, 0x10, 0xf0 });
		
		// "6"
		saveSprite(30, new short[] { 0xf0, 0x80, 0xf0, 0x90, 0xf0 });
		
		// "7"
		saveSprite(35, new short[] { 0xf0, 0x10, 0x20, 0x40, 0x40 });
		
		// "8"
		saveSprite(40, new short[] { 0xf0, 0x90, 0xf0, 0x90, 0xf0 });
		
		// "9"
		saveSprite(45, new short[] { 0xf0, 0x90, 0xf0, 0x10, 0xf0 });
		
		// "A"
		saveSprite(50, new short[] { 0xf0, 0x90, 0xf0, 0x90, 0x90 });
		
		// "B"
		saveSprite(55, new short[] { 0xe0, 0x90, 0xe0, 0x90, 0xe0 });
		
		// "C"
		saveSprite(60, new short[] { 0xf0, 0x80, 0x80, 0x80, 0xf0 });
		
		// "D"
		saveSprite(65, new short[] { 0xe0, 0x90, 0x90, 0x90, 0xe0 });
		
		// "E"
		saveSprite(70, new short[] { 0xf0, 0x80, 0xf0, 0x80, 0xf0 });
		
		// "F"
		saveSprite(75, new short[] { 0xf0, 0x80, 0xf0, 0x80, 0x80 });
	}
	
	private void loadOpcodeHandlers() {
		opcodeHandlers = new OpcodeHandler[] {
			// 0xxx
			() -> {
				int lastBit = opcode & 0x000f;
				
				if (lastBit == 0x0) {
					display.clear();
				} else if (lastBit == 0xe) {
					// RET
					pc = stack[sp];
					sp--;
				} else {
					System.err.println(String.format("Unknown opcode: %x", opcode));
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
				int reg = (opcode & 0x0f00) >> 8;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] == value) {
					pc += 2;
				}
			},
			
			// 4xxx
			() -> {
				// SNE Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] != value) {
					pc += 2;
				}
			},
			
			// 5xxx
			() -> {
				// SE Vx, Vy
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				
				if (registers[regX] == registers[regY]) {
					pc += 2;
				}
			},
			
			// 6xxx
			() -> {
				// LD Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				registers[reg] = val;
			},
			
			// 7xxx
			() -> {
				// ADD Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				registers[reg] += val;
			},
			
			// 8xxx
			() -> {
				int type = opcode & 0x000f;
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				
				switch (type) {
				case 0:
					// LD Vx, Vy
					registers[regX] = registers[regY];
					break;
				case 1:
					// OR Vx, Vy
					registers[regX] = (short)(registers[regX] | registers[regY]);
					break;
				case 2:
					// AND Vx, Vy
					registers[regX] = (short)(registers[regX] & registers[regY]);
					break;
				case 3:
					// XOR Vx, Vy
					registers[regX] = (short)(registers[regX] ^ registers[regY]);
					break;
				case 4:
					// ADD Vx, Vy
					int res = registers[regX] + registers[regY];
					vfRegister = (short)(res > 255 ? 1 : 0);
					
					registers[regX] = (short)res;
					break;
				case 5:
					// SUB Vx, Vy
					vfRegister = (short)(registers[regX] > registers[regY] ? 1 : 0);
					registers[regX] -= registers[regY];
					break;
				case 6:
					// SHR Vx {, Vy }
					int lsb = registers[regX] & 0b1;
					vfRegister = (short)lsb;
										
					registers[regX] /= 2;
					break;
				case 7:
					// SUBN Vx, Vy
					vfRegister = (short)(registers[regY] > registers[regX] ? 1 : 0);
					registers[regX] = (short)(registers[regY] - registers[regX]);
					break;
				case 0xe:
					// SHL Vx {, Vy }
					int msb = (registers[regX] >> 15);
					vfRegister = (short)msb;
					
					registers[regX] *= 2;
					break;
				default:
					System.err.println(String.format("Unknown opcode: %x", opcode));
					break;
				}
			},
			
			// 9xxx
			() -> {
				// SNE Vx, Vy
				int regX = (opcode & 0xf000) >> 12;
				int regY = (opcode & 0x0f00) >> 8;
				
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
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				short rand = (short)random.nextInt(256);
				
				registers[reg] = (short)(reg & val);
			},
			
			// Dxxx
			() -> {
				// DRW Vx, Vy, nibble
				int size = opcode & 0x000f;
				int xPos = (opcode & 0x0f00) >> 8;
				int yPos = (opcode & 0x00f0) >> 4;
				
				// TODO
				for (int i = 0; i < size; i++) {
					short data = memory[iRegister + i];
				}
			},
			
			
			// Exxx
			() -> {
				int key = (opcode & 0x0f00) >> 8;
				int instr = opcode & 0x00ff;
				
				if (instr == 0x9e) {
					// SKP Vx
					if (keys[key]) {
						pc += 2;
					}
				} else if (instr == 0xa1) {
					// LD Vx, DT
					if (!keys[key]) {
						pc += 2;
					}
				} else {
					System.err.println(String.format("Unknown opcode: %x", opcode));
				}
			},
			
			// Fxxx
			() -> {
				int reg = (opcode & 0x0f00) >> 8;
				int type = opcode & 0x00ff;
				
				switch (type) {
				case 0x07:
					// LD Vx, DT
					registers[reg] = (short)delayTimer;
					break;
				case 0x0a:
					// LD Vx, k
					// TODO
					break;
				case 0x15:
					// LD DT, Vx
					delayTimer = registers[reg];
					break;
				case 0x18:
					// LD ST, Vx
					soundTimer = registers[reg];
					break;
				case 0x1e:
					// ADD I, Vx
					iRegister += registers[reg];
					break;
				case 0x29:
					// LD F, Vx
					// TODO
					break;
				case 0x33:
					// LD B, Vx
					// TODO
					break;
				case 0x55:
					// LD [I], Vx
					// TODO
					break;
				case 0x65:
					// LD Vx, [I]
					// TODO
					break;
				default:
					System.err.println(String.format("Unknown opcode: %x", opcode));
					break;
				}
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
}
