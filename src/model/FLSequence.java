package model;

import org.jdom.Element;

import tools.ElementParser;
import tools.FLTreeNode;

public class FLSequence extends FLTreeNode {
	private FLFrame frame;
	private int duration;

	public FLSequence() {
	}

	public FLSequence(FLFrame frame) {
		this.frame = frame;
		duration = 1;
	}

	public void loadElement(Element e, FLAnimation a) {
		ElementParser ep = new ElementParser(e);
		duration = ep.getAttributeInteger("duration");
		int frameId = ep.getAttributeInteger("id");
		if (frameId >= 0) {
			frame = a.getFrames().get(frameId);
		}
	}

	public Element toElement(FLAnimation a) {
		Element e = new Element("Sequence");
		e.setAttribute("duration", "" + duration);
		e.setAttribute("id", "" + a.getFrames().indexOf(frame));
		return e;
	}

	@Override
	public String getName() {
		return frame.getName();
	}

	public FLFrame getFrame() {
		return frame;
	}

	public void setFrame(FLFrame frame) {
		this.frame = frame;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}
