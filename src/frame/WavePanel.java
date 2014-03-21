package frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import tools.Tools;

public class WavePanel extends JPanel {
	public WavePanel(String file) {
		init(file);
		Timer selectTimer = new Timer(true);
		selectTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (WavePanel.this.isVisible()) {
					source.newPixels();
					rippleRender();
				}
			}
		}, 0, 10);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				makeWave(e.getPoint(), 6, 128);
			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				makeWave(e.getPoint(), 4, 128);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				makeWave(e.getPoint(), 2, 128);
			}
		});
	}

	public void makeWave(Point p, int r, int h) {
		int x = p.x * offImage.getWidth(null) / getWidth();
		int y = p.y * offImage.getHeight(null) / getHeight();
		makeWave(x, y, 4, 128);
	}

	void makeWave(int x, int y, int r, int h) {
		int width = offImage.getWidth(null);
		int height = offImage.getHeight(null);
		if ((x + r) > width || (y + r) > height || (x - r) < 0 || (y - r) < 0) {
			return;
		}
		int value = r * r;
		for (int posx = x - r; posx < x + r; ++posx) {
			for (int posy = y - r; posy < y + r; ++posy) {
				if ((posx - x) * (posx - x) + (posy - y) * (posy - y) < value) {
					arrWaveCurrent[width * posy + posx] = -h;
				}
			}
		}
	}

	private Image offImage;
	private MemoryImageSource source;
	int[] arrWaveCurrent;
	int[] arrWaveNext;
	int[] arrClrInfo;
	int[] arrClrBuff;

	private void init(String file) {
		Image image = Tools.getImageByIO(file);
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int length = width * height;
		arrWaveCurrent = new int[length];
		arrWaveNext = new int[length];
		arrClrInfo = new int[length];
		arrClrBuff = new int[length];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height,
				arrClrInfo, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		source = new MemoryImageSource(width, height, arrClrBuff, 0, width);
		source.setAnimated(true);
		image.getGraphics();
		offImage = createImage(source);
		setSize(width, height);
		setPreferredSize(new Dimension(width, height));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.YELLOW);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(offImage, 0, 0, getWidth(), getHeight(), this);
	}

	void rippleRender() {
		int width = offImage.getWidth(null);
		int height = offImage.getHeight(null);
		int index = width;
		int len = height - 1;
		for (int y = 1; y < len; ++y) {
			for (int x = 0; x < width; ++x, ++index) {
				int x1 = arrWaveCurrent[index - 1];
				int x2 = arrWaveCurrent[index + 1];
				int x3 = arrWaveCurrent[index - width];
				int x4 = arrWaveCurrent[index + width];
				arrWaveNext[index] = (((x1 + x2 + x3 + x4) >> 1) - arrWaveNext[index]);
				arrWaveNext[index] -= arrWaveNext[index] >> 5;
				int yoffset = x3 - x4;
				int xoffset = x1 - x2;
				int offset = width * yoffset + xoffset;
				if (index + offset > 0 && index + offset < width * height) {
					arrClrBuff[index] = arrClrInfo[index + offset];
				} else {
					arrClrBuff[index] = arrClrInfo[index];
				}
			}
		}
		int[] temp = arrWaveCurrent;
		arrWaveCurrent = arrWaveNext;
		arrWaveNext = temp;
	}
}