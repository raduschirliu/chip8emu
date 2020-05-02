package chip8emu.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import chip8emu.emulator.CPU;

public class MemoryPanel extends JPanel implements UpdatablePanel {
	private static final long serialVersionUID = 1L;
	
	private JScrollPane sp;
	private JTable table;
	private DefaultTableModel tableModel;
	private CPU cpu;
	
	public MemoryPanel(CPU cpu) {
		this.cpu = cpu;
		
		setLayout(new BorderLayout());
		
		String columns[] = { "Address", "Value (Hex)", "Value (Decimal)" };
		tableModel = new DefaultTableModel(null, columns) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		table = new JTable(tableModel);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		for (int i = 0; i < cpu.getMemory().length; i++) {
			tableModel.addRow(new Object[] {});
		}
		
		sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(300, 300));
		add(sp, BorderLayout.CENTER);
	}
	
	public void update() {
		short[] mem = cpu.getMemory();
		
		for (int i = 0; i < mem.length; i++) {
			tableModel.setValueAt(String.format("%x", i), i, 0);
			tableModel.setValueAt(String.format("%x", mem[i]), i, 1);
			tableModel.setValueAt(mem[i], i, 2);
		}
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionInterval(cpu.getPC(), cpu.getPC());
	}
}
