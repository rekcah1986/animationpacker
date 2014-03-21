package model;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tools.ElementParser;
import tools.FLTreeNode;
import tools.Tools;

public class FLAnimation extends FLTreeNode {
	private final Vector<FLAction> actions;
	private final Vector<FLFrame> frames;
	private final Vector<FLPlist> plists;
	private String filePath;

	public FLAnimation() {
		actions = new Vector<FLAction>();
		frames = new Vector<FLFrame>();
		plists = new Vector<FLPlist>();
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
		if (filePath != null) {
			setName(Tools.getFileName(filePath, false));
		}
	}

	public String getCurrentPath() {
		if (filePath == null) {
			return Tools.getCurrentDirectory();
		}
		return Tools.getPath(filePath);
	}

	public void load(File file) {
		setFilePath(file.getAbsolutePath());
		loadElement(Tools.readFromXml(file));
	}

	@Override
	public Element toElement() {
		Element e = new Element("Animation");
		Element ePlists = new Element("Plists");
		for (FLPlist plist : plists) {
			ePlists.addContent(plist.toElement(this));
		}
		e.addContent(ePlists);
		Element eFrames = new Element("Frames");
		for (FLFrame frame : frames) {
			eFrames.addContent(frame.toElement(this));
		}
		e.addContent(eFrames);
		Element eActions = new Element("Actions");
		for (FLAction action : actions) {
			eActions.addContent(action.toElement(this));
		}
		e.addContent(eActions);
		return e;
	}

	@Override
	public void loadElement(Element e) {
		ElementParser ep = new ElementParser(e);
		List<Element> list = ep.getChildAsList("Plists");
		for (Element ePlist : list) {
			FLPlist plist = new FLPlist();
			plist.loadElement(ePlist, this);
			plists.add(plist);
		}
		list = ep.getChildAsList("Frames");
		for (Element eFrame : list) {
			FLFrame frame = new FLFrame();
			frame.loadElement(eFrame, this);
			frames.add(frame);
		}
		list = ep.getChildAsList("Actions");
		for (Element eAction : list) {
			FLAction action = new FLAction();
			action.loadElement(eAction, this);
			actions.add(action);
		}
	}

	public Vector<FLAction> getActions() {
		return actions;
	}

	public Vector<FLFrame> getFrames() {
		return frames;
	}

	public Vector<FLPlist> getPlists() {
		return plists;
	}

	public FLPlist getPlist(String name) {
		for (int i = 0; i < plists.size(); i++) {
			FLPlist p = plists.get(i);
			if (p.toString().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public FLPlist getPlistFullPath(String name) {
		for (int i = 0; i < plists.size(); i++) {
			FLPlist p = plists.get(i);
			if (p.getPlistFile().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public FLClip getClip(String key) {
		String[] strs = key.split("/");
		String plistName = strs[0];
		String clipName = strs[1];
		FLPlist plist = getPlist(plistName);
		if (plist != null) {
			return plist.getClip(clipName);
		}
		return null;
	}

	public int getClipNumberKey(FLClip clip) {
		FLPlist plist = clip.getPlist();
		int plistId = plists.indexOf(plist);
		int clipId = plist.getClips().indexOf(clip);
		int key = (plistId << 16) + clipId;
		return key;
	}

	public FLClip getClipFromNumberKey(int key) {
		int clipId = key & 0x0000FFFF;
		int plistId = key >> 16;
		FLPlist plist = plists.get(plistId);
		FLClip clip = plist.getClips().get(clipId);
		return clip;
	}
}
