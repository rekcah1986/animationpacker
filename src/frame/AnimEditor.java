package frame;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import model.FLAction;
import model.FLAnimation;
import model.FLClip;
import model.FLFrame;
import model.FLModule;
import model.FLPlist;
import model.FLSequence;
import tools.FLTreeNode;
import tools.RekcahAction;
import tools.RekcahUndoableEdit;
import tools.Tools;

interface UndoManagerStateListener {
	abstract void stateChanged();
}

public class AnimEditor extends JPanel {
	private final FLAnimation animation;
	private final JTable tableActions;
	private final JTable tableFrames;
	private final JTable tableSequence;
	private final JTable tableModule;
	private final JList listPlist;
	private final JTree treePlist;
	private final DefaultTreeModel treePlistModel;
	private final FLActionTableModel actionTableModel;
	private final FLSequenceTableModel sequenceTableModel;
	private final FLFrameTableModel frameTableModel;
	private final FLModuleTableModel moduleTableModel;
	private final ModuleCanvas moduleCanvas;
	private final UndoManager undoManager;
	private final UndoManagerStateListener undoManagerListener;
	private boolean modified;

	public AnimEditor(FLAnimation a,
			final UndoManagerStateListener undoManagerListener) {
		animation = a;
		this.undoManagerListener = undoManagerListener;
		undoManager = new UndoManager() {
			@Override
			public synchronized boolean addEdit(UndoableEdit anEdit) {
				boolean ret = super.addEdit(anEdit);
				if (!modified) {
					setModified(true);
				}
				stopPlay();
				if (undoManagerListener != null) {
					undoManagerListener.stateChanged();
				}
				return ret;
			}

			@Override
			public synchronized void undo() throws CannotUndoException {
				super.undo();
				if (!modified) {
					setModified(true);
				}
				stopPlay();
				if (undoManagerListener != null) {
					undoManagerListener.stateChanged();
				}
			}

			@Override
			public synchronized void redo() throws CannotRedoException {
				super.redo();
				if (!modified) {
					setModified(true);
				}
				stopPlay();
				if (undoManagerListener != null) {
					undoManagerListener.stateChanged();
				}
			}
		};
		tableActions = new JTable();
		tableFrames = new JTable();
		tableSequence = new JTable();
		tableModule = new JTable();
		listPlist = new JList(animation.getPlists());
		treePlist = new JTree();
		moduleCanvas = new ModuleCanvas(this);
		moduleCanvas.setModuleSelectedListener(new ModuleSelectedListener() {
			@Override
			public void atChanged() {
				updateTableModuleSelected();
			}
		});
		listPlist.setModel(new DefaultListModel());
		listPlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		treePlistModel = new DefaultTreeModel(new DefaultMutableTreeNode());
		treePlist.setModel(treePlistModel);
		treePlist.setRootVisible(false);
		treePlist.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(final JTree tree,
					final Object value, final boolean sel,
					final boolean expanded, final boolean leaf, final int row,
					final boolean hasFocus) {
				Component c = super.getTreeCellRendererComponent(tree, value,
						sel, expanded, leaf, row, hasFocus);
				if (leaf && value instanceof FLTreeNode) {
					Icon icon = ((FLTreeNode) value).getIcon();
					((DefaultTreeCellRenderer) c).setIcon(icon);
				}
				return c;
			}
		});
		listPlist.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						if (!listPlist.hasFocus()) {
							return;
						}
						FLPlist plist = (FLPlist) listPlist.getSelectedValue();
						if (plist != null) {
							int row = treePlist.getRowForPath(new TreePath(
									plist.getPath()));
							boolean select = false;
							if (treePlist.getSelectionPath() == null) {
								select = true;
							} else {
								Object[] path = treePlist.getSelectionPath()
										.getPath();
								if (path == null || path[1] != plist) {
									select = true;
								}
							}
							if (select) {
								treePlist.setSelectionRow(row);
								treePlist.scrollRowToVisible(row);
							}
						}
					}
				});
		treePlist.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						TreePath path = treePlist.getSelectionPath();
						if (path == null) {
							return;
						}
						Object obj = path.getPath()[1];
						if (obj instanceof FLPlist) {
							FLPlist plist = (FLPlist) obj;
							listPlist.setSelectedValue(plist, true);
						}
					}
				});
		DragSource dragSourceListPList = new DragSource();
		dragSourceListPList.createDefaultDragGestureRecognizer(listPlist,
				DnDConstants.ACTION_COPY, new DragGestureListener() {
					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						Vector<String> keys = new Vector<String>();
						FLPlist plist = (FLPlist) listPlist.getSelectedValue();
						for (int j = 0; j < plist.getClips().size(); j++) {
							keys.add(plist.getClips().get(j).getKey());
						}
						if (keys.size() <= 0) {
							return;
						}
						Transferable transferable = new ClipTransferable(keys);
						dge.startDrag(DragSource.DefaultCopyDrop, transferable);
					}
				});
		DragSource dragSourceTreePList = new DragSource();
		dragSourceTreePList.createDefaultDragGestureRecognizer(treePlist,
				DnDConstants.ACTION_COPY, new DragGestureListener() {
					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						if (treePlist.getSelectionPath() == null) {
							return;
						}
						TreePath[] path = treePlist.getSelectionPaths();
						Vector<String> keys = new Vector<String>();
						for (int i = 0; i < path.length; i++) {
							TreePath p = path[i];
							Object obj = p.getLastPathComponent();
							if (obj instanceof FLPlist) {
								FLPlist plist = (FLPlist) obj;
								for (int j = 0; j < plist.getClips().size(); j++) {
									keys.add(plist.getClips().get(j).getKey());
								}
							} else if (obj instanceof FLClip) {
								FLClip clip = (FLClip) obj;
								keys.add(clip.getKey());
							}
						}
						if (keys.size() <= 0) {
							return;
						}
						Transferable transferable = new ClipTransferable(keys);
						dge.startDrag(DragSource.DefaultCopyDrop, transferable);
					}
				});
		actionTableModel = new FLActionTableModel(animation.getActions());
		tableActions.setModel(actionTableModel);
		tableActions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableActions.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						atTableActionSelectionChanged(false);
					}
				});
		// FLSequenceTableModel 
		sequenceTableModel = new FLSequenceTableModel();
		tableSequence.setModel(sequenceTableModel);
		tableSequence.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableSequence.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						atTableSequencesSelectionChanged(false);
					}
				});
		frameTableModel = new FLFrameTableModel(animation.getFrames());
		tableFrames.setModel(frameTableModel);
		tableFrames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableFrames.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						atTableFramesSelectionChanged(false);
					}
				});
		DragSource dragSourceTableFrames = new DragSource();
		dragSourceTableFrames.createDefaultDragGestureRecognizer(tableFrames,
				DnDConstants.ACTION_COPY, new DragGestureListener() {
					@Override
					public void dragGestureRecognized(DragGestureEvent dge) {
						int id = tableFrames.getSelectedRow();
						Transferable transferable = new FrameTransferable(id);
						dge.startDrag(DragSource.DefaultCopyDrop, transferable);
					}
				});
		moduleTableModel = new FLModuleTableModel();
		tableModule.setModel(moduleTableModel);
		tableModule.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						atTableModulesSelectionChanged(false);
					}
				});
		this.setLayout(new BorderLayout());
		final JSplitPane split12, split3, splitLeft, splitMiddle, splitRight, splitPlist;
		split12 = new JSplitPane();
		split3 = new JSplitPane();
		splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitMiddle = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPlist = new JSplitPane();
		split3.setLeftComponent(split12);
		this.add(split3, BorderLayout.CENTER);
		split12.setLeftComponent(splitLeft);
		split12.setRightComponent(splitMiddle);
		split3.setRightComponent(splitRight);
		//tableActions
		final JPanel panelTableAction = new JPanel();
		final JPanel panelTableSequence = new JPanel();
		final JPanel panelTableModule = new JPanel();
		final JPanel panelTableFrame = new JPanel();
		final JPanel panelPlistTop = new JPanel();
		final JPanel panelCanvas = new JPanel();
		splitLeft.setTopComponent(panelTableAction);
		splitLeft.setBottomComponent(panelTableSequence);
		splitMiddle.setTopComponent(panelTableModule);
		splitMiddle.setBottomComponent(panelTableFrame);
		splitRight.setTopComponent(panelPlistTop);
		splitRight.setBottomComponent(panelCanvas);
		panelPlistTop.add(splitPlist);
		splitPlist.setLeftComponent(new JScrollPane(listPlist));
		splitPlist.setRightComponent(new JScrollPane(treePlist));
		//Toolbar
		JToolBar tbTableAction = new JToolBar();
		tbTableAction.add(actionNewAction);
		tbTableAction.add(actionDeleteAction);
		tbTableAction.add(actionMoveUpAction);
		tbTableAction.add(actionMoveDownAction);
		tbTableAction.add(actionCloneAction);
		JToolBar tbTableSequence = new JToolBar();
		tbTableSequence.add(actionMoveUpSequence);
		tbTableSequence.add(actionMoveDownSequence);
		tbTableSequence.add(actionDeleteSequence);
		JToolBar tbTableModule = new JToolBar();
		tbTableModule.add(actionMoveUpModule);
		tbTableModule.add(actionMoveDownModule);
		tbTableModule.add(actionDeleteModule);
		JToolBar tbTableFrame = new JToolBar();
		tbTableFrame.add(actionNewFrame);
		tbTableFrame.add(actionDeleteFrame);
		tbTableFrame.add(actionMoveUpFrame);
		tbTableFrame.add(actionMoveDownFrame);
		tbTableFrame.add(actionCloneFrame);
		JToolBar tbTreePlist = new JToolBar();
		tbTreePlist.add(actionAddPlist);
		tbTreePlist.add(actionRemovePlist);
		JToolBar tbCanvas = new JToolBar();
		JComboBox listStageSize = new JComboBox();
		listStageSize.addItem("256x256");
		listStageSize.addItem("512x512");
		listStageSize.addItem("1024x1024");
		listStageSize.addItem("2048x2048");
		listStageSize.addItem("4096x4096");
		listStageSize.setSelectedItem(AppConfig.config().getStageWidth() + "x" + AppConfig.config().getStageHeight());
		listStageSize.setMaximumSize(new Dimension(100, listStageSize.getPreferredSize().height));
		listStageSize.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String[] strs = e.getItem().toString().split("x");
					int width = Integer.parseInt(strs[0]);
					int height = Integer.parseInt(strs[1]);
					AppConfig.config().setStageWidth(width);
					AppConfig.config().setStageHeight(height);
					moduleCanvas.resetStage();
				}
			}
		});
		tbCanvas.add(listStageSize);
		tbCanvas.addSeparator();
		tbCanvas.add(actionCanvasZoomNormal);
		tbCanvas.add(actionCanvasZoomIn);
		tbCanvas.add(actionCanvasZoomOut);
		tbCanvas.addSeparator();
		final JLabel lblPosition = new JLabel();
		lblPosition.setBorder(new EtchedBorder());
		lblPosition.setSize(200, 20);
		tbCanvas.add(lblPosition);
		moduleCanvas.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				updatePositionLabel(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				updatePositionLabel(e);
			}

			private void updatePositionLabel(MouseEvent e) {
				Point p = moduleCanvas.convertPoint(e.getPoint());
				String str = String.format("X=%d Y=%d", p.x, p.x, p.y);
				lblPosition.setText(str);
			}
		});
		Tools.makePanel(panelTableAction, "Actions", tbTableAction,
				tableActions);
		Tools.makePanel(panelTableSequence, "Sequences", tbTableSequence,
				tableSequence);
		Tools.makePanel(panelTableModule, "Modules", tbTableModule, tableModule);
		Tools.makePanel(panelTableFrame, "Frames", tbTableFrame, tableFrames);
		Tools.makePanel(panelPlistTop, "Plists", tbTreePlist, splitPlist);
		Tools.makePanel(panelCanvas, "ModuleCanvas", tbCanvas, moduleCanvas);
		new DropTarget(panelPlistTop, DnDConstants.ACTION_COPY,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent event) {
						DataFlavor[] dataFlavors = event
								.getCurrentDataFlavors();
						if (dataFlavors[0].match(DataFlavor.javaFileListFlavor)) {
							event.acceptDrop(DnDConstants.ACTION_COPY);
							try {
								Transferable tr = event.getTransferable();
								Object obj = tr
										.getTransferData(DataFlavor.javaFileListFlavor);
								@SuppressWarnings("unchecked")
								List<File> files = (List<File>) obj;
								for (int i = 0; i < files.size(); i++) {
									File file = files.get(i);
									if (file.isFile()
											&& file.getName().toLowerCase()
													.endsWith(".plist")) {
										FLPlist plist = new FLPlist();
										plist.load(file);
										addPlist(plist);
									}
								}
							} catch (UnsupportedFlavorException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				});
		new DropTarget(panelTableSequence, DnDConstants.ACTION_COPY,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent event) {
						if (sequenceTableModel.getSequences() == null) {
							Tools.msg(AnimEditor.this,
									"Please create Action first.");
							return;
						}
						Point p = event.getLocation();
						p.y -= (tableSequence.getLocationOnScreen().getY() - panelTableSequence
								.getLocationOnScreen().y);
						int row = tableSequence.rowAtPoint(p);
						Vector<FLSequence> sequences = sequenceTableModel
								.getSequences();
						Transferable t = event.getTransferable();
						DataFlavor[] data = t.getTransferDataFlavors();
						try {
							for (int i = 0; i < data.length; i++) {
								Object obj = t.getTransferData(data[i]);
								if (obj instanceof Integer) {
									int id = (Integer) obj;
									FLFrame frame = animation.getFrames().get(
											id);
									FLSequence seq = new FLSequence(frame);
									addSequence(sequences, seq, row);
								}
							}
							tableSequence.updateUI();
						} catch (UnsupportedFlavorException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		new DropTarget(panelTableAction, DnDConstants.ACTION_COPY,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent event) {
						Transferable t = event.getTransferable();
						DataFlavor[] data = t.getTransferDataFlavors();
						try {
							Vector<FLModule> modules = new Vector<FLModule>();
							for (int i = 0; i < data.length; i++) {
								if (!t.isDataFlavorSupported(data[i])) {
									continue;
								}
								Object obj = t.getTransferData(data[i]);
								if (obj instanceof Vector) {
									@SuppressWarnings("unchecked")
									Vector<String> vector = (Vector<String>) obj;
									for (int j = 0; j < vector.size(); j++) {
										String key = vector.get(j);
										FLClip clip = animation.getClip(key);
										FLModule module = new FLModule(clip);
										module.setSelected(true);
										modules.add(module);
									}
								}
							}
							if (modules.size() > 0) {
								final Vector<FLAction> vector = animation
										.getActions();
								final FLAction action = new FLAction();
								action.setName("action_" + (vector.size() + 1));
								final Vector<FLFrame> frames = new Vector<FLFrame>();
								for (int i = 0; i < modules.size(); i++) {
									FLFrame frame = new FLFrame();
									FLModule module = modules.get(i);
									frame.setName("frame_" + module.getName());
									frame.getModules().add(module);
									frames.add(frame);
								}
								action.addFrames(frames);
								undoManager.addEdit(new RekcahUndoableEdit(
										"NewActionWithClips") {
									@Override
									protected void init() {
										super.init();
										animation.getFrames().addAll(frames);
										vector.add(action);
										updateUI();
									}

									@Override
									public void undo()
											throws CannotUndoException {
										super.undo();
										animation.getFrames().removeAll(frames);
										vector.remove(action);
										updateUI();
									}

									@Override
									public void redo()
											throws CannotRedoException {
										super.redo();
										animation.getFrames().addAll(frames);
										vector.add(action);
										updateUI();
									}

									public void updateUI() {
										tableActions.updateUI();
										tableSequence.updateUI();
										tableFrames.updateUI();
										tableModule.updateUI();
										atTableActionSelectionChanged(true);
									}
								});
							}
						} catch (UnsupportedFlavorException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		new DropTarget(panelTableModule, DnDConstants.ACTION_COPY,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent event) {
						int index = tableFrames.getSelectedRow();
						if (index < 0 || index >= animation.getFrames().size()) {
							event.dropComplete(false);
							Tools.msg(AnimEditor.this,
									"Please create Frame first.");
							return;
						}
						FLFrame frame = animation.getFrames().get(index);
						Transferable t = event.getTransferable();
						DataFlavor[] data = t.getTransferDataFlavors();
						try {
							Point p = event.getLocation();
							p.y -= (tableModule.getLocationOnScreen().getY() - panelTableModule
									.getLocationOnScreen().y);
							int row = tableModule.rowAtPoint(p);
							moduleCanvas.unselectAll();
							Vector<FLModule> modules = new Vector<FLModule>();
							for (int i = 0; i < data.length; i++) {
								if (!t.isDataFlavorSupported(data[i])) {
									continue;
								}
								Object obj = t.getTransferData(data[i]);
								if (obj instanceof Vector) {
									@SuppressWarnings("unchecked")
									Vector<String> vector = (Vector<String>) obj;
									for (int j = 0; j < vector.size(); j++) {
										String key = vector.get(j);
										FLClip clip = animation.getClip(key);
										FLModule module = new FLModule(clip);
										module.setSelected(true);
										modules.add(module);
									}
								}
							}
							if (modules.size() > 0) {
								addModules(frame, modules, row);
								updateTableModuleSelected();
								tableModule.updateUI();
							}
						} catch (UnsupportedFlavorException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		new DropTarget(panelTableFrame, DnDConstants.ACTION_COPY,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent event) {
						Transferable t = event.getTransferable();
						DataFlavor[] data = t.getTransferDataFlavors();
						try {
							Vector<FLModule> modules = new Vector<FLModule>();
							for (int i = 0; i < data.length; i++) {
								if (!t.isDataFlavorSupported(data[i])) {
									continue;
								}
								Object obj = t.getTransferData(data[i]);
								if (obj instanceof Vector) {
									@SuppressWarnings("unchecked")
									Vector<String> vector = (Vector<String>) obj;
									for (int j = 0; j < vector.size(); j++) {
										String key = vector.get(j);
										FLClip clip = animation.getClip(key);
										FLModule module = new FLModule(clip);
										module.setSelected(true);
										modules.add(module);
									}
								}
							}
							if (modules.size() > 0) {
								final Vector<FLFrame> frames = new Vector<FLFrame>();
								for (int i = 0; i < modules.size(); i++) {
									FLFrame frame = new FLFrame();
									FLModule module = modules.get(i);
									frame.setName("frame_" + module.getName());
									frame.getModules().add(module);
									frames.add(frame);
								}
								Point p = event.getLocation();
								p.y -= (tableFrames.getLocationOnScreen()
										.getY() - panelTableFrame
										.getLocationOnScreen().y);
								final int row = Tools.limit(
										tableFrames.rowAtPoint(p),
										tableFrames.getRowCount());
								undoManager.addEdit(new RekcahUndoableEdit(
										"AddFrames") {

									@Override
									protected void init() {
										super.init();
										animation.getFrames().addAll(row,
												frames);
										tableFrames.updateUI();
									}

									@Override
									public void undo()
											throws CannotUndoException {
										super.undo();
										animation.getFrames().removeAll(frames);
										tableFrames.updateUI();
									}

									@Override
									public void redo()
											throws CannotRedoException {
										super.redo();
										animation.getFrames().addAll(row,
												frames);
										tableFrames.updateUI();
									}
								});
							}
						} catch (UnsupportedFlavorException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		tableActions.getColumnModel().getColumn(0).setPreferredWidth(20);
		tableFrames.getColumnModel().getColumn(0).setPreferredWidth(20);
		tableSequence.getColumnModel().getColumn(0).setPreferredWidth(20);
		tableModule.getColumnModel().getColumn(0).setPreferredWidth(20);
		split3.setResizeWeight(0.25);
		split12.setResizeWeight(0.5);
		splitLeft.setResizeWeight(0.4);
		splitMiddle.setResizeWeight(0.3);
		splitRight.setResizeWeight(0.3);
		splitPlist.setDividerLocation(128);
		resetEditorComonent();
		//Action Player
		Timer selectTimer = new Timer(true);
		selectTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (playing) {
					playNextFrame();
				}
			}
		}, 0, 60);
	}

	public boolean save() {
		String name = animation.getName() + ".fan";
		File file = null;
		if (animation.getFilePath() == null) {
			file = Tools.openFile(animation.getCurrentPath(), name);
			file = Tools.showSaveFile(AnimEditor.this, null, file,
					"Animation Files(*.fan)", "fan");
		} else {
			file = Tools.openFile(null, animation.getFilePath());
		}
		if (file == null) {
			return false;
		}
		Tools.writeToXml(file, animation.toElement());
		AppConfig.config().addRecentFile(file.getPath());
		setModified(false);
		if (undoManagerListener != null) {
			undoManagerListener.stateChanged();
		}
		return true;
	}

	public boolean saveAs() {
		animation.setFilePath(null);
		return save();
	}

	private boolean playing;
	private int durationId;

	public boolean isPlaying() {
		return playing;
	}

	public void startPlay() {
		durationId = 0;
		playing = true;
	}

	public void stopPlay() {
		playing = false;
	}

	public void playNextFrame() {
		int count = tableSequence.getRowCount();
		if (count <= 0) {
			return;
		}
		int row = tableSequence.getSelectedRow();
		if (row < 0) {
			row = 0;
			tableSequence.setRowSelectionInterval(row, row);
		} else if (row >= count) {
			row = count - 1;
			tableSequence.setRowSelectionInterval(row, row);
		}
		FLSequence seq = sequenceTableModel.getSequences().get(row);
		if (durationId >= seq.getDuration()) {
			durationId = 1;
			if (++row >= count) {
				row = 0;
			}
			tableSequence.setRowSelectionInterval(row, row);
		} else {
			durationId++;
		}
	}

	public void resetEditorComonent() {
		final DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) treePlistModel
				.getRoot();
		final DefaultListModel listModel = (DefaultListModel) listPlist
				.getModel();
		treeRoot.removeAllChildren();
		listModel.removeAllElements();
		for (FLPlist plist : animation.getPlists()) {
			treeRoot.add(plist);
			listModel.addElement(plist);
			treePlist.expandPath(new TreePath(plist.getPath()));
		}
		if (tableActions.getRowCount() > 0
				&& tableActions.getSelectedRow() == -1) {
			tableActions.setRowSelectionInterval(0, 0);
		}
		if (tableFrames.getRowCount() > 0 && tableFrames.getSelectedRow() == -1) {
			tableFrames.setRowSelectionInterval(0, 0);
		}
	}

	protected void atTableActionSelectionChanged(boolean manual) {
		Vector<FLAction> actions = actionTableModel.getActions();
		if (actions == null) {
			return;
		}
		int row = tableActions.getSelectedRow();
		if (row >= 0 && row < tableActions.getRowCount()) {
			FLAction action = actions.get(row);
			if (action.getSequences() != sequenceTableModel.getSequences()) {
				sequenceTableModel.setSequences(action.getSequences());
			}
		} else {
			sequenceTableModel.setSequences(null);
		}
		if (manual) {
			tableActions.updateUI();
		}
		tableSequence.updateUI();
	}

	protected void atTableSequencesSelectionChanged(boolean manual) {
		int count = tableSequence.getRowCount();
		if (count > 0) {
			int row = tableSequence.getSelectedRow();
			if (row < 0) {
				row = 0;
				tableSequence.setRowSelectionInterval(row, row);
			} else if (row >= count) {
				row = count - 1;
				tableSequence.setRowSelectionInterval(row, row);
			}
			FLSequence seq = sequenceTableModel.getSequences().get(row);
			int frameId = animation.getFrames().indexOf(seq.getFrame());
			if (frameId >= 0) {
				tableFrames.setRowSelectionInterval(frameId, frameId);
			}
		}
		if (manual) {
			tableSequence.updateUI();
		}
	}

	private boolean lockModuleTable = false;

	protected void atTableModulesSelectionChanged(boolean manual) {
		Vector<FLModule> modules = moduleTableModel.getModules();
		if (lockModuleTable || modules == null) {
			return;
		}
		for (int i = 0; i < modules.size(); i++) {
			boolean selected = tableModule.getSelectionModel().isSelectedIndex(
					i);
			modules.get(i).setSelected(selected);
		}
		moduleCanvas.updateUI();
	}

	protected void atTableFramesSelectionChanged(boolean manual) {
		int count = animation.getFrames().size();
		FLFrame frame = null;
		if (count > 0) {
			int row = tableFrames.getSelectedRow();
			if (row < 0) {
				row = 0;
				tableFrames.setRowSelectionInterval(row, row);
			} else if (row >= count) {
				row = count - 1;
				tableFrames.setRowSelectionInterval(row, row);
			}
			frame = animation.getFrames().get(row);
			Vector<FLModule> modules = frame.getModules();
			for (FLModule module : modules) {
				module.setSelected(false);
			}
			moduleTableModel.setModules(frame.getModules());
			moduleTableModel.fireTableDataChanged();
		} else {
			moduleTableModel.setModules(null);
		}
		if (manual) {
			tableFrames.updateUI();
		}
		moduleCanvas.setFrame(frame);
		moduleCanvas.repaint();
	}

	protected void updateTableModuleSelected() {
		int frameId = tableFrames.getSelectedRow();
		if (frameId < 0 || frameId >= tableFrames.getRowCount()) {
			return;
		} else {
			lockModuleTable = true;
			FLFrame frame = animation.getFrames().get(frameId);
			if (frame != null) {
				tableModule.clearSelection();
				for (int i = 0; i < tableModule.getRowCount(); i++) {
					if (frame.getModules().get(i).isSelected()) {
						tableModule.addRowSelectionInterval(i, i);
					}
				}
			}
			lockModuleTable = false;
			moduleCanvas.repaint();
			tableModule.updateUI();
		}
	}

	public void addModules(final FLFrame frame, final Vector<FLModule> module,
			int index) {
		final Vector<FLModule> vector = frame.getModules();
		final int row = index < 0 ? vector.size() : index;
		undoManager.addEdit(new RekcahUndoableEdit("AddModule") {

			@Override
			protected void init() {
				super.init();
				vector.addAll(row, module);
				updateTableModuleSelected();
			}

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				vector.removeAll(module);
				updateTableModuleSelected();
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				vector.addAll(row, module);
				updateTableModuleSelected();
			}
		});
	}

	public void addSequence(final Vector<FLSequence> sequences,
			final FLSequence sequence, int index) {
		final int row = index < 0 ? sequences.size() : index;
		undoManager.addEdit(new RekcahUndoableEdit("AddSequence") {

			@Override
			protected void init() {
				super.init();
				sequences.add(row, sequence);
				tableSequence.setRowSelectionInterval(row, row);
				atTableSequencesSelectionChanged(true);
			}

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				sequences.remove(sequence);
				atTableSequencesSelectionChanged(true);
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				sequences.add(row, sequence);
				tableSequence.setRowSelectionInterval(row, row);
				atTableSequencesSelectionChanged(true);
			}
		});
	}

	public void addPlist(final FLPlist plist) {
		if (plist == null) {
			return;
		}
		if (animation.getPlistFullPath(plist.getPlistFile()) != null) {
			Tools.msg(AnimEditor.this, "Plist has been existed~");
			return;
		}
		final DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) treePlistModel
				.getRoot();
		final DefaultListModel listModel = (DefaultListModel) listPlist
				.getModel();
		undoManager.addEdit(new RekcahUndoableEdit("AddPlist") {
			@Override
			protected void init() {
				super.init();
				animation.getPlists().add(plist);
				listModel.addElement(plist);
				treeRoot.add(plist);
				treePlist.expandPath(new TreePath(plist.getPath()));
				treePlist.updateUI();
				listPlist.updateUI();
			}

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				animation.getPlists().remove(plist);
				treeRoot.remove(plist);
				listModel.removeElement(plist);
				treePlist.updateUI();
				listPlist.updateUI();
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				animation.getPlists().add(plist);
				listModel.addElement(plist);
				treeRoot.add(plist);
				treePlist.expandPath(new TreePath(plist.getPath()));
				treePlist.updateUI();
				listPlist.updateUI();
			}
		});
	}

	public void removePlist(final FLPlist plist) {
		if (plist == null) {
			return;
		}
		if (animation.getPlists().contains(plist)) {
			Tools.msg(AnimEditor.this, "Can't remove useful Plist.");
			return;
		}
		final DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) treePlistModel
				.getRoot();
		final DefaultListModel listModel = (DefaultListModel) listPlist
				.getModel();
		undoManager.addEdit(new RekcahUndoableEdit("RemovePlist") {
			@Override
			protected void init() {
				super.init();
				animation.getPlists().remove(plist);
				listModel.removeElement(plist);
				treeRoot.remove(plist);
				treePlist.updateUI();
				listPlist.updateUI();
			}

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				animation.getPlists().add(plist);
				listModel.addElement(plist);
				treeRoot.add(plist);
				treePlist.expandPath(new TreePath(plist.getPath()));
				treePlist.updateUI();
				listPlist.updateUI();
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				animation.getPlists().remove(plist);
				listModel.removeElement(plist);
				treeRoot.remove(plist);
				treePlist.updateUI();
				listPlist.updateUI();
			}
		});
	}

	//Action
	RekcahAction actionNewAction = new RekcahAction("New", "/res/add.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableActions;
			final Vector<FLAction> vector = animation.getActions();
			final FLAction object = new FLAction();
			final int row = Tools.limit(table.getSelectedRow() + 1,
					vector.size());
			object.setName("action_" + (table.getModel().getRowCount() + 1));
			undoManager.addEdit(new RekcahUndoableEdit("NewAction") {
				@Override
				protected void init() {
					super.init();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
					atTableActionSelectionChanged(true);
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.remove(object);
					if (vector.size() > 0 && row - 1 >= 0) {
						table.setRowSelectionInterval(row - 1, row - 1);
						table.scrollRectToVisible(table.getCellRect(row - 1, 0,
								true));
					}
					atTableActionSelectionChanged(true);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
					atTableActionSelectionChanged(true);
				}
			});
		}
	};
	RekcahAction actionDeleteAction = new RekcahAction("Delete",
			"/res/cancel.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableActions;
			final Vector<FLAction> vector = animation.getActions();
			final int count = vector.size();
			if (count <= 0) {
				return;
			}
			final int row = table.getSelectedRow();
			final FLAction object = vector.get(row);
			undoManager.addEdit(new RekcahUndoableEdit("DeleteAction") {
				@Override
				protected void init() {
					super.init();
					vector.remove(object);
					if (row > 0) {
						int selectRow = Tools.limit(row - 1, vector.size() - 1);
						table.setRowSelectionInterval(selectRow, selectRow);
						table.scrollRectToVisible(table.getCellRect(selectRow,
								0, true));
					}
					atTableActionSelectionChanged(true);
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
					atTableActionSelectionChanged(true);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.remove(row);
					if (vector.size() > 0) {
						int selectRow = Tools.limit(row - 1, vector.size() - 1);
						table.setRowSelectionInterval(selectRow, selectRow);
						table.scrollRectToVisible(table.getCellRect(selectRow,
								0, true));
					}
					atTableActionSelectionChanged(true);
				}
			});
		}
	};
	RekcahAction actionMoveUpAction = new RekcahAction("MoveUp", "/res/up.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableActions;
			final Vector<FLAction> vector = animation.getActions();
			final int row = table.getSelectedRow();
			if (row < 1) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveUpAction") {
				@Override
				protected void init() {
					super.init();
					FLAction object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLAction object = vector.remove(row - 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLAction object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionMoveDownAction = new RekcahAction("MoveDown",
			"/res/down.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableActions;
			final Vector<FLAction> vector = animation.getActions();
			int count = vector.size();
			final int row = table.getSelectedRow();
			if (row >= count - 1) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveDownAction") {

				@Override
				protected void init() {
					super.init();
					FLAction object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLAction object = vector.remove(row + 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLAction object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionCloneAction = new RekcahAction("CloneAction",
			"/res/clone.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableActions;
			final Vector<FLAction> vector = animation.getActions();
			final int count = vector.size();
			final int row = table.getSelectedRow();
			if (count <= 0 || row < 0) {
				return;
			}
			final FLAction obj = new FLAction(vector.get(row));
			obj.setName(obj.getName() + "_copy");
			final int index = row + 1;
			undoManager.addEdit(new RekcahUndoableEdit("CloneAction") {

				@Override
				protected void init() {
					super.init();
					vector.add(index, obj);
					table.updateUI();
					table.setRowSelectionInterval(index, index);
					table.scrollRectToVisible(table.getCellRect(index, 0, true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.remove(index);
					table.updateUI();
					table.setRowSelectionInterval(index - 1, index - 1);
					table.scrollRectToVisible(table.getCellRect(index - 1, 0,
							true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.add(index, obj);
					table.updateUI();
					table.setRowSelectionInterval(index, index);
					table.scrollRectToVisible(table.getCellRect(index, 0, true));
				}
			});
		}
	};
	//Sequence
	RekcahAction actionMoveUpSequence = new RekcahAction("MoveUp",
			"/res/up.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableSequence;
			final int row = table.getSelectedRow();
			if (row < 1) {
				return;
			}
			final Vector<FLSequence> vector = sequenceTableModel.getSequences();
			if (vector == null) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveUpSequence") {
				@Override
				protected void init() {
					super.init();
					FLSequence object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLSequence object = vector.remove(row - 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLSequence object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionMoveDownSequence = new RekcahAction("MoveDown",
			"/res/down.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableSequence;
			final Vector<FLSequence> vector = sequenceTableModel.getSequences();
			if (vector == null) {
				return;
			}
			int count = vector.size();
			final int row = table.getSelectedRow();
			if (row < 0 || row >= count - 1) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveUpSequence") {
				@Override
				protected void init() {
					super.init();
					FLSequence object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLSequence object = vector.remove(row + 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLSequence object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionDeleteSequence = new RekcahAction("Delete",
			"/res/cancel.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableSequence;
			final Vector<FLSequence> vector = sequenceTableModel.getSequences();
			final int row = table.getSelectedRow();
			if (null == vector || vector.size() <= 0 || row < 0) {
				return;
			}
			final FLSequence object = vector.get(row);
			undoManager.addEdit(new RekcahUndoableEdit("DeleteSequence") {
				@Override
				protected void init() {
					super.init();
					vector.remove(object);
					if (row > 0) {
						table.setRowSelectionInterval(row - 1, row - 1);
						table.scrollRectToVisible(table.getCellRect(row - 1, 0,
								true));
					}
					table.updateUI();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
					table.updateUI();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.remove(object);
					if (row > 0) {
						table.setRowSelectionInterval(row - 1, row - 1);
						table.scrollRectToVisible(table.getCellRect(row - 1, 0,
								true));
					}
					table.updateUI();
				}
			});
		}
	};
	//Module
	RekcahAction actionMoveUpModule = new RekcahAction("MoveUp", "/res/up.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableModule;
			if (table.getSelectedRowCount() <= 0) {
				return;
			}
			final int[] rows = table.getSelectedRows();
			if (rows[0] < 1) {
				return;
			}
			final Vector<FLModule> vector = moduleTableModel.getModules();
			undoManager.addEdit(new RekcahUndoableEdit("MoveUpModule") {
				@Override
				protected void init() {
					super.init();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i]);
						vector.add(rows[i] - 1, tmp);
					}
					updateTableModuleSelected();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i] - 1);
						vector.add(rows[i], tmp);
					}
					updateTableModuleSelected();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i]);
						vector.add(rows[i] - 1, tmp);
					}
					updateTableModuleSelected();
				}
			});
		}
	};
	RekcahAction actionMoveDownModule = new RekcahAction("MoveDown",
			"/res/down.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableModule;
			if (table.getSelectedRowCount() <= 0) {
				return;
			}
			final int[] rows = table.getSelectedRows();
			if (rows[rows.length - 1] >= table.getRowCount() - 1) {
				return;
			}
			final Vector<FLModule> vector = moduleTableModel.getModules();
			undoManager.addEdit(new RekcahUndoableEdit("MoveDownModule") {
				@Override
				protected void init() {
					super.init();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i]);
						vector.add(rows[i] + 1, tmp);
					}
					updateTableModuleSelected();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i] + 1);
						vector.add(rows[i], tmp);
					}
					updateTableModuleSelected();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < rows.length; i++) {
						FLModule tmp = vector.remove(rows[i]);
						vector.add(rows[i] + 1, tmp);
					}
					updateTableModuleSelected();
				}
			});
		}
	};
	RekcahAction actionDeleteModule = new RekcahAction("Delete",
			"/res/cancel.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableModule;
			if (table.getSelectedRowCount() <= 0) {
				return;
			}
			final int[] rows = table.getSelectedRows();
			final Vector<FLModule> vector = moduleTableModel.getModules();
			undoManager.addEdit(new RekcahUndoableEdit("MoveDownModule") {
				Vector<FLModule> removed;

				@Override
				protected void init() {
					super.init();
					removed = new Vector<FLModule>();
					for (int i = rows.length - 1; i >= 0; i--) {
						FLModule m = vector.remove(rows[i]);
						removed.add(0, m);
					}
					updateTableModuleSelected();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < rows.length; i++) {
						FLModule m = removed.get(i);
						vector.add(rows[i], m);
					}
					updateTableModuleSelected();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = rows.length - 1; i >= 0; i--) {
						vector.remove(rows[i]);
					}
					updateTableModuleSelected();
				}
			});
		}
	};
	//Frame
	RekcahAction actionNewFrame = new RekcahAction("New", "/res/add.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableFrames;
			final Vector<FLFrame> vector = animation.getFrames();
			final FLFrame object = new FLFrame();
			object.setName("frame_" + (table.getModel().getRowCount() + 1));
			final int row = Tools.limit(table.getSelectedRow() + 1,
					vector.size());

			undoManager.addEdit(new RekcahUndoableEdit("NewFrame") {

				@Override
				protected void init() {
					super.init();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					atTableFramesSelectionChanged(true);
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.remove(object);
					atTableFramesSelectionChanged(true);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					atTableFramesSelectionChanged(true);
				}
			});
		}
	};
	RekcahAction actionDeleteFrame = new RekcahAction("Delete",
			"/res/cancel.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableFrames;
			final Vector<FLFrame> vector = animation.getFrames();
			final int count = vector.size();
			if (count <= 0) {
				return;
			}
			final int row = tableFrames.getSelectedRow();
			final FLFrame object = frameTableModel.getFrames().get(row);
			for (FLAction action : animation.getActions()) {
				for (FLSequence sequence : action.getSequences()) {
					if (sequence.getFrame() == object) {
						Tools.msg(AnimEditor.this, "Can't delete useful Frame.");
						return;
					}
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("DeleteAction") {
				@Override
				protected void init() {
					super.init();
					vector.remove(object);
					atTableFramesSelectionChanged(true);
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.add(row, object);
					table.setRowSelectionInterval(row, row);
					atTableFramesSelectionChanged(true);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.remove(object);
					atTableFramesSelectionChanged(true);
				}
			});
		}
	};
	RekcahAction actionMoveUpFrame = new RekcahAction("MoveUp", "/res/up.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableFrames;
			final Vector<FLFrame> vector = animation.getFrames();
			final int row = table.getSelectedRow();
			if (row < 1) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveUpFrame") {
				@Override
				protected void init() {
					super.init();
					FLFrame object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLFrame object = vector.remove(row - 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLFrame object = vector.remove(row);
					vector.add(row - 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row - 1, row - 1);
					table.scrollRectToVisible(table.getCellRect(row - 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionMoveDownFrame = new RekcahAction("MoveDown",
			"/res/down.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableFrames;
			final Vector<FLFrame> vector = animation.getFrames();
			int count = vector.size();
			final int row = table.getSelectedRow();
			if (row >= count - 1) {
				return;
			}
			undoManager.addEdit(new RekcahUndoableEdit("MoveDownFrame") {

				@Override
				protected void init() {
					super.init();
					FLFrame object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					FLFrame object = vector.remove(row + 1);
					vector.add(row, object);
					table.updateUI();
					table.setRowSelectionInterval(row, row);
					table.scrollRectToVisible(table.getCellRect(row, 0, true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					FLFrame object = vector.remove(row);
					vector.add(row + 1, object);
					table.updateUI();
					table.setRowSelectionInterval(row + 1, row + 1);
					table.scrollRectToVisible(table.getCellRect(row + 1, 0,
							true));
				}
			});
		}
	};
	RekcahAction actionCloneFrame = new RekcahAction("Clone", "/res/clone.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JTable table = tableFrames;
			final Vector<FLFrame> vector = animation.getFrames();
			final int count = vector.size();
			final int row = table.getSelectedRow();
			if (count <= 0 || row < 0) {
				return;
			}
			final FLFrame obj = new FLFrame(vector.get(row));
			final int index = row + 1;
			obj.setName(obj.getName() + "_copy");
			undoManager.addEdit(new RekcahUndoableEdit("CloneFrame") {

				@Override
				protected void init() {
					super.init();
					vector.add(index, obj);
					table.updateUI();
					table.setRowSelectionInterval(index, index);
					table.scrollRectToVisible(table.getCellRect(index, 0, true));
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					vector.remove(index);
					table.updateUI();
					table.setRowSelectionInterval(index - 1, index - 1);
					table.scrollRectToVisible(table.getCellRect(index - 1, 0,
							true));
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					vector.add(index, obj);
					table.updateUI();
					table.setRowSelectionInterval(index, index);
					table.scrollRectToVisible(table.getCellRect(index, 0, true));
				}
			});
		}
	};
	//Plist
	RekcahAction actionAddPlist = new RekcahAction("AddPlist", "/res/add.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			File file = Tools.showOpenFile(AnimEditor.this, null, false,
					"Select Plist file", "plist");
			if (file == null) {
				return;
			}
			FLPlist sp = new FLPlist();
			sp.load(file);
			addPlist(sp);
		}
	};
	RekcahAction actionRemovePlist = new RekcahAction("RemovePlist",
			"/res/delete.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			FLPlist plist = (FLPlist) listPlist.getSelectedValue();
			removePlist(plist);
		}
	};
	//Canvas
	RekcahAction actionCanvasZoomNormal = new RekcahAction("ZoomNormal",
			"/res/zoom.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			moduleCanvas.setZoomFactor(1);
			treePlist.updateUI();
		}
	};
	RekcahAction actionCanvasZoomIn = new RekcahAction("ZoomIn",
			"/res/zoom_in.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			float zoom = moduleCanvas.getZoomFactor();
			if (zoom <= 2) {
				zoom *= 2;
				moduleCanvas.setZoomFactor(zoom);
				treePlist.updateUI();
			}
		}
	};
	RekcahAction actionCanvasZoomOut = new RekcahAction("ZoomOut",
			"/res/zoom_out.png") {
		@Override
		public void actionPerformed(ActionEvent e) {
			float zoom = moduleCanvas.getZoomFactor();
			if (zoom >= 0.5) {
				zoom /= 2;
				moduleCanvas.setZoomFactor(zoom);
				treePlist.updateUI();
			}
		}
	};
	public FLAnimation getAnimation() {
		return animation;
	}

	public JTree getTreePlist() {
		return treePlist;
	}

	public JTable getTableActions() {
		return tableActions;
	}

	public JTable getTableFrames() {
		return tableFrames;
	}

	public JTable getTableSequence() {
		return tableSequence;
	}

	public JTable getTableModule() {
		return tableModule;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public boolean close() {
		JTabbedPane tab = ((JTabbedPane) getParent());
		tab.remove(this);
		return true;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
}
