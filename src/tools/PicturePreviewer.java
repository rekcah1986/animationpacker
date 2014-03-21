package tools;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * 文件对话框图片预览器
 * 
 * @author Rekc@h
 */
public class PicturePreviewer extends JComponent implements
		PropertyChangeListener {

	private static final long serialVersionUID = 7616883524290664225L;
	ImageIcon thumbnail = null;

	public PicturePreviewer() {
		setPreferredSize(new Dimension(100, 50));
	}

	public void loadImage(File f) {
		if (f == null) {
			thumbnail = null;
		} else {
			ImageIcon tmpIcon = new ImageIcon(f.getPath());
			if (tmpIcon.getIconWidth() > 90) {
				thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(
						90, -1, Image.SCALE_DEFAULT));
			} else {
				thumbnail = tmpIcon;
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();
		if (prop == JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) {
			if (isShowing()) {
				loadImage((File) e.getNewValue());
				repaint();
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		if (thumbnail != null) {
			int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
			int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
			if (y < 0) {
				y = 0;
			}
			if (x < 5) {
				x = 5;
			}
			thumbnail.paintIcon(this, g, x, y);
		}
	}
}
