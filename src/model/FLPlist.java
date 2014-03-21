package model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tools.FLTreeNode;
import tools.Tools;

public class FLPlist extends FLTreeNode {
	String plistFile;
	String textureFile;
	int format = 2;

	public FLPlist() {
	}

	public void loadElement(Element e, FLAnimation a) {
		setPlistFile(e.getText());
		File file = Tools.openFile(null, a.getCurrentPath() + File.separator + plistFile);
		load(file);
	}

	public Element toElement(FLAnimation a) {
		Element e = new Element("Plist");
		String path = Tools.getRelativePath(a.getCurrentPath(), plistFile);
		e.setText(Tools.getFileName(path));
		return e;
	}

	@SuppressWarnings("unchecked")
	public Vector<FLClip> getClips() {
		return this.children;
	}

	public String getPlistFile() {
		return plistFile;
	}

	public void setPlistFile(String plistFile) {
		this.plistFile = plistFile;
	}

	public String getTextureFile() {
		return textureFile;
	}

	public void setTextureFile(String textureFile) {
		this.textureFile = textureFile;
	}

	public void load(File file) {
		this.plistFile = file.getPath();
		setName(file.getName());
		Element doc = Tools.readFromXml(file);
		List<?> list = doc.getChild("dict").getChildren();
		for (int i = 0; i < list.size(); i += 2) {
			Element node = (Element) list.get(i);
			Element dict = (Element) list.get(i + 1);
			String key = node.getText();
			if (key.equals("frames")) {
				loadFrames(dict);
			} else if (key.equals("metadata")) {
				loadMetadata(dict);
			} else if (key.equals("texture")) {
				loadTexture(dict);
				format = -1;
			}
		}
	}

	public void loadFrames(Element e) {
		List<?> list = e.getChildren();
		for (int i = 0; i < list.size(); i += 2) {
			Element node = (Element) list.get(i);
			Element dict = (Element) list.get(i + 1);
			FLClip frame = new FLClip();
			frame.setPlist(this);
			//key
			String key = node.getText();
			frame.setName(key);
			if (format == 2) {
				//frame
				String text = ((Element) dict.getChildren().get(1)).getText();
				frame.setFrame(Tools.convertStr2Rect(text));
				//offset
				text = ((Element) dict.getChildren().get(3)).getText();
				frame.setOffset(Tools.convertStr2Point(text));
				//rotated
				text = ((Element) dict.getChildren().get(5)).getName();
				frame.setRotated(text.toLowerCase().equals("true"));
				//sourceColorRect
				text = ((Element) dict.getChildren().get(7)).getText();
				frame.setSourceColorRect(Tools.convertStr2Rect(text));
				//sourceSize
				text = ((Element) dict.getChildren().get(9)).getText();
				frame.setSourceSize(Tools.convertStr2Size(text));
			} else if (format == -1) {
				int x, y, width, height;
				float offsetX, offsetY;
				int originalWidth, originalHeight;
				//x
				String text = ((Element) dict.getChildren().get(1)).getText();
				x = Integer.parseInt(text);
				//y
				text = ((Element) dict.getChildren().get(3)).getText();
				y = Integer.parseInt(text);
				//width
				text = ((Element) dict.getChildren().get(5)).getText();
				width = Integer.parseInt(text);
				//height
				text = ((Element) dict.getChildren().get(7)).getText();
				height = Integer.parseInt(text);
				//offsetX
				text = ((Element) dict.getChildren().get(9)).getText();
				offsetX = Float.parseFloat(text);
				//offsetY
				text = ((Element) dict.getChildren().get(11)).getText();
				offsetY = Float.parseFloat(text);
				//originalWidth
				text = ((Element) dict.getChildren().get(13)).getText();
				originalWidth = Integer.parseInt(text);
				//originalHeight
				text = ((Element) dict.getChildren().get(15)).getText();
				originalHeight = Integer.parseInt(text);
				//setting
				frame.setFrame(new Rectangle(x, y, width, height));
				frame.setOffset(offsetX, offsetY);
				frame.setSourceSize(new Dimension(originalWidth, originalHeight));
			}
			this.add(frame);
		}
	}

	public void loadMetadata(Element e) {
		List<?> list = e.getChildren();
		for (int i = 0; i < list.size(); i += 2) {
			Element key = (Element) list.get(i);
			Element value = (Element) list.get(i + 1);
			if (key.getText().equals("textureFileName")) {
				String path = Tools.getPath(plistFile);
				textureFile = path + File.separator + value.getText();
			}
		}
	}

	public void loadTexture(Element e) {
		//		int width = 0;
		//		int height = 0;
		//		List<?> list = e.getChildren();
		//		for (int i = 0; i < list.size(); i += 2) {
		//			Element key = (Element) list.get(i);
		//			Element value = (Element) list.get(i + 1);
		//			if (key.getText().equals("width")) {
		//				width = Integer.parseInt(value.getText());
		//			} else if (key.getText().equals("height")) {
		//				height = Integer.parseInt(value.getText());
		//			}
		//		}
		String path = Tools.getPath(plistFile);
		String png = Tools.getFileName(plistFile, false) + ".png";
		textureFile = path + File.separator + png;
	}

	public FLClip getClip(String clipName) {
		for (int i = 0; i < getClips().size(); i++) {
			FLClip clip = getClips().get(i);
			if (clip.getName().equals(clipName)) {
				return clip;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return Tools.getFileName(plistFile);
	}
}
