package frame;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;

import tools.ElementParser;
import tools.Tools;

/**
 * 配置
 * @author Rekc@h
 */
public class AppConfig {

	private final String version = "AnimationPacker V0.2";
	private final String configPath = "AnimationPacker.cfg";
	private Vector<String> recentFiles = new Vector<String>();
	private Vector<String> lastFiles = new Vector<String>();
	private boolean loadLastFiles = true;
	private boolean alwaysOnTop = false;
	private String theme = "random";
	private int stageWidth = 512;
	private int stageHeight = 512;
	private Color moduleSelectedColor = new Color(200, 255, 0, 128);
	private Color canvasBackgroundColor = new Color(192, 192, 192);
	private Color canvasCellColor = new Color(255, 200, 0);
	private Color canvasBorderColor = new Color(255, 0, 0);
	private Color canvasAxisColor = new Color(0, 0, 255);
	
	private AppConfig() {
	}

	private static AppConfig config;

	public static AppConfig config() {
		if (config == null) {
			config = new AppConfig();
		}
		return config;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public Color getModuleSelectedColor() {
		return moduleSelectedColor;
	}

	public void setModuleSelectedColor(Color moduleSelectedColor) {
		this.moduleSelectedColor = moduleSelectedColor;
	}

	public boolean isAlwaysOnTop() {
		return alwaysOnTop;
	}

	public void setAlwaysOnTop(boolean alwaysOnTop) {
		this.alwaysOnTop = alwaysOnTop;
	}

	public boolean isLoadLastFiles() {
		return loadLastFiles;
	}

	public void setLoadLastFiles(boolean load) {
		this.loadLastFiles = load;
	}

	public void loadConfig() {
		try {
			File file = new File(configPath);
			Element e = Tools.readFromXml(file);
			ElementParser ep = new ElementParser(e);
			theme = ep.getAttributeString("theme");
			alwaysOnTop = ep.getAttributeBoolean("alwaysOnTop");
			loadLastFiles = ep.getAttributeBoolean("loadLastFiles");
			Element stage = ep.getChild("Stage");
			if (stage != null) {
				Element stageSize = stage.getChild("Size");
				int width = Integer.parseInt(stageSize.getAttributeValue("width"));
				int height = Integer.parseInt(stageSize.getAttributeValue("height"));
				setStageWidth(width);
				setStageHeight(height);
			}
			Element files = ep.getChild("RecentFiles");
			if (files != null) {
				List<Element> list = new ElementParser(files).getElements();
				for (Element eFile : list) {
					String path = eFile.getAttributeValue("path");
					File f = Tools.openFile(getCurrentDirectory(), path);
					if (f != null) {
						recentFiles.add(f.getAbsolutePath());
					}
				}
			}
			files = ep.getChild("LastFiles");
			if (files != null) {
				List<Element> list = new ElementParser(files).getElements();
				for (Element eFile : list) {
					String path = eFile.getAttributeValue("path");
					File f = Tools.openFile(getCurrentDirectory(), path);
					if (f != null) {
						lastFiles.add(f.getAbsolutePath());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		Element e = new Element("Config");
		e.setAttribute("theme", theme != null ? theme : "random");
		e.setAttribute("alwaysOnTop", "" + alwaysOnTop);
		e.setAttribute("loadLastFiles", "" + loadLastFiles);
		Element stage = new Element("Stage");
		Element stageSize = new Element("Size");
		stageSize.setAttribute("width", "" + getStageWidth());
		stageSize.setAttribute("height", "" + getStageHeight());
		stage.addContent(stageSize);
		e.addContent(stage);
		
		Element files = new Element("RecentFiles");
		for (String f : recentFiles) {
			if (f == null) {
				continue;
			}
			Element file = new Element("File");
			file.setAttribute("path", f);
			files.addContent(file);
		}
		e.addContent(files);

		files = new Element("LastFiles");
		for (String f : lastFiles) {
			if (f == null) {
				continue;
			}
			Element file = new Element("File");
			file.setAttribute("path", f);
			files.addContent(file);
		}
		e.addContent(files);
		File file = new File(configPath);
		Tools.writeToXml(file, e);
	}

	public Vector<String> getRecentFiles() {
		return recentFiles;
	}

	public void setRecentFiles(Vector<String> recentFiles) {
		this.recentFiles = recentFiles;
	}

	public void addRecentFile(String file) {
		if (this.recentFiles.contains(file)) {
			return;
		}
		this.recentFiles.add(0, file);
		if (this.recentFiles.size() > 10) {
			this.recentFiles.remove(10);
		}
	}

	public Vector<String> getLastFiles() {
		return lastFiles;
	}

	public void setLastFiles(Vector<String> files) {
		this.lastFiles = files;
	}

	public String getCurrentDirectory() {
		if (configPath != null) {
			return Tools.getPath(configPath);
		}
		return System.getProperty("user.dir");
	}

	public String getVersion() {
		return version;
	}

	public int getStageWidth() {
		return stageWidth;
	}

	public void setStageWidth(int stageWidth) {
		this.stageWidth = stageWidth;
	}

	public int getStageHeight() {
		return stageHeight;
	}

	public void setStageHeight(int stageHeight) {
		this.stageHeight = stageHeight;
	}

	public Color getCanvasCellColor() {
		return canvasCellColor;
	}

	public void setCanvasCellColor(Color canvasCellColor) {
		this.canvasCellColor = canvasCellColor;
	}

	public Color getCanvasBackgroundColor() {
		return canvasBackgroundColor;
	}

	public void setCanvasBackgroundColor(Color canvasBackgroundColor) {
		this.canvasBackgroundColor = canvasBackgroundColor;
	}

	public Color getCanvasAxisColor() {
		return canvasAxisColor;
	}

	public void setCanvasAxisColor(Color canvasAxisColor) {
		this.canvasAxisColor = canvasAxisColor;
	}

	public Color getCanvasBorderColor() {
		return canvasBorderColor;
	}

	public void setCanvasBorderColor(Color canvasBorderColor) {
		this.canvasBorderColor = canvasBorderColor;
	}
}
