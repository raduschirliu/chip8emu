package chip8emu.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
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
	
	private JLabel fpsLabel, pcLabel, spLabel, iLabel, opcodeLabel, dtLabel, stLabel, romLabel;
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
		iLabel.setText("I : " + String.format("%x", cpu.getI()));
		opcodeLabel.setText(String.format("Opcode: %x", cpu.getOpcode()));
		dtLabel.setText("Delay timer: " + cpu.getDelayTimer());
		stLabel.setText("Sound timer: " + cpu.getSoundTimer());
		
		try {
			((UpdatablePanel)tabs.getSelectedComponent()).update();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void romUpdated() {
		if (cpu.getActiveROM() != null) {
			romLabel.setText("ROM: " + cpu.getActiveROM().getFileName());
		} else {
			romLabel.setText("ROM: None");
		}
	}
	
	private void initTopPanel() {
		// Top panel
		topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
			
		// Labels
		JPanel topLabels = new JPanel();
		GridLayout labelLayout = new GridLayout(4, 2);
		labelLayout.setHgap(60);
		topLabels.setLayout(labelLayout);
		topPanel.add(topLabels, BorderLayout.WEST);
		
		// PC Label
		pcLabel = new JLabel();
		topLabels.add(pcLabel);
		
		// FPS Label
		fpsLabel = new JLabel();
		topLabels.add(fpsLabel);
		
		// SP Label
		spLabel = new JLabel();
		topLabels.add(spLabel);
		
		// Delay timer Label
		dtLabel = new JLabel();
		topLabels.add(dtLabel);
		
		// I Label
		iLabel = new JLabel();
		topLabels.add(iLabel);
		
		// Sound timer Label
		stLabel = new JLabel();
		topLabels.add(stLabel);
		
		// Opcode Label
		opcodeLabel = new JLabel();
		topLabels.add(opcodeLabel);
		
		// ROM Label
		romLabel = new JLabel();
		topLabels.add(romLabel);
		romUpdated();
		
		
		// Buttons
		JPanel topButtons = new JPanel();
		topButtons.setLayout(new BoxLayout(topButtons, BoxLayout.Y_AXIS));
		topPanel.add(topButtons, BorderLayout.EAST);
		
		// Pause button
		JButton pauseButton = new JButton(cpu.getRunning() ? "Pause" : "Resume");
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
		tabs.add("Input", new InputPanel(cpu));
		
		getContentPane().add(tabs, BorderLayout.CENTER);
	}
}
