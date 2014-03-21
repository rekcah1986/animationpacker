package tools;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;

/**
 * 可编辑的树结点
 *
 * @author Rekc@h
 */
public class FLTreeNode extends DefaultMutableTreeNode implements RekcahConst {
	private static final long serialVersionUID = -2204297617778025284L;

	public FLTreeNode() {
	}

	public FLTreeNode(FLTreeNode node) {
		this.name = node.name;
		this.icon = node.icon;
	}

	public FLTreeNode(String name) {
		this.setName(name);
	}

	public FLTreeNode(Element e) {
		loadElement(e);
	}

	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected Icon icon;

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public String toString() {
		return name;
	}

	public void loadElement(Element e) {
		name = e.getName();
	}

	public Element toElement() {
		Element e = new Element(name);
		return e;
	}
}
