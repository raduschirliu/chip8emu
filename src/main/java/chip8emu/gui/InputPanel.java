package chip8emu.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import chip8emu.emulator.CPU;

public class InputPanel extends JPanel implements UpdatablePanel {
	private static final long serialVersionUID = 1L;
	
	private Color releasedColor, pressedColor;
	private JLabel labels[];
	private CPU cpu;
	
	public InputPanel(CPU cpu) {
		this.cpu = cpu;
		
		GridLayout layout = new GridLayout(4, 4);
		layout.setHgap(5);
		layout.setVgap(5);
		setLayout(layout);
		
		releasedColor = new Color(200, 100, 100);
		pressedColor = new Color(100, 200, 100);
		
		labels = new JLabel[16];
		
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel(String.format("%x", i).toUpperCase());
			labels[i].setHorizontalAlignment(JLabel.CENTER);
			labels[i].setVerticalAlignment(JLabel.CENTER);
			labels[i].setOpaque(true);
		}
		
		add(labels[0x1]);
		add(labels[0x2]);
		add(labels[0x3]);
		add(labels[0xc]);
		
		add(labels[0x4]);
		add(labels[0x5]);
		add(labels[0x6]);
		add(labels[0xd]);
		
		add(labels[0x7]);
		add(labels[0x8]);
		add(labels[0x9]);
		add(labels[0xe]);
		
		add(labels[0xa]);
		add(labels[0x0]);
		add(labels[0xb]);
		add(labels[0xf]);
	}
	
	public void update() {
		boolean[] keys = cpu.getKeys();
		
		for (int i = 0; i < keys.length; i++) {
			labels[i].setBackground(keys[i] ? pressedColor : releasedColor);
		}
	}
}
