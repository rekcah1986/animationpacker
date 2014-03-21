package tools;

import javax.swing.undo.AbstractUndoableEdit;

public class RekcahUndoableEdit extends AbstractUndoableEdit {
	String name;

	public RekcahUndoableEdit(String name) {
		if (name == null) {
			name = "";
		}
		this.name = name;
		init();
	}

	protected void init() {
	}

	@Override
	public String getPresentationName() {
		return name;
	}
}
