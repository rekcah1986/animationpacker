package tools;

import java.util.List;

import org.jdom.Element;

/**
 * XML Element解析器
 * 
 * @author Rekc@h
 */
public class ElementParser {
	private final Element e;

	public ElementParser(Element e) {
		this.e = e;
	}

	public String getAttributeString(String name) {
		return e.getAttributeValue(name);
	}

	public int getAttributeInteger(String name) {
		String value = e.getAttributeValue(name);
		int ret = (int) Tools.getNumber(value);
		return ret;
	}

	public short getAttributeShort(String name) {
		String value = e.getAttributeValue(name);
		short ret = (short) Tools.getNumber(value);
		return ret;
	}

	public byte getAttributeByte(String name) {
		String value = e.getAttributeValue(name);
		byte ret = (byte) Tools.getNumber(value);
		return ret;
	}

	public long getAttributeLong(String name) {
		String value = e.getAttributeValue(name);
		long ret = Tools.getNumber(value);
		return ret;
	}

	public boolean getAttributeBoolean(String name) {
		String value = e.getAttributeValue(name);
		boolean ret = Tools.getBoolean(value);
		return ret;
	}

	public Element getChild(String name) {
		return e.getChild(name);
	}

	@SuppressWarnings("unchecked")
	public List<Element> getChildAsList(String name) {
		return getChild(name).getChildren();
	}

	@SuppressWarnings("unchecked")
	public List<Element> getElements() {
		return e.getChildren();
	}
}
