package model;

import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tools.ElementParser;
import tools.FLTreeNode;

public class FLAction extends FLTreeNode {
	private final Vector<FLSequence> sequences;

	public FLAction() {
		sequences = new Vector<FLSequence>();
	}

	public void loadElement(Element e, FLAnimation a) {
		ElementParser ep = new ElementParser(e);
		setName(ep.getAttributeString("name"));
		List<Element> list = ep.getElements();
		for (Element eSequence : list) {
			FLSequence sequence = new FLSequence();
			sequence.loadElement(eSequence, a);
			sequences.add(sequence);
		}
	}

	public Element toElement(FLAnimation a) {
		Element e = new Element("Action");
		e.setAttribute("name", getName());
		for (FLSequence sequence : sequences) {
			e.addContent(sequence.toElement(a));
		}
		return e;
	}

	public FLAction(FLAction a) {
		super(a);
		this.sequences = new Vector<FLSequence>(a.sequences);
	}

	public Vector<FLSequence> getSequences() {
		return sequences;
	}

	public void addFrames(Vector<FLFrame> frames) {
		for (FLFrame f : frames) {
			sequences.add(new FLSequence(f));
		}
	}
}
