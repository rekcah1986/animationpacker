package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

public class AboutDialog extends JDialog {
	public AboutDialog(Component aParent) {
		super(JOptionPane.getFrameForComponent(aParent), "About", true);
		init();
	}

	private void init() {
		this.rootPane.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EXIT");
		this.rootPane.getActionMap().put("EXIT", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel contentPanel = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
		WavePanel aboutPanel = new WavePanel("/res/about.jpg") {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.WHITE);
				String str = AppConfig.config().getVersion()
						+ "\nBy Rekc@h\n2012.12\nEmail:gaobin107@qq.com\nQQ:442416294";
				String[] strs = str.split("\n");
				int lineHeight = 20;
				int top = (getHeight() - strs.length * lineHeight) / 2;
				for (int i = 0; i < strs.length; i++) {
					int width = g.getFontMetrics().stringWidth(strs[i]);
					g.drawString(strs[i], (getWidth() - width) / 2, top
							+ lineHeight * i);
				}
			}
		};

		tabbedPane.add("About", aboutPanel);

		JPanel propertiesPanel = new JPanel(new BorderLayout());
		JTable propertiesTable = new JTable(new PropertiesTableModel(null));
		propertiesTable.setAutoResizeMode(0);
		propertiesTable.setColumnSelectionAllowed(false);
		propertiesTable.setRowSelectionAllowed(false);
		propertiesTable.getColumnModel().getColumn(0).setPreferredWidth(188);
		propertiesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
		propertiesPanel.add(new JScrollPane(propertiesTable), "Center");
		tabbedPane.add("Properties", propertiesPanel);

		contentPanel.add(tabbedPane, "Center");
		setContentPane(contentPanel);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension dlgSize = new Dimension(400, 320);
		int dlgPosX = screenSize.width / 2 - dlgSize.width / 2;
		int dlgPosY = screenSize.height / 2 - dlgSize.height / 2;
		setLocation(dlgPosX, dlgPosY);
		setSize(dlgSize);
		setVisible(true);
	}
}

class PropertiesTableModel extends AbstractTableModel {

	public PropertiesTableModel(AboutDialog dialog) {
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int index) {
		if (index == 0)
			return "Property";
		if (index == 1)
			return "Value";
		return "ERROR";
	}

	@Override
	public int getRowCount() {
		return System.getProperties().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int colIndex) {
		Iterator<?> iter = System.getProperties().keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Object key = iter.next();
			if (i == rowIndex) {
				if (colIndex == 0) {
					return key;
				}
				return System.getProperties().getProperty(key.toString());
			}
			i++;
		}
		return "ERROR";
	}
}