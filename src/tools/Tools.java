package tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 工具类
 * 
 * @author Rekc@h
 */
public class Tools implements RekcahConst {

	public static void msg(Component parent, final String msg) {
		JOptionPane.showMessageDialog(parent, msg);
	}

	public static String showInputDialog(Component parent, String title,
			String message, int type) {
		return JOptionPane.showInputDialog(parent, message, title, type);
	}

	public static void showImageBox(Component parent, final Image image,
			final String msg, String title) {
		JOptionPane.showMessageDialog(parent, msg, title,
				JOptionPane.OK_CANCEL_OPTION, new ImageIcon(image));
	}

	public static boolean ask(Component parent, final String msg) {
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parent,
				msg, "请确认", JOptionPane.OK_CANCEL_OPTION);
	}

	public static boolean confirm(Component parent, final String msg) {
		return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent,
				msg, "确认下", JOptionPane.YES_NO_OPTION);
	}

	public static void log(final String msg) {
		System.out.println(msg);
	}

	public static void err(final String msg) {
		System.err.println(msg);
	}

	public static void warn(final Throwable t) {
		System.err.println(t.getMessage());
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Tools.warn(e);
		}
	}

	public static void makePanel(JPanel panel, String title, JToolBar toolbar,
			JComponent component) {
		panel.setBorder(new TitledBorder(title));
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		toolbar.setBorder(new EtchedBorder());
		panel.add(toolbar);
		toolbar.setFloatable(false);
		if (!(component instanceof JSplitPane)) {
			component = new JScrollPane(component);
		}
		panel.add(component);
		panel.setLayout(gridbag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		gridbag.setConstraints(toolbar, c);
		c.weighty = 1;
		c.gridy = 1;
		gridbag.setConstraints(component, c);
	}

	// //////////////////////////////////////////////////////////////
	// 文件处理
	public static boolean isWindows() {
		String osName = System.getProperties().getProperty("os.name");
		return osName.toLowerCase().startsWith("windows");
	}

	public static String getCurrentDirectory() {
		return System.getProperties().getProperty("user.dir");
	}

	public static String getPathFromURL(String url) {
		try {
			URL ret = null;
			ret = new URL(url);
			String path = ret.getFile();
			File file = new File(path);
			path = file.getCanonicalPath();
			return path;
		} catch (MalformedURLException e) {
			Tools.warn(e);
		} catch (IOException e) {
			Tools.warn(e);
		}
		return null;
	}

	public static URL getURL(File file) {
		URL ret = null;
		try {
			ret = file.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static BufferedImage getImageByIO(String name) {
		try {
			BufferedImage image = ImageIO.read(name.getClass()
					.getResource(name));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Image getImage(String name) {
		ImageIcon icon = getImageIcon(name);
		Image img = icon.getImage();
		return img;
	}

	public static ImageIcon getImageIcon(String name) {
		return getImageIcon(name, false);
	}

	public static ImageIcon getImageIcon(String name, boolean inJar) {
		if (!(name.startsWith("/") || name.startsWith("\\"))) {
			name = formatPath(File.separator + name);
		}
		ImageIcon icon = null;
		if (inJar) {
			URL url = name.getClass().getResource(name);
			if (url == null) {
				Tools.err("getImageIcon name " + name + "  inJar ");
			}
			icon = new ImageIcon(url);
		} else {
			icon = new ImageIcon(name);
		}
		return icon;
	}

	public static Image getScaledImage(Image icon, int dstW, int dstH) {
		int w = icon.getWidth(null);
		int h = icon.getHeight(null);
		if (w > dstW || h > dstH) {
			if (w > h) {
				icon = icon.getScaledInstance(dstW, -1, Image.SCALE_SMOOTH);
			} else {
				icon = icon.getScaledInstance(-1, dstH, Image.SCALE_SMOOTH);
			}
		}
		return new ImageIcon(icon).getImage();
	}

	public static ImageIcon getScaledImage(ImageIcon icon, int dstW, int dstH) {
		String desc = icon.getDescription();
		icon = new ImageIcon(getScaledImage(icon.getImage(), dstW, dstH));
		icon.setDescription(desc);
		return icon;
	}

	public static Image cutImage(Image srcImage, int startX, int startY,
			int destWidth, int destHeight) {
		try {
			Image destImg = null;
			ImageFilter cropFilter;
			// 四个参数分别为图像起点坐标和宽高
			cropFilter = new CropImageFilter(startX, startY, destWidth,
					destHeight);
			destImg = Toolkit.getDefaultToolkit().createImage(
					new FilteredImageSource(srcImage.getSource(), cropFilter));
			destImg = new ImageIcon(destImg).getImage();
			return destImg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Image cutImage(Image srcImage, Rectangle dst) {
		return cutImage(srcImage, dst.x, dst.y, dst.width, dst.height);
	}

	public static ImageIcon cutImage(ImageIcon srcImage, Rectangle dst) {
		Image image = cutImage(srcImage.getImage(), dst.x, dst.y, dst.width,
				dst.height);
		ImageIcon icon = new ImageIcon(image);
		icon.setDescription(srcImage.getDescription());
		return icon;
	}

	public static Image getTransImage(final Image img, int trans) {
		Image ret = img;
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		switch (trans) {
			case FLIP_X: {
				ret = new BufferedImage(width, height,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2d = (Graphics2D) ret.getGraphics();
				g2d.translate(width, 0);
				g2d.scale(-1, 1);
				g2d.drawImage(img, 0, 0, null);
				g2d.dispose();
				break;
			}
			case FLIP_Y: {
				ret = new BufferedImage(width, height,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2d = (Graphics2D) ret.getGraphics();
				g2d.translate(0, height);
				g2d.scale(1, -1);
				g2d.drawImage(img, 0, 0, null);
				g2d.dispose();
				break;
			}
			case FLIP_X_Y: {
				ret = new BufferedImage(width, height,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2d = (Graphics2D) ret.getGraphics();
				g2d.translate(width, height);
				g2d.scale(-1, -1);
				g2d.drawImage(img, 0, 0, null);
				break;
			}
			case FLIP_90: {
				ret = new BufferedImage(height, width,
						BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g2d = (Graphics2D) ret.getGraphics();
				g2d.rotate(Math.toRadians(90), height, 0);
				g2d.drawImage(img, height, 0, null);
				g2d.dispose();
				break;
			}
			case FLIP_90_X: {
				ret = Tools.getTransImage(img, FLIP_90);
				ret = Tools.getTransImage(ret, FLIP_X);
				break;
			}
			case FLIP_90_Y: {
				ret = Tools.getTransImage(img, FLIP_90);
				ret = Tools.getTransImage(ret, FLIP_Y);
				break;
			}
			case FLIP_90_X_Y: {
				ret = Tools.getTransImage(img, FLIP_90);
				ret = Tools.getTransImage(ret, FLIP_X_Y);
				break;
			}
		}
		return ret;
	}

	public static Color brighter(Color color, double FACTOR) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int i = (int) (1.0 / (1.0 - FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			return new Color(i, i, i);
		}
		if (r > 0 && r < i) {
			r = i;
		}
		if (g > 0 && g < i) {
			g = i;
		}
		if (b > 0 && b < i) {
			b = i;
		}
		return new Color((int) Math.min((r / FACTOR), 255), (int) Math.min(
				(g / FACTOR), 255), (int) Math.min((b / FACTOR), 255));
	}

	public static Color darker(Color color, double FACTOR) {
		return new Color((int) Math.max((color.getRed() * FACTOR), 0),
				(int) Math.max((color.getGreen() * FACTOR), 0), (int) Math.max(
						(color.getBlue() * FACTOR), 0));
	}

	public static Color showSelectColor(Component parent, String title,
			Color preColor) {
		return JColorChooser.showDialog(parent, title, preColor);
	}

	/**
	 * 选择一个文件夹
	 */
	public static File showSelectFolder(Component parent,
			String currentDirectory) {
		File ret = null;
		JFileChooser chooser = new JFileChooser(currentDirectory);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
			ret = chooser.getSelectedFile();
		}
		return ret;
	}

	public static ImageIcon showOpenImage(Component parent,
			final String currentDirectory, final String description,
			final String... extensions) {
		File file = showOpenFile(parent, currentDirectory, true, description,
				extensions);
		if (file == null) {
			return null;
		}
		try {
			ImageIcon icon = new ImageIcon(file.toURI().toURL());
			return icon;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formatPath(String path) {
		if (path.toLowerCase().startsWith("file:/")) {
			try {
				URL url = new URL(path);
				File file = new File(url.toURI());
				// if (path.startsWith("/")) { // Windows上多一个这东西
				// path = path.substring(1);
				// }
				path = file.getPath();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		path = path.replace('/', File.separatorChar);
		path = path.replace('\\', File.separatorChar);
		return path;
	}

	/**
	 * 获取Jar所在的位置
	 * 
	 * @param c
	 *            最好是一个窗口的Class
	 * @return
	 */
	public static String getJarLocation(Class<?> c) {
		String location = "";
		CodeSource loc = c.getProtectionDomain().getCodeSource();
		try {
			location = URLDecoder.decode(loc.getLocation().getFile(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tools.warn(e);
		}
		return location;
	}

	/**
	 * 判断当前运行的是jar还是class Class c 最好是一个窗口的Class
	 * 
	 * @return
	 */
	public static boolean isRunJar(Class<?> c) {
		boolean ret = false;
		String location = getJarLocation(c);
		if (location.endsWith("/") || location.endsWith("\\")) { // 不是jar
			ret = false;
		} else { // 是jar
			ret = true;
		}
		return ret;
	}

	/**
	 * 枚举Jar包中的URL，支持class运行模式
	 * 
	 * @param c
	 *            最好是一个本地类的Class,例如MyFrame.class
	 * @param pack
	 *            包名，例如"/res/"
	 * @return URL列表
	 */
	public static Vector<URL> getJarFiles(Class<?> c, String pack) {
		Vector<URL> ret = new Vector<URL>();
		// 在包前面加一个斜线，变成标准包路径
		if (!pack.startsWith("\\") && !pack.startsWith("/")) {
			pack = "/" + pack;
		}
		String location = getJarLocation(c);
		try {
			if (location.endsWith("/") || location.endsWith("\\")) { // 不是jar
				File dir = new File(location + pack);
				if (dir.isDirectory()) {
					for (File f : dir.listFiles()) {
						ret.add(f.toURI().toURL());
					}
				}
			} else { // 是jar
				JarFile file = new JarFile(location);
				Enumeration<JarEntry> e = file.entries();
				while (e.hasMoreElements()) {
					ZipEntry j = e.nextElement();
					if (!j.isDirectory() && j.getName().startsWith(pack)) {
						ret.add(c.getResource(j.getName()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 打开文件对话框
	 */
	public static File showOpenFile(Component parent,
			final String currentDirectory, boolean preview,
			final String description, final String... extensions) {
		JFileChooser chooser = showFileChooser(currentDirectory, preview,
				false, description, extensions);
		File file = null;
		if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
			file = chooser.getSelectedFile();
		}
		return file;
	}

	/**
	 * 打开多文件对话框
	 */
	public static File[] showOpenFiles(Component parent,
			final String currentDirectory, boolean preview,
			final String description, final String... extensions) {
		JFileChooser chooser = showFileChooser(currentDirectory, preview, true,
				description, extensions);
		File[] files = null;
		if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
			files = chooser.getSelectedFiles();
		}
		return files;
	}

	/**
	 * 显示打开文件对话框，返回选择器
	 * 
	 * @param currentDirectory
	 * @param preview
	 * @param description
	 * @param extensions
	 * @return
	 */
	public static JFileChooser showFileChooser(final String currentDirectory,
			boolean preview, boolean multiSelection, final String description,
			final String... extensions) {
		JFileChooser chooser = new JFileChooser(currentDirectory);
		if (preview) {
			PicturePreviewer viewer = new PicturePreviewer();
			chooser.setAccessory(viewer);
			chooser.addPropertyChangeListener(viewer);
		}
		chooser.setMultiSelectionEnabled(multiSelection);
		chooser.setFileFilter(new FileNameExtensionFilter(description,
				extensions));
		return chooser;
	}

	/**
	 * 保存文件对话框
	 */
	public static File showSaveFile(Component parent,
			final String currentDirectory, File preFile,
			final String description, String extension) {
		if (currentDirectory != null) {
			Tools.createFolder(currentDirectory);
		}
		JFileChooser chooser = new JFileChooser(currentDirectory);
		chooser.setSelectedFile(preFile);
		chooser.setFileFilter(new FileNameExtensionFilter(description,
				extension));
		File file = null;
		if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(parent)) {
			file = chooser.getSelectedFile();
			if (!extension.startsWith(".")) {
				extension = "." + extension;
			}
			if (!file.getPath().endsWith(extension)) {
				file = new File(file.getPath() + extension);
			}
		}
		return file;
	}

	public static String readText(String path) {
		try {
			InputStreamReader reader = new InputStreamReader(path.getClass()
					.getResourceAsStream(path), "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();
			String text = null;
			String nextLine = System.getProperty("line.separator");
			while ((text = br.readLine()) != null) {
				sb.append(text);
				sb.append(nextLine);
			}
			return sb.toString();
		} catch (IOException ex) {
			Tools.warn(ex);
		}
		return null;
	}

	public static File openFile(final String parent, final String path) {
		File ret;
		if (!isWindows() || path.contains(":")) {
			ret = new File(path);
		} else {
			File p = new File(parent);
			if (!p.isDirectory()) {
				createFolder(parent);
			}
			ret = new File(parent, path);
		}
		return ret;
	}

	/**
	 * 判断文件夹是否为空
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFileExist(String path, String... ext) {
		File p = new File(path);
		if (p == null || !p.isDirectory()) {
			return false;
		}
		File[] files = Tools.getFiles(p, false, ext);
		if (files == null || files.length == 0) {
			return false;
		}
		return true;
	}

	/**
	 * 判断文件夹是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFolderExist(String path) {
		File f = new File(path);
		if (f == null || !f.isDirectory()) {
			return false;
		}
		return true;
	}

	/**
	 * 打开文件
	 * 
	 * @param file
	 *            文件名
	 * @param inJar
	 *            是否在jar包中
	 * @return 返回输入流
	 */
	public static InputStream openFileAsStream(String file, boolean inJar) {
		InputStream is = null;
		try {
			if (inJar) {
				is = file.getClass().getResourceAsStream(file);
			} else {
				is = new FileInputStream(file);
			}
		} catch (FileNotFoundException e) {
			Tools.warn(e);
		}
		return is;
	}

	/**
	 * 弹出浏览器
	 * 
	 * @param htmlFile
	 */
	public static void showBrowser(Component parent, String htmlFile) {
		Tools.log("showBrowser: " + htmlFile);
		try {
			File file = new File(htmlFile);
			if (file == null || !file.exists()) {
				Tools.msg(parent, "找不到文件" + htmlFile);
				return;
			}
			URI uri = new File(htmlFile).toURI();
			Desktop.getDesktop().browse(uri);
		} catch (IOException e2) {
			Tools.warn(e2);
		}
	}

	/**
	 * 创建文件夹
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createFolder(final String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		boolean ret = dir.isDirectory();
		if (!ret) {
			err("createDirectory failed " + path);
		}
		return ret;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param dir
	 */
	public static boolean deleteFolder(String dir) {
		boolean ret;
		try {
			File delfolder = new File(dir);
			if (!delfolder.exists()) {
				ret = true;
			} else {
				File oldFile[] = delfolder.listFiles();
				for (int i = 0; i < oldFile.length; i++) {
					if (oldFile[i].isDirectory()) {
						deleteFolder(dir + oldFile[i].getName()
								+ File.separator); // 递归清空子文件夹
					}
					oldFile[i].delete();
				}
				ret = true;
			}
		} catch (Exception e) {
			Tools.warn(e);
			ret = false;
		}
		return ret;
	}

	public static boolean createFile(final String path) {
		File file = new File(path);
		return createFile(file);
	}

	public static boolean createFile(final File file) {
		try {
			if (file.exists()) {
				file.delete();
			}
			boolean ret = file.createNewFile();
			if (!ret) {
				err("createFile failed " + file);
			}
			return ret;
		} catch (IOException ex) {
			err("createFile failed " + file);
		}
		return false;
	}

	public static String getPath(String file) {
		int index = file.lastIndexOf(File.separator);
		if (index < 0) {
			return file;
		}
		return file.substring(0, index);
	}

	public static String getFileName(String file) {
		return getFileName(file, true);
	}

	public static String getFileName(String file, boolean withExt) {
		int index = file.lastIndexOf(File.separator);
		if (withExt) {
			return file.substring(index + 1);
		} else {
			int lastIndex = file.lastIndexOf('.');
			return file.substring(index + 1, lastIndex);
		}
	}

	public static String getRelativePath(final String base, final String path) {
		String ret = getRelativePath(new File(base), new File(path)).toString();
		return ret;
	}

	/**
	 * 获得相对路径, 仅限Windows版
	 * 
	 * @param base
	 * @param path
	 * @return
	 */
	public static File getRelativePath(File base, File path) {
		try {
			if (!base.isDirectory()) {
				base = base.getParentFile();
			}
			String a = base.getCanonicalFile().toURI().getPath();
			String b = path.getCanonicalFile().toURI().getPath();
			if (b.substring(0, 3).equals(a.substring(0, 3))) { // 盘符一致
				int index = b.indexOf(a);
				if (index == -1) { // 不是子目录
					String[] basePaths = a.split("/");
					String[] otherPaths = b.split("/");
					int n = 0;
					for (; n < basePaths.length && n < otherPaths.length; n++) {
						if (basePaths[n].equals(otherPaths[n]) == false) {
							break;
						}
					}
					StringBuilder tmp = new StringBuilder("../");
					for (int m = n; m < basePaths.length - 1; m++) {
						tmp.append("../");
					}
					for (int m = n; m < otherPaths.length; m++) {
						tmp.append(otherPaths[m]);
						tmp.append("/");
					}
					return new File(tmp.toString());
				} else { // 是子目录
					return new File(b.substring(index + a.length()));
				}
			} else {
				return new File(b);
			}
		} catch (IOException ex) {
			Tools.warn(ex);
		}
		return null;
	}

	/**
	 * 获取子文件
	 * 
	 * @param parentDir
	 *            父文件夹
	 * @param withDir
	 *            是否反回子文件夹
	 * @param extensions
	 *            扩展名
	 * @return
	 */
	public static File[] getFiles(File parentDir, final boolean withDir,
			String... extensions) {
		File[] ret = new File[0];
		if (parentDir == null || !parentDir.exists()
				|| !parentDir.isDirectory()) {
			return ret;
		}
		final Vector<String> vectorExtensions = new Vector<String>();
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i] != null && extensions[i].length() > 0) {
				vectorExtensions.add(extensions[i].toLowerCase(Locale.ENGLISH));
			}
		}
		if (vectorExtensions.size() <= 0) {
			return ret;
		}
		ret = parentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f != null) {
					if (withDir && f.isDirectory()) {
						return true;
					}
					String fileName = f.getName();
					int i = fileName.lastIndexOf('.');
					if (i > 0 && i < fileName.length() - 1) {
						String desiredExtension = fileName.substring(i + 1)
								.toLowerCase(Locale.ENGLISH);
						for (int j = 0; j < vectorExtensions.size(); j++) {
							String extension = vectorExtensions.get(j);
							if (desiredExtension.equals(extension)) {
								return true;
							}
						}
					}
				}
				return false;
			}
		});
		return ret;
	}

	public static Element readFromXml(File file) {
		if (file == null || !file.exists() || file.isDirectory()) {
			Tools.err("文件打不开" + file);
			return null;
		}
		Element e = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			builder.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
			Document doc = builder.build(file);
			e = doc.getRootElement();// 得到根
		} catch (JDOMException e1) {
			Tools.warn(e1);
		} catch (IOException e1) {
			Tools.warn(e1);
		} catch (Exception e1) {
			Tools.warn(e1);
		}
		if (e == null) {
			Tools.err("readFromXml error " + file);
		}
		return e;
	}

	public static boolean writeToXml(File file, Element e) {
		// 写入文件
		Document xml = new Document(e);// 创建以project为根结点的xml文件
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			if (!file.exists()) {
				if (!Tools.createFile(file)) {
					Tools.err("创建文件失败!");
					return false;
				}
			}
			FileOutputStream fos = new FileOutputStream(file);
			outputter.output(xml, fos);
			fos.flush();
			fos.close();
			return true;
		} catch (FileNotFoundException e1) {
			Tools.warn(e1);
		} catch (IOException e1) {
			Tools.warn(e1);
		}
		return false;
	}

	public static String removeLastChar(String src, String last) {
		String ret = src;
		if (src.endsWith(last)) {
			ret = src.substring(0, src.length() - 1);
		}
		return ret;
	}

	// 文件处理
	// //////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////
	// 数学工具
	public static String getRectangleString(Rectangle r) {
		return r.x + "," + r.y + "," + r.width + "," + r.height;
	}

	public static Rectangle getRectangleFromString(String s) {
		int[] r = getIntArray(s);
		Rectangle ret = new Rectangle(r[0], r[1], r[2], r[3]);
		return ret;
	}

	public static String getHexString(int value) {
		String ret = String.format("%1$#010x", value);
		return ret;
	}

	public static int getHexNumber(String str) {
		str = str.toLowerCase().replace("0x", "");
		int ret = Integer.parseInt(str, 16);
		return ret;
	}

	private static Random rand = new Random(System.currentTimeMillis());

	/**
	 * 返回不小于0且不大于指定数的一个随机数值
	 * 
	 * @param max
	 *            int 指定数
	 * @return
	 */
	public static int random(final int max) {
		return random(0, max);
	}

	/**
	 * 返回给定范围内的一个随机数值
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int random(int min, int max) {
		if (max < min) {
			int tmp = max;
			max = min;
			min = tmp;
		}
		int num = Math.abs(rand.nextInt()) % (max - min + 1);
		return num + min;
	}

	/**
	 * 取得一个限制范围内的数值
	 * 
	 * @param num
	 * @param min
	 * @param max
	 * @return
	 */
	public static int limit(final int num, int min, int max) {
		if (min > max) {
			int tmp = max;
			max = min;
			min = tmp;
		}
		if (num > max) {
			return max;
		} else if (num < min) {
			return min;
		} else {
			return num;
		}
	}

	public static int limit(final int num, final int max) {
		return limit(num, 0, max);
	}

	/**
	 * 填充数组
	 * 
	 * @param array
	 * @param value
	 */
	public static void fillArray(final int[] array, final int value) {
		Arrays.fill(array, value);
	}

	/**
	 * 填充2维表
	 * 
	 * @param a2d
	 * @param value
	 */
	public static void fillArray2D(final int[][] a2d, final int value) {
		for (int i = 0; i < a2d.length; i++) {
			fillArray(a2d[i], value);
		}
	}

	public static int[][] resizeArray2D(final int[][] a2d, final int len1,
			final int len2, final int nullValue) {
		int[][] old = a2d;
		int[][] ret = newArray2D(len1, len2, nullValue);
		if (old == null) {
			return ret;
		}
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[i].length; j++) {
				if (i < old.length && j < old[i].length) {
					ret[i][j] = old[i][j];
				}
			}
		}
		return ret;
	}

	/**
	 * 创建一个数组,并填充指定数值
	 * 
	 * @param len
	 * @param value
	 * @return
	 */
	public static int[] newArray(final int len, final int value) {
		int[] array = new int[len];
		fillArray(array, value);
		return array;
	}

	/**
	 * 创建一个二维表,并填充指定数值
	 * 
	 * @param len1
	 * @param len2
	 * @param value
	 * @return
	 */
	public static int[][] newArray2D(final int len1, final int len2,
			final int value) {
		int[][] array = new int[len1][len2];
		for (int i = 0; i < array.length; i++) {
			array[i] = newArray(len2, value);
		}
		return array;
	}

	public static int[][] fillArray1DToArray2D(final int[] a1d, final int len1,
			final int len2) {
		if (len1 * len2 > a1d.length) {
			Tools.log("fillArray1DToArray2D  out of bounds: " + a1d.length
					+ "  " + len1 + "x" + len2);
		}
		int[][] array = new int[len1][len2];
		for (int i = 0; i < len1; i++) {
			array[i] = new int[len2];
			for (int j = 0; j < len2; j++) {
				array[i][j] = a1d[i * len2 + j];
			}
		}
		return array;
	}

	/**
	 * 在数组中搜索指定数值
	 * 
	 * @param array
	 * @param value
	 * @return
	 */
	public static int getIndexFromArray(final int[] array, final int value) {
		return Arrays.binarySearch(array, value);
	}

	/**
	 * 比较两数组是否相等
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean deepEquals(final Object[] array1,
			final Object[] array2) {
		return Arrays.deepEquals(array1, array2);
	}

	public static boolean isInIntArray(final int num, final int[] array) {
		if (array == null) {
			return false;
		}
		for (int i = 0; i < array.length; i++) {
			if (num == array[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 字符串转换成数组
	 */
	public static int[] getIntArray(final String att) {
		String str = att;
		if (str == null) {
			return null;
		}
		if (str.equals("")) {
			return new int[0];
		}
		int count = 0;
		while (str.indexOf(",") != -1) {
			str = str.substring(str.indexOf(",") + 1);
			++count;
		}
		++count;
		int[] list = new int[count];
		str = att;
		for (int i = 0; i < list.length - 1; ++i) {
			try {
				list[i] = Integer.parseInt(str.substring(0, str.indexOf(",")));
			} catch (NumberFormatException ex1) {
				return new int[0];
			}
			str = str.substring(str.indexOf(",") + 1);
		}
		try {
			list[(list.length - 1)] = Integer.parseInt(str);
		} catch (NumberFormatException ex1) {
			return new int[0];
		}
		return list;
	}

	public static long getNumber(String str) {
		long num = 0;
		try {
			if (str.toLowerCase().startsWith("0x")) {
				num = Long.parseLong(str.substring(2), 16);
			} else {
				num = Long.parseLong(str);
			}
		} catch (Exception e) {
			Tools.warn(e);
		}
		return num;
	}

	public static boolean getBoolean(String str) {
		boolean b = false;
		try {
			b = Boolean.parseBoolean(str);
		} catch (Exception e) {
			Tools.warn(e);
		}
		return b;
	}

	public static Point convertStr2Point(String str) {
		str = str.replace("{", "");
		str = str.replace("}", "");
		String[] list = str.split(",");
		int x = Integer.parseInt(list[0].trim());
		int y = Integer.parseInt(list[1].trim());
		Point point = new Point(x, y);
		return point;
	}

	public static Dimension convertStr2Size(String str) {
		Point tmp = convertStr2Point(str);
		Dimension size = new Dimension(tmp.x, tmp.y);
		return size;
	}

	public static Rectangle convertStr2Rect(String str) {
		str = str.replace("{", "");
		str = str.replace("}", "");
		String[] list = str.split(",");
		int x = Integer.parseInt(list[0].trim());
		int y = Integer.parseInt(list[1].trim());
		int w = Integer.parseInt(list[2].trim());
		int h = Integer.parseInt(list[3].trim());
		Rectangle rect = new Rectangle(x, y, w, h);
		return rect;
	}

	// 数学工具
	// //////////////////////////////////////////////////////////////
	/**
	 * 完全展开一个JTree
	 * 
	 * @param tree
	 *            JTree
	 */
	public static void expandTree(JTree tree) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		if (root != null) {
			expandAll(tree, new TreePath(root), true);
		}
	}

	/**
	 * 完全展开或关闭一个树,用于递规执行
	 * 
	 * @param tree
	 *            JTree
	 * @param parent
	 *            父节点
	 * @param expand
	 *            为true则表示展开树,否则为关闭整棵树
	 */
	public static void expandAll(JTree tree, TreePath parent, boolean expand) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}
}
