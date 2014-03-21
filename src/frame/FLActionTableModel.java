package frame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import model.FLAction;

public class FLActionTableModel extends AbstractTableModel {

	private final Vector<FLAction> actions;

	public static final String[] COLUMN_NAMES = { "ID", "Name" };

	public FLActionTableModel(Vector<FLAction> actions) {
		this.actions = actions;
	}

	public Vector<FLAction> getActions() {
		return actions;
	}

	@Override
	public int getRowCount() {
		if (actions == null) {
			return 0;
		}
		return actions.size();
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
			return actions.get(row);
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column == 1) {
			actions.get(row).setName(value.toString());
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 1;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
}
