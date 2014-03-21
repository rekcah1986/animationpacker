package tools;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * 按钮动作
 * 
 * @author Rekc@h
 * @date 2012-1-12
 */
public abstract class RekcahAction extends AbstractAction {

	private static final long serialVersionUID = 7814383756269096727L;

	public RekcahAction() {
		init();
	}

	public RekcahAction(String name) {
		super(name);
		this.setTooltip(name);
		init();
	}

	public RekcahAction(String name, Icon icon) {
		super(name, icon);
		this.setTooltip(name);
		init();
	}

	public RekcahAction(String name, String icon) {
		super(name);
		this.setTooltip(name);
		setIcon(Tools.getImageIcon(icon, true));
		init();
	}

	protected void init() {
	}

	public void setTooltip(String tip) {
		putValue(Action.SHORT_DESCRIPTION, tip);
	}

	public void setAccelerator(KeyStroke keyStroke) {
		this.putValue(Action.ACCELERATOR_KEY, keyStroke);
	}

	public void setAccelerator(KeyStroke keyStroke, JComponent com) {
		InputMap im = com
				.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = com.getActionMap();
		im.put(keyStroke, this);
		am.put(this, this);
	}

	public void setName(String name) {
		putValue(Action.NAME, name);
	}

	public void setIcon(Icon icon) {
		this.putValue(Action.SMALL_ICON, icon);
	}

	public void setSelected(boolean select) {
		this.putValue(Action.SELECTED_KEY, select);
	}

	public boolean isSelected() {
		boolean b = (Boolean) getValue(SELECTED_KEY);
		return b;
	}
}
