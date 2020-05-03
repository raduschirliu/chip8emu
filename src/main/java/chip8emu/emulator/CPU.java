package chip8emu.emulator;

import java.awt.Toolkit;
import java.util.Random;

import chip8emu.gui.Display;

public class CPU {
	private short pc;
	private short sp;
	private short stack[];
	private short iRegister;
	private short memory[];
	private short digitLocations[];
	private short registers[];
	private short opcode;
	private int delayTimer, soundTimer;
	private int keyRegister;
	private boolean awaitingKey;
	private boolean keys[];
	private boolean running;
	private OpcodeHandler opcodeHandlers[];
	private Random random;
	private ROM activeROM;
	private Display display;
	
	public CPU() {
		random = new Random();
		running = true;
		
		loadOpcodeHandlers();
	}
	
	public void reset() {
		stack = new short[32];
		registers = new short[16];
		digitLocations = new short[16];
		keys = new boolean[16];
		memory = new short[4096];
		awaitingKey = false;
		
		pc = 0x200;
		sp = -1;
		
		loadSprites();
		
		if (display != null) {
			display.clear();
		}
	}
	
	public void loadROM(String filePath) {
		reset();
		activeROM = new ROM(filePath);
		
		short data;
		int i = 0x200;
		
		while ((data = activeROM.nextByte()) != -1) {
			memory[i] = data;
			i++;
		}
		
		activeROM.close();
		System.out.println("Loaded ROM: " + activeROM.getFilePath());
	}
	
	public void keyPressed(int keyCode, boolean pressed) {
		if (pressed) {
			awaitingKey = false;
			
			if (keyRegister >= 0) {
				registers[keyRegister] = (short)keyCode;
				keyRegister = -1;
			}
		}
		
		keys[keyCode] = pressed;
	}
	
	public void step() {
		if (!awaitingKey) {
			opcode = memory[pc];
			opcode <<= 8;
			opcode += memory[pc + 1];
			
			int leading = (opcode >> 12) & 0xf;
			opcodeHandlers[leading].run();
			
			if (delayTimer > 0) {
				delayTimer--;
			}
			
			if (soundTimer > 0) {
				soundTimer--;
				Toolkit.getDefaultToolkit().beep();
			}
			
			pc += 2;
		}
	}
	
	public void toggleRunning() {
		running = !running;
	}
	
	public boolean getRunning() {
		return running;
	}
	
	public void setDisplay(Display display) {
		this.display = display;
	}
	
	public short getOpcode() {
		return opcode;
	}
	
	public short getPC() {
		return pc;
	}
	
	public short getSP() {
		return sp;
	}
	
	public short getI() {
		return iRegister;
	}
	
	public short[] getRegisters() {
		return registers;
	}
	
	public short[] getMemory() {
		return memory;
	}
	
	public short[] getStack() {
		return stack;
	}
	
	public boolean[] getKeys() {
		return keys;
	}
	
	public int getDelayTimer() {
		return delayTimer;
	}
	
	public int getSoundTimer() {
		return soundTimer;
	}
	
	private void saveSprite(int digit, int startAddress, short[] bytes) {
		digitLocations[digit] = (short)startAddress;
		
		for (int i = 0; i < bytes.length; i++) {
			memory[startAddress + i] = bytes[i];
		}
	}
	
	private void loadSprites() {
		saveSprite(0x0, 0, new short[] { 0xf0, 0x90, 0x90, 0x90, 0xf0 });
		saveSprite(0x1, 5, new short[] { 0x20, 0x60, 0x20, 0x20, 0x70 });
		saveSprite(0x2, 10, new short[] { 0xf0, 0x10, 0xf0, 0x80, 0xf0 });
		saveSprite(0x3, 15, new short[] { 0xf0, 0x10, 0xf0, 0x10, 0xf0 });
		saveSprite(0x4, 20, new short[] { 0x90, 0x90, 0xf0, 0x10, 0x10 });
		saveSprite(0x5, 25, new short[] { 0xf0, 0x80, 0xf0, 0x10, 0xf0 });
		saveSprite(0x6, 30, new short[] { 0xf0, 0x80, 0xf0, 0x90, 0xf0 });
		saveSprite(0x7, 35, new short[] { 0xf0, 0x10, 0x20, 0x40, 0x40 });
		saveSprite(0x8, 40, new short[] { 0xf0, 0x90, 0xf0, 0x90, 0xf0 });
		saveSprite(0x9, 45, new short[] { 0xf0, 0x90, 0xf0, 0x10, 0xf0 });
		saveSprite(0xa, 50, new short[] { 0xf0, 0x90, 0xf0, 0x90, 0x90 });
		saveSprite(0xb, 55, new short[] { 0xe0, 0x90, 0xe0, 0x90, 0xe0 });
		saveSprite(0xc, 60, new short[] { 0xf0, 0x80, 0x80, 0x80, 0xf0 });
		saveSprite(0xd, 65, new short[] { 0xe0, 0x90, 0x90, 0x90, 0xe0 });
		saveSprite(0xe, 70, new short[] { 0xf0, 0x80, 0xf0, 0x80, 0xf0 });
		saveSprite(0xf, 75, new short[] { 0xf0, 0x80, 0xf0, 0x80, 0x80 });
	}
	
	private void loadOpcodeHandlers() {
		opcodeHandlers = new OpcodeHandler[] {
			// 0???
			() -> {
				int type = opcode & 0x00ff;
				
				if (type == 0xe0) {
					// CLS
					display.clear();
				} else if (type == 0xee) {
					// RET
					pc = stack[sp];
					sp--;
				} else {
					System.err.println(String.format("Unknown opcode: %x", opcode));
				}
			},
			
			// 1nnn
			() -> {
				// JMP
				pc = (short)(opcode & 0x0fff);
				pc -= 2;
			},
			
			// 2nnn
			() -> {
				// CALL addr
				sp++;
				stack[sp] = pc;
				pc = (short)(opcode & 0x0fff);
				pc -= 2;
			},
			
			// 3xkk
			() -> {
				// SE Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] == value) {
					pc += 2;
				}
			},
			
			// 4xkk
			() -> {
				// SNE Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short value = (short)(opcode & 0x00ff);
				
				if (registers[reg] != value) {
					pc += 2;
				}
			},
			
			// 5xy0
			() -> {
				// SE Vx, Vy
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				
				if (registers[regX] == registers[regY]) {
					pc += 2;
				}
			},
			
			// 6xkk
			() -> {
				// LD Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				registers[reg] = (short)(val % 256);
			},
			
			// 7xkk
			() -> {
				// ADD Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				registers[reg] = (short)((registers[reg] + val) % 256);
			},
			
			// 8xy?
			() -> {
				int type = opcode & 0x000f;
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				
				switch (type) {
				case 0x0:
					// LD Vx, Vy
					registers[regX] = registers[regY];
					break;
				case 0x1:
					// OR Vx, Vy
					registers[regX] = (short)(registers[regX] | registers[regY]);
					break;
				case 0x2:
					// AND Vx, Vy
					registers[regX] = (short)(registers[regX] & registers[regY]);
					break;
				case 0x3:
					// XOR Vx, Vy
					registers[regX] = (short)(registers[regX] ^ registers[regY]);
					break;
				case 0x4:
					// ADD Vx, Vy
					int res = registers[regX] + registers[regY];
					registers[15] = (short)(res > 255 ? 1 : 0);
					
					registers[regX] = (short)(res % 256);
					break;
				case 0x5:
					// SUB Vx, Vy
					registers[15] = (short)(registers[regX] > registers[regY] ? 1 : 0);
					registers[regX] = (short)((registers[regX] - registers[regY]) % 256);
					
					if (registers[regX] < 0) {
						registers[regX] = (short)(256 + registers[regX]);
					}
					break;
				case 0x6:
					// SHR Vx {, Vy }
					int lsb = registers[regX] & 0b1;
					registers[15] = (short)lsb;
										
					registers[regX] /= 2;
					break;
				case 0x7:
					// SUBN Vx, Vy
					registers[15] = (short)(registers[regY] > registers[regX] ? 1 : 0);
					registers[regX] = (short)((registers[regY] - registers[regX]) % 256);
					break;
				case 0xe:
					// SHL Vx {, Vy }
					int msb = (registers[regX] >> 15);
					registers[15] = (short)msb;
					
					registers[regX] = (short)((registers[regX] * 2) % 256);
					break;
				default:
					System.err.println(String.format("Unknown opcode: %x", opcode));
					break;
				}
			},
			
			// 9xy0
			() -> {
				// SNE Vx, Vy
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				
				if (registers[regX] != registers[regY]) {
					pc += 2;
				}
			},
			
			// Annn
			() -> {
				// LD I, addr
				iRegister = (short)(opcode & 0x0fff);
			},
			
			// Bnnn
			() -> {
				// JP V0, addr
				short offset = (short)(opcode & 0x0fff);
				pc = (short)(registers[0] + offset);
				pc -= 2;
			},
			
			// Cxkk
			() -> {
				// RND Vx, byte
				int reg = (opcode & 0x0f00) >> 8;
				short val = (short)(opcode & 0x00ff);
				short rand = (short)random.nextInt(256);
				
				registers[reg] = (short)(rand & val);
			},
			
			// Dxyn
			() -> {
				// DRW Vx, Vy, nibble
				int size = opcode & 0x000f;
				int regX = (opcode & 0x0f00) >> 8;
				int regY = (opcode & 0x00f0) >> 4;
				int xPos = registers[regX];
				int yPos = registers[regY];
				boolean collision = false;
				
				for (int i = 0; i < size; i++) {
					short data = memory[iRegister + i];
					collision = display.drawByte(xPos, yPos + i, data) || collision;
				}
				
				registers[15] = (short)(collision ? 1 : 0);
			},
			
			
			// Ex??
			() -> {
				int reg = (opcode & 0x0f00) >> 8;
				int instr = opcode & 0x00ff;
				int key = registers[reg];
				
				if (key > 15) {
					return;
				}
				
				if (instr == 0x9e) {
					// SKP Vx
					if (keys[key]) {
						pc += 2;
					}
				} else if (instr == 0xa1) {
					// SKNP Vx
					if (!keys[key]) {
						pc += 2;
					}
				} else {
					System.err.println(String.format("Unknown opcode: %x", opcode));
				}
			},
			
			// Fx??
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
					awaitingKey = true;
					keyRegister = reg;
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
					iRegister = digitLocations[registers[reg]];
					break;
				case 0x33:
					// LD B, Vx
					memory[iRegister] = (short)(registers[reg] / 100);
					memory[iRegister + 1] = (short)((registers[reg] / 10) % 10);
					memory[iRegister + 2] = (short)((registers[reg] % 100) % 10);
					break;
				case 0x55:
					// LD [I], Vx
					for (int i = 0; i <= reg; i++) {
						memory[iRegister + i] = registers[i];
					}
					
					break;
				case 0x65:
					// LD Vx, [I]
					for (int i = 0; i <= reg; i++) {
						registers[i] = memory[iRegister + i];
					}
					
					break;
				default:
					System.err.println(String.format("Unknown opcode: %x", opcode));
					break;
				}
			}
		};
	}
}
