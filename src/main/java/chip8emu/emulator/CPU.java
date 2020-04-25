package chip8emu.emulator;

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
	
	public CPU() {
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
				int lastBit = opcode & 0b0001;
				
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
				pc = (short)(opcode & 0b0111);
			},
			
			// 2xxx
			() -> {
				// CALL addr
				sp++;
				stack[sp] = pc;
				pc = (short)(opcode & 0b0111);
			},
			
			// 3xxx
			() -> {
				// SE Vx, byte
				int reg = opcode & 0b0100;
				short value = (short)(opcode & 0b0011);
				
				if (registers[reg] == value) {
					pc += 2;
				}
			},
			
			// 4xxx
			() -> {
				// SNE Vx, byte
				int reg = opcode & 0b0100;
				short value = (short)(opcode & 0b0011);
				
				if (registers[reg] != value) {
					pc += 2;
				}
			},
			
			// 5xxx
			() -> {
				// SE Vx, Vy
				int regX = opcode & 0b0100;
				int regY = opcode & 0b0010;
				
				if (registers[regX] == registers[regY]) {
					pc += 2;
				}
			},
			
			// 6xxx
			() -> {
				// LD Vx, byte
				int reg = opcode & 0b0100;
				short val = (short)(opcode & 0x0011);
				registers[reg] = val;
			},
			
			// 7xxx
			() -> {
				// ADD Vx, byte
				int reg = opcode & 0b0100;
				short val = (short)(opcode & 0x0011);
				registers[reg] += val;
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
		opcodeHandlers[leading - 1].run();
		
		pc += 2;
	}
}
