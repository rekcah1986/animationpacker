package model;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import tools.FLTreeNode;
import tools.Tools;

public class FLClip extends FLTreeNode {
	private FLPlist plist;
	private Rectangle frame;
	private double offsetX, offsetY;
	private boolean rotated;
	private Rectangle sourceColorRect;
	private Dimension sourceSize;

	public FLPlist getPlist() {
		return plist;
	}

	public void setPlist(FLPlist plist) {
		this.plist = plist;
	}

	public Rectangle getFrame() {
		return frame;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}

	public void setFrame(Rectangle frame) {
		this.frame = frame;
	}

	public void setOffset(double offsetX, double offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public void setOffset(Point offset) {
		this.offsetX = offset.x;
		this.offsetY = offset.y;
	}

	public boolean isRotated() {
		return rotated;
	}

	public void setRotated(boolean rotated) {
		this.rotated = rotated;
	}

	public Rectangle getSourceColorRect() {
		return sourceColorRect;
	}

	public void setSourceColorRect(Rectangle sourceColorRect) {
		this.sourceColorRect = sourceColorRect;
	}

	public Dimension getSourceSize() {
		return sourceSize;
	}

	public void setSourceSize(Dimension sourceSize) {
		this.sourceSize = sourceSize;
	}

	public Image getImage() {
		Image image = Tools.getImage(plist.textureFile);
		Rectangle dst = new Rectangle(frame);
		Image ret = null;
		if (rotated) {
			dst.width = frame.height;
			dst.height = frame.width;
			ret = Tools.cutImage(image, dst);
			ret = Tools.getTransImage(ret, FLIP_90_X_Y);
		} else {
			ret = Tools.cutImage(image, dst);
		}
		return ret;
	}

	@Override
	public Icon getIcon() {
		if (icon == null) {
			ImageIcon ii = new ImageIcon(getImage());
			icon = Tools.getScaledImage(ii, 32, 32);
		}
		return super.getIcon();
	}

	public String getKey() {
		return plist.toString() + "/" + getName();
	}
}