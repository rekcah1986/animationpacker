package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import org.jdom.Element;

import tools.ElementParser;
import tools.FLTreeNode;
import tools.Tools;
import frame.AppConfig;

public class FLModule extends FLTreeNode {
	private FLClip clip;
	private int posX, posY, prevX, prevY, width, height, trans;
	private boolean selected;
	private transient Image image;

	public FLModule() {
	}

	public FLModule(FLClip clip) {
		setClip(clip);
		setTrans(trans);
		//		posX = -width / 2 + clip.getOffset().x;
		//		posY = -height / 2 - clip.getOffset().y;
		posX = (int) clip.getOffsetX();
		posY = (int) clip.getOffsetY();
	}

	public void loadElement(Element e, FLAnimation a) {
		ElementParser ep = new ElementParser(e);
		//		int key = Tools.getHexNumber(ep.getAttributeString("clip"));
		//		FLClip clip = a.getClipFromNumberKey(key);
		String plistName = ep.getAttributeString("plist");
		String clipName = ep.getAttributeString("clip");
		FLClip clip = a.getPlist(plistName).getClip(clipName);
		setClip(clip);
		posX = ep.getAttributeInteger("x");
		posY = ep.getAttributeInteger("y");
		trans = ep.getAttributeInteger("trans");
		setTrans(trans);
	}

	public Element toElement(FLAnimation a) {
		Element e = new Element("Module");
		//		String key = Tools.getHexString(a.getClipNumberKey(clip));
		FLPlist plist = clip.getPlist();
		e.setAttribute("plist", "" + plist.getName());
		e.setAttribute("clip", "" + clip.getName());
		e.setAttribute("x", "" + posX);
		e.setAttribute("y", "" + posY);
		e.setAttribute("trans", "" + trans);
		return e;
	}

	public FLModule(FLModule m) {
		this.clip = m.clip;
		posX = m.posX;
		posY = m.posY;
		prevX = m.prevX;
		prevY = m.prevY;
		width = m.width;
		height = m.height;
		trans = m.trans;
		selected = m.selected;
		image = m.image;
		setTrans(m.trans);
	}

	public Rectangle getBounds() {
		return new Rectangle(posX - width / 2, posY - height / 2, width, height);
	}

	public void draw(Graphics g) {
		g.translate(-width / 2, -height / 2);
		if (selected) {
			Color color = AppConfig.config().getModuleSelectedColor();
			g.setColor(color);
			g.fillRect(posX, posY, width, height);
		}
		g.drawImage(image, posX, posY, null);
		g.translate(width / 2, height / 2);
	}

	public boolean isFlipX() {
		return isTrans(FLIP_X);
	}

	public boolean isFlipY() {
		return isTrans(FLIP_Y);
	}

	public boolean isFlip90() {
		return isTrans(FLIP_90);
	}

	public void rotateClockwise() {
		boolean f = (isFlipX() && isFlipY()) || (!isFlipX() && !isFlipY());
		if (f && isFlip90() || !f && !isFlip90()) {
			changeTrans(FLIP_X);
			changeTrans(FLIP_Y);
		}
		changeTrans(FLIP_90);
		setTrans(trans);
	}

	public void rotateAntiClockwise() {
		boolean f = (isFlipX() && isFlipY()) || (!isFlipX() && !isFlipY());
		if (f && !isFlip90() || !f && isFlip90()) {
			changeTrans(FLIP_X);
			changeTrans(FLIP_Y);
		}
		changeTrans(FLIP_90);
		setTrans(trans);
	}

	public void rotate90() {
		changeTrans(FLIP_90);
		setTrans(trans);
	}

	public void flipX() {
		changeTrans(FLIP_X);
		setTrans(trans);
	}

	public void flipY() {
		changeTrans(FLIP_Y);
		setTrans(trans);
	}

	public void axisFlipX() {
		posX *= -1;
		flipX();
	}

	public void axisFlipY() {
		posY *= -1;
		flipY();
	}

	public void changeTrans(int f) {
		if ((this.trans & f) != 0) {
			this.trans &= (f ^ 0xFFFFFFFF);
		} else {
			this.trans |= f;
		}
	}

	public boolean isTrans(int f) {
		return (this.trans & f) != 0;
	}

	public void setTrans(int trans) {
		this.trans = trans;
		image = Tools.getTransImage(clip.getImage(), trans);
		width = image.getWidth(null);
		height = image.getHeight(null);
	}

	public void savePos() {
		prevX = posX;
		prevY = posY;
	}

	public void restorePrevPos() {
		setPosX(prevX);
		setPosY(prevY);
	}

	public FLClip getClip() {
		return clip;
	}

	public void setClip(FLClip clip) {
		this.clip = clip;
		setName(clip.getName());
	}

	public Point getPosition() {
		return new Point(posX, posY);
	}

	public void setPosition(int x, int y) {
		setPosX(x);
		setPosY(y);
	}

	public void setPosition(Point p) {
		setPosition(p.x, p.y);
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getPrevX() {
		return prevX;
	}

	public void setPrevX(int prevX) {
		this.prevX = prevX;
	}

	public int getPrevY() {
		return prevY;
	}

	public void setPrevY(int prevY) {
		this.prevY = prevY;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getTrans() {
		return trans;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
