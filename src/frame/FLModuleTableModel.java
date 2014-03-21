package frame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import model.FLModule;

public class FLModuleTableModel extends AbstractTableModel {

	private Vector<FLModule> modules;
	private static final String[] COLUMN_NAMES = { "ID", "Module", "Trans" };

	public FLModuleTableModel() {
	}

	public void setModules(Vector<FLModule> modules) {
		this.modules = modules;
	}

	public Vector<FLModule> getModules() {
		return this.modules;
	}

	@Override
	public int getRowCount() {
		if (modules == null) {
			return 0;
		}
		return modules.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return row;
		} else if (column == 1) {
			return modules.get(row);
		} else if (column == 2) {
			return modules.get(row).getTrans();
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
}
