package frame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import model.FLSequence;

public class FLSequenceTableModel extends AbstractTableModel {

	Vector<FLSequence> sequences;

	static final String[] COLUMN_NAMES = { "ID", "Frame", "Duration" };
	static final Class<?>[] COLUMN_CLASSES = { String.class, String.class,
			Integer.class };

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASSES[columnIndex];
	}

	public FLSequenceTableModel() {
	}

	public void setSequences(Vector<FLSequence> sequences) {
		this.sequences = sequences;
	}

	public Vector<FLSequence> getSequences() {
		return sequences;
	}

	@Override
	public int getRowCount() {
		if (sequences == null) {
			return 0;
		}
		return sequences.size();
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
			return sequences.get(row).getFrame();
		} else if (column == 2) {
			return sequences.get(row).getDuration();
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column == 2) {
			sequences.get(row).setDuration(Integer.parseInt(value.toString()));
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 2;
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
}
