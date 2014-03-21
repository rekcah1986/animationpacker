package frame;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import model.FLFrame;

public class FLFrameTableModel extends AbstractTableModel {

	private final Vector<FLFrame> frames;

	static final String[] COLUMN_NAMES = { "ID", "Name" };

	public FLFrameTableModel(Vector<FLFrame> frames) {
		this.frames = frames;
	}

	public Vector<FLFrame> getFrames() {
		return frames;
	}

	@Override
	public int getRowCount() {
		if (frames == null) {
			return 0;
		}
		return frames.size();
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
			return frames.get(row);
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column == 1) {
			frames.get(row).setName(value.toString());
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
