package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.undo.UndoManager;

import model.FLAnimation;
import tools.RekcahAction;
import tools.Tools;

import com.jtattoo.plaf.JTattooUtilities;

/**
 * Main Frame
 * @author Rekc@h
 */
public class MainFrame extends JFrame {
	public static void main(String[] args) {
		AppConfig config = AppConfig.config();
		config.loadConfig();
		String theme = config.getTheme();
		int index = Tools.random(0, THEMES.length - 1);
		for (int i = 0; i < THEMES.length; i++) {
			if (THEMES[i][0].equals(theme)) {
				index = i;
				break;
			}
		}
		setTheme(THEMES[index][1]);
		new MainFrame();
	}

	public final static String[][] THEMES = {
			{ "Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel" },
			{ "Areo", "com.jtattoo.plaf.aero.AeroLookAndFeel" },
			{ "Aluminium", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel" },
			{ "Bernstein", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel" },
			{ "Fast", "com.jtattoo.plaf.fast.FastLookAndFeel" },
			{ "Graphite", "com.jtattoo.plaf.graphite.GraphiteLookAndFeel" },
			{ "HiFi", "com.jtattoo.plaf.hifi.HiFiLookAndFeel" },
			{ "Luna", "com.jtattoo.plaf.luna.LunaLookAndFeel" },
			{ "McWin", "com.jtattoo.plaf.mcwin.McWinLookAndFeel" },
			{ "Mint", "com.jtattoo.plaf.mint.MintLookAndFeel" },
			{ "Noire", "com.jtattoo.plaf.noire.NoireLookAndFeel" },
			{ "Smart", "com.jtattoo.plaf.smart.SmartLookAndFeel" },
			{ "Texture", "com.jtattoo.plaf.texture.TextureLookAndFeel" } };

	public static void setTheme(final String themeClass) {
		try {
			Class.forName(themeClass).getMethod("setTheme", String.class)
					.invoke(null, "Default");
			UIManager.setLookAndFeel(themeClass);
			if (JTattooUtilities.getJavaVersion() >= 1.6D) {
				Window[] windows = Window.getWindows();
				for (int i = 0; i < windows.length; i++) {
					if (windows[i].isDisplayable()) {
						SwingUtilities.updateComponentTreeUI(windows[i]);
					}
				}
			} else {
				Frame[] frames = Frame.getFrames();
				for (int i = 0; i < frames.length; i++) {
					if (frames[i].isDisplayable()) {
						SwingUtilities.updateComponentTreeUI(frames[i]);
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private final JTabbedPane tabManager;
	private final WavePanel wavePanel;
	private int newFileIndex;
	private JMenu menuRecentFiles;

	public MainFrame() {
		final AppConfig config = AppConfig.config();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setLocationByPlatform(true);
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('F');
		menuFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateRecentFilesMenu();
			}
		});
		menuFile.add(actionNewAnimation);
		menuFile.add(actionOpenAnimation);
		menuRecentFiles = new JMenu("Recent Files");
		menuFile.add(menuRecentFiles);
		menuFile.addSeparator();
		menuFile.add(actionSave);
		menuFile.add(actionSaveAs);
		menuFile.addSeparator();
		menuFile.add(actionClose);
		menuFile.add(actionCloseAll);
		menuFile.addSeparator();
		actionLoadLastFiles.setSelected(config.isLoadLastFiles());
		JMenuItem itemLoadLastFiles = new JCheckBoxMenuItem(actionLoadLastFiles);
		menuFile.add(itemLoadLastFiles);
		menuFile.addSeparator();
		menuFile.add(actionExit);
		menuBar.add(menuFile);

		JMenu menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic('E');
		menuEdit.add(actionUndo);
		menuEdit.add(actionRedo);
		menuBar.add(menuEdit);
		JMenu menuView = new JMenu("View");
		menuView.setMnemonic('V');
		actionAlwaysOnTop.setSelected(config.isAlwaysOnTop());
		JMenuItem itemAlwaysOnTop = new JCheckBoxMenuItem(actionAlwaysOnTop);
		menuView.add(itemAlwaysOnTop);
		menuView.addSeparator();
		menuView.add(actionPlay);
		menuView.add(actionPlayNext);
		menuBar.add(menuView);

		JMenu menuLAF = new JMenu("Theme");
		menuLAF.setMnemonic('T');
		ButtonGroup groupLaf = new ButtonGroup();
		for (int i = 0; i < THEMES.length; i++) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(THEMES[i][0]);
			menuLAF.add(item);
			groupLaf.add(item);
			final String themeName = THEMES[i][0];
			final String themeClass = THEMES[i][1];
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setTheme(themeClass);
					config.setTheme(themeName);
				}
			});
			if (item.getText().equals(config.getTheme())) {
				item.setSelected(true);
			}
		}
		menuBar.add(menuLAF);

		JMenu menuHelp = new JMenu("Help");
		menuHelp.setMnemonic('H');
		JMenuItem menuItemAbout = new JMenuItem("About...");
		menuItemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(MainFrame.this);
			}
		});
		menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuHelp.add(menuItemAbout);
		menuBar.add(menuHelp);

		JToolBar toolbar = new JToolBar();
		toolbar.add(actionNewAnimation);
		toolbar.add(actionOpenAnimation);
		toolbar.add(actionSave);
		toolbar.addSeparator();
		toolbar.add(actionUndo);
		toolbar.add(actionRedo);
		toolbar.addSeparator();
		toolbar.add(actionPlay);
		toolbar.add(actionPlayNext);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		wavePanel = new WavePanel("/res/logo.jpg");
		tabManager = new JTabbedPane() {
			@Override
			public Color getBackgroundAt(int index) {
				if (getTabCount() <= 0) {
					return null;
				}
				return super.getBackgroundAt(index);
			}

			@Override
			protected void fireStateChanged() {
				super.fireStateChanged();
				onTabCountChanged();
			}
		};
		getContentPane().add(tabManager);
		wavePanel.setVisible(false);
		setSize(1000, 800);
		this.setVisible(true);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionExit.actionPerformed(null);
			}
		});
		if (config.isLoadLastFiles()) {
			Vector<String> paths = config.getLastFiles();
			for (String path : paths) {
				createAnimation(Tools.openFile(config.getCurrentDirectory(),
						path));
			}
		}
		updateRecentFilesMenu();
		onTabCountChanged();
	}

	private void onTabCountChanged() {
		refreshActions();
		if (tabManager.getTabCount() > 0) {
			if (tabManager.getParent() == null) {
				getContentPane().remove(wavePanel);
				getContentPane().add(tabManager);
				tabManager.setVisible(true);
				wavePanel.setVisible(false);
			}
		} else {
			if (wavePanel.getParent() == null) {
				getContentPane().remove(tabManager);
				getContentPane().add(wavePanel);
				tabManager.setVisible(false);
				wavePanel.setVisible(true);
			}
		}
	}

	public void updateRecentFilesMenu() {
		AppConfig config = AppConfig.config();
		if (config.getRecentFiles().size() > 0) {
			for (final String file : config.getRecentFiles()) {
				JMenuItem item = new JMenuItem(file);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						createAnimation(Tools.openFile(null, file));
					}
				});
				menuRecentFiles.add(item);
			}
		} else {
			menuRecentFiles.setEnabled(false);
		}
	}

	public AnimEditor currentEditor() {
		AnimEditor editor = (AnimEditor) tabManager.getSelectedComponent();
		return editor;
	}

	public AnimEditor getEditorByFile(String file) {
		for (int i = 0; i < tabManager.getTabCount(); i++) {
			AnimEditor editor = (AnimEditor) tabManager.getComponentAt(i);
			if (file.equals(editor.getAnimation().getFilePath())) {
				return editor;
			}
		}
		return null;
	}

	public FLAnimation createAnimation(File file) {
		FLAnimation a = new FLAnimation();
		if (file != null) {
			if(!file.exists()) {
				Tools.msg(MainFrame.this, "File " + file.getAbsolutePath()
						+ " can't be opened.");
				return null;
			}else if (getEditorByFile(file.getAbsolutePath()) != null) {
				Tools.msg(MainFrame.this, "File " + file.getAbsolutePath()
						+ " has been opened now.");
				return null;
			}
			a.load(file);
			AppConfig.config().addRecentFile(file.getPath());
		} else {
			newFileIndex++;
			String title = "untitled_" + newFileIndex;
			a.setName(title);
		}
		final AnimEditor editor = new AnimEditor(a,
				new UndoManagerStateListener() {
					@Override
					public void stateChanged() {
						refreshActions();
					}
				});
		int index = Tools.limit(tabManager.getSelectedIndex() + 1, 0,
				tabManager.getTabCount());
		tabManager
				.insertTab(null, null, editor, "Double click to Close", index);
		//add tab label
		JPanel tab = new JPanel(new BorderLayout());
		tab.setOpaque(false);
		JLabel lblTitle = new JLabel(a.getName());
		Icon icon = Tools.getImageIcon("/res/closeBlue.png", true);
		JLabel btnClose = new JLabel(icon);
		btnClose.setToolTipText("Click to close");
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				editor.close();
			}
		});
		tab.add(lblTitle, BorderLayout.CENTER);
		tab.add(btnClose, BorderLayout.EAST);
		tabManager.setTabComponentAt(index, tab);
		tabManager.setSelectedIndex(index);
		return editor.getAnimation();
	}

	public void refreshActions() {
		AnimEditor editor = currentEditor();
		if (editor != null) {
			UndoManager um = editor.getUndoManager();
			actionClose.setEnabled(true);
			actionUndo.setEnabled(um.canUndo());
			actionRedo.setEnabled(um.canRedo());
			actionUndo.setTooltip(um.getUndoPresentationName());
			actionRedo.setTooltip(um.getRedoPresentationName());
			actionSave.setEnabled(editor.isModified());
			actionSaveAs.setEnabled(true);
			actionPlay.setEnabled(true);
			actionPlay.setSelected(editor.isPlaying());
			actionPlayNext.setEnabled(true);
			setTitle(editor.getAnimation().getName()
					+ (editor.isModified() ? "* -  " : " - ")
					+ AppConfig.config().getVersion());
		} else {
			actionClose.setEnabled(false);
			actionUndo.setEnabled(false);
			actionRedo.setEnabled(false);
			actionUndo.setTooltip(null);
			actionRedo.setTooltip(null);
			actionSave.setEnabled(false);
			actionSaveAs.setEnabled(false);
			actionPlay.setEnabled(false);
			actionPlayNext.setEnabled(false);
			setTitle(AppConfig.config().getVersion());
		}
	}

	public RekcahAction actionLoadLastFiles = new RekcahAction("LoadLastFiles") {
		@Override
		public void init() {
			setSelected(AppConfig.config().isLoadLastFiles());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
		}
	};

	public RekcahAction actionAlwaysOnTop = new RekcahAction("AlwaysOnTop",
			"/res/pin.png") {
		@Override
		public void init() {
			setSelected(AppConfig.config().isAlwaysOnTop());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean b = isSelected();
			MainFrame.this.setAlwaysOnTop(b);
		}
	};

	RekcahAction actionNewAnimation = new RekcahAction("New",
			"/res/filenew.png") {

		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
					KeyEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			createAnimation(null);
		}
	};
	RekcahAction actionOpenAnimation = new RekcahAction("Open",
			"/res/fileopen.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
					KeyEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String currentPath = "E:/Projects/busy";
			File[] files = Tools.showOpenFiles(MainFrame.this, currentPath,
					false, "Animation File(.fan)", "fan");
			if (null == files || files.length <= 0) {
				return;
			}
			for (File file : files) {
				createAnimation(file);
			}
		}
	};
	RekcahAction actionSave = new RekcahAction("Save", "/res/filesave.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					KeyEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			currentEditor().save();
		}
	};
	RekcahAction actionSaveAs = new RekcahAction("SaveAs...") {

		@Override
		public void actionPerformed(ActionEvent e) {
			currentEditor().saveAs();
		}
	};
	RekcahAction actionClose = new RekcahAction("Close") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
					KeyEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AnimEditor editor = currentEditor();
			if (editor.isModified()) {
				int ret = JOptionPane.showConfirmDialog(MainFrame.this,
						"This file has not been saved, save it？", "Confirm",
						JOptionPane.YES_NO_CANCEL_OPTION);
				switch (ret) {
					case JOptionPane.CANCEL_OPTION:
						return;
					case JOptionPane.YES_OPTION:
						if (!currentEditor().save()) {
							return;
						}
						break;
					case JOptionPane.NO_OPTION:
						break;
				}
			}
			editor.close();
		}
	};

	protected void saveConfig() {
		AppConfig config = AppConfig.config();
		config.setAlwaysOnTop(actionAlwaysOnTop.isSelected());
		config.setLoadLastFiles(actionLoadLastFiles.isSelected());
		Vector<String> files = new Vector<String>();
		for (int i = 0; i < tabManager.getTabCount(); i++) {
			Component c = tabManager.getComponentAt(i);
			if (c instanceof AnimEditor) {
				AnimEditor editor = (AnimEditor) c;
				String path = editor.getAnimation().getFilePath();
				files.add(path);
			}
		}
		config.setLastFiles(files);
		config.saveConfig();
	}

	public boolean closeAllFile() {
		Vector<AnimEditor> editors = new Vector<AnimEditor>();
		for (int i = 0; i < tabManager.getTabCount(); i++) {
			Component c = tabManager.getComponentAt(i);
			if (c instanceof AnimEditor) {
				AnimEditor editor = (AnimEditor) c;
				if (editor.isModified()) {
					editors.add(editor);
				}
			}
		}
		if (editors.size() > 0) {
			int ret = JOptionPane.showConfirmDialog(MainFrame.this,
					"Some files has not been saved, save them？", "Confirm",
					JOptionPane.YES_NO_CANCEL_OPTION);
			switch (ret) {
				case JOptionPane.CANCEL_OPTION:
					return false;
				case JOptionPane.YES_OPTION:
					for (AnimEditor editor : editors) {
						if (!editor.save()) {
							return false;
						}
					}
					break;
				case JOptionPane.NO_OPTION:
					break;
			}
		}
		for (int i = tabManager.getTabCount() - 1; i >= 0; i--) {
			Component c = tabManager.getComponentAt(i);
			if (c instanceof AnimEditor) {
				AnimEditor editor = (AnimEditor) c;
				editor.close();
			}
		}
		return true;
	}

	RekcahAction actionCloseAll = new RekcahAction("CloseAll") {
		@Override
		public void actionPerformed(ActionEvent e) {
			closeAllFile();
		}
	};

	RekcahAction actionExit = new RekcahAction("Exit") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			saveConfig();
			if (!closeAllFile()) {
				return;
			}
			MainFrame.this.dispose();
		}
	};
	RekcahAction actionUndo = new RekcahAction("Undo", "/res/undo.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
					KeyEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			UndoManager um = currentEditor().getUndoManager();
			um.undo();
		}
	};
	RekcahAction actionRedo = new RekcahAction("Redo", "/res/redo.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
					KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			UndoManager um = currentEditor().getUndoManager();
			um.redo();
		}
	};
	RekcahAction actionPlay = new RekcahAction("Play", "/res/play.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
			setSelected(false);
		}

		@Override
		public void setSelected(boolean select) {
			super.setSelected(select);
			if (select) {
				setName("Pause");
				setIcon(Tools.getImageIcon("/res/pause.png", true));
			} else {
				setName("Play");
				setIcon(Tools.getImageIcon("/res/play.png", true));
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AnimEditor editor = currentEditor();
			if (editor.isPlaying()) {
				editor.stopPlay();
				setSelected(false);
			} else {
				editor.startPlay();
				setSelected(true);
			}
		}
	};
	RekcahAction actionPlayNext = new RekcahAction("PlayNext",
			"/res/playnext.png") {
		@Override
		protected void init() {
			super.init();
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
			setSelected(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AnimEditor editor = currentEditor();
			if (editor.isPlaying()) {
				editor.stopPlay();
			}
			editor.playNextFrame();
		}
	};
}
