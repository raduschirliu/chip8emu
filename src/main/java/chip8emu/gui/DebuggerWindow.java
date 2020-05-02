package chip8emu.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import chip8emu.emulator.CPU;

public class DebuggerWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JLabel fpsLabel, pcLabel, spLabel, opcodeLabel, dtLabel, stLabel;
	private JPanel topPanel;
	private JTabbedPane tabs;
	private CPU cpu;
	private Display display;
	
	public DebuggerWindow(CPU cpu, Display display) {
		super("CHIP-8 Emulator Debugger");
		this.cpu = cpu;
		this.display = display;
		
		((JComponent)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());
		
		initTopPanel();
		initTabs();
		
		setSize(640, 480);
		setVisible(true);
	}
	
	public void update() {
		fpsLabel.setText("FPS: " + display.getFPS());
		pcLabel.setText(String.format("PC: %x", cpu.getPC()));
		spLabel.setText("SP : " + cpu.getSP());
		opcodeLabel.setText(String.format("Opcode: %x", cpu.getOpcode()));
		dtLabel.setText("Delay timer: " + cpu.getDelayTimer());
		stLabel.setText("Sound timer: " + cpu.getSoundTimer());
		
		try {
			((UpdatablePanel)tabs.getSelectedComponent()).update();
		} catch (Exception e) {
			
		}
	}
	
	private void initTopPanel() {
		// Top panel
		topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		
		// Labels
		JPanel topLabels = new JPanel();
		topLabels.setLayout(new BoxLayout(topLabels, BoxLayout.Y_AXIS));
		topPanel.add(topLabels, BorderLayout.WEST);
		
		// FPS Label
		fpsLabel = new JLabel();
		topLabels.add(fpsLabel);
		
		// PC Label
		pcLabel = new JLabel();
		topLabels.add(pcLabel);
		
		// SP Label
		spLabel = new JLabel();
		topLabels.add(spLabel);
	
		// Opcode Label
		opcodeLabel = new JLabel();
		topLabels.add(opcodeLabel);
		
		// Delay timer Label
		dtLabel = new JLabel();
		topLabels.add(dtLabel);
		
		// Sound timer Label
		stLabel = new JLabel();
		topLabels.add(stLabel);
		
		
		// Buttons
		JPanel topButtons = new JPanel();
		topButtons.setLayout(new BoxLayout(topButtons, BoxLayout.Y_AXIS));
		topPanel.add(topButtons, BorderLayout.EAST);
		
		// Pause button
		JButton pauseButton = new JButton("Pause");
		pauseButton.addActionListener((ActionEvent e) -> {
			cpu.toggleRunning();
			pauseButton.setText(cpu.getRunning() ? "Pause" : "Resume");
		});
		topButtons.add(pauseButton);
		
		// Step button
		JButton stepButton = new JButton("Step CPU");
		stepButton.addActionListener((ActionEvent e) -> {
			cpu.step();
		});
		topButtons.add(stepButton);
	}
	
	private void initTabs() {
		tabs = new JTabbedPane();
		
		tabs.add("Registers", new RegisterPanel(cpu));
		tabs.add("Stack", new StackPanel(cpu));
		tabs.add("Memory", new MemoryPanel(cpu));
		tabs.add("Input", new JPanel());
		
		getContentPane().add(tabs, BorderLayout.CENTER);
	}
}
