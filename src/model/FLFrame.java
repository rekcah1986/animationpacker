package model;

import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tools.ElementParser;
import tools.FLTreeNode;

public class FLFrame extends FLTreeNode {
	private final Vector<FLModule> modules;

	public FLFrame() {
		modules = new Vector<FLModule>();
	}

	public FLFrame(FLFrame f) {
		setName(f.getName());
		modules = new Vector<FLModule>();
		for (FLModule m : f.modules) {
			modules.add(new FLModule(m));
		}
	}

	public void loadElement(Element e, FLAnimation a) {
		ElementParser ep = new ElementParser(e);
		setName(ep.getAttributeString("name"));
		List<Element> list = ep.getElements();
		for (Element eModule : list) {
			FLModule module = new FLModule();
			module.loadElement(eModule, a);
			modules.add(module);
		}
	}

	public Element toElement(FLAnimation a) {
		Element e = new Element("Frame");
		e.setAttribute("name", getName());
		for (FLModule module : modules) {
			e.addContent(module.toElement(a));
		}
		return e;
	}

	public Vector<FLModule> getModules() {
		return modules;
	}
}
