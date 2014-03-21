package frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import model.FLClip;
import model.FLFrame;
import model.FLModule;
import tools.RekcahAction;
import tools.RekcahUndoableEdit;

interface ModuleSelectedListener {
	void atChanged();
}

public class ModuleCanvas extends JPanel {
	private FLFrame frame;
	private Point selectStartPoint;
	private Rectangle selectArea;
	private int selectAlpha;
	private float zoomFactor;
	boolean moveMode = false;
	boolean mousePressed = false;
	boolean inited = false;
	private ModuleSelectedListener moduleSelectedListener;
	private UndoManager undoManager;

	public ModuleCanvas(final AnimEditor editor) {
		undoManager = editor.getUndoManager();
		zoomFactor = 1;
		setBackground(new Color(192, 192, 192));
		resetStage();
		Timer selectTimer = new Timer(true);
		selectTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!mousePressed && selectAlpha > 0) {
					selectAlpha--;
					ModuleCanvas.this.repaint();
				}
				if(!inited && getVisibleRect().getWidth() > 0) {
					inited = true;
					resetStage();
				}
			}
		}, 0, 10);
		new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {

			Vector<FLModule> added = new Vector<FLModule>();

			@Override
			public void dragEnter(DropTargetDragEvent event) {
				if (editor.isPlaying() || frame == null) {
					event.rejectDrag();
					return;
				}
				ModuleCanvas.this.unselectAll();
				Point p = convertPoint(event.getLocation());
				selectStartPoint = p;
				Vector<FLModule> modules = frame.getModules();
				Transferable t = event.getTransferable();
				DataFlavor[] data = t.getTransferDataFlavors();
				try {
					for (int i = 0; i < data.length; i++) {
						Object obj = t.getTransferData(data[i]);
						if (obj instanceof Vector) {
							@SuppressWarnings("unchecked")
							Vector<String> vector = (Vector<String>) obj;
							for (int j = 0; j < vector.size(); j++) {
								String key = vector.get(j);
								FLClip clip = editor.getAnimation()
										.getClip(key);
								FLModule module = new FLModule(clip);
								module.setSelected(true);
								module.setPosX(p.x - module.getWidth() / 2);
								module.setPosY(p.y - module.getHeight() / 2);
								module.savePos();
								modules.add(module);
								added.add(module);
							}
						}
					}
					moveMode = true;
					if (moduleSelectedListener != null) {
						ModuleCanvas.this.moduleSelectedListener.atChanged();
					}
					repaint();
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				if (frame == null) {
					return;
				}
				for (int i = 0; i < added.size(); i++) {
					frame.getModules().remove(added.get(i));
				}
				added.clear();
				repaint();
				if (moduleSelectedListener != null) {
					ModuleCanvas.this.moduleSelectedListener.atChanged();
				}
			}

			@Override
			public void dragOver(DropTargetDragEvent event) {
				if (selectStartPoint == null || frame == null) {
					return;
				}
				Point p1 = selectStartPoint;
				Point p2 = convertPoint(event.getLocation());
				int offX = p2.x - p1.x;
				int offY = p2.y - p1.y;
				Vector<FLModule> modules = frame.getModules();
				for (FLModule module : modules) {
					if (module.isSelected()) {
						module.setPosX(module.getPrevX() + offX);
						module.setPosY(module.getPrevY() + offY);
					}
				}
				ModuleCanvas.this.repaint();
			}

			@Override
			public void drop(DropTargetDropEvent event) {
				if (frame == null) {
					return;
				}
				Vector<FLModule> modules = new Vector<FLModule>();
				for (int i = 0; i < added.size(); i++) {
					frame.getModules().remove(added.get(i));
					modules.add(added.get(i));
				}
				editor.addModules(frame, modules, -1);
				editor.updateTableModuleSelected();
				added.clear();
				event.dropComplete(true);
			}
		});

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (editor.isPlaying() || e.getButton() != MouseEvent.BUTTON1) {
					return;
				}
				mousePressed = true;
				selectStartPoint = convertPoint(e.getPoint());
				FLModule selectedModule = getModuleAt(selectStartPoint);
				if (selectedModule == null) {
					if ((e.getModifiers() & InputEvent.CTRL_MASK) == 0) {
						unselectAll();
					}
					selectArea = new Rectangle(selectStartPoint.x,
							selectStartPoint.y, 0, 0);
					moveMode = false;
				} else {
					if (!selectedModule.isSelected()) {
						if ((e.getModifiers() & InputEvent.CTRL_MASK) == 0) {
							unselectAll();
						}
						selectedModule.setSelected(true);
					}
					Vector<FLModule> modules = frame.getModules();
					for (FLModule module : modules) {
						if (module.isSelected()) {
							module.savePos();
						}
					}
					moveMode = true;
				}
				if (moduleSelectedListener != null) {
					moduleSelectedListener.atChanged();
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (editor.isPlaying()) {
					editor.stopPlay();
					return;
				}
				mousePressed = false;
				if (e.isPopupTrigger()) { // show menu
					if (frame != null) {
						JPopupMenu menu = new JPopupMenu();
						menu.add(actionFlipX);
						menu.add(actionFlipY);
						menu.add(actionAxisFlipX);
						menu.add(actionAxisFlipY);
						menu.add(actionRotateClockwise);
						menu.add(actionRotateAntiClockwise);
						menu.show(ModuleCanvas.this, e.getX(), e.getY());
					}
				} else if (frame != null) {
					Point p2 = convertPoint(e.getPoint());
					FLModule selectedModule = getModuleAt(p2);
					if (selectedModule == null) {
						Vector<FLModule> modules = frame.getModules();
						for (FLModule module : modules) {
							module.setSelected(false);
							if (selectArea == null || selectArea.isEmpty()) {
								if (module.getBounds().contains(p2)) {
									module.setSelected(true);
								}
							} else {
								Rectangle rect = module.getBounds();
								if (selectArea.contains(rect)) {
									module.setSelected(true);
								}
							}
						}
					} else { //move finished
						if (moveMode) {
							final Vector<FLModule> moved = new Vector<FLModule>();
							final Vector<Point> oldPoints = new Vector<Point>();
							final Vector<Point> newPoints = new Vector<Point>();
							for (FLModule module : frame.getModules()) {
								if (module.isSelected()
										&& module.getPosX() != module
												.getPrevX()
										|| module.getPosY() != module
												.getPrevY()) {
									moved.add(module);
									oldPoints.add(new Point(module.getPrevX(),
											module.getPrevY()));
									newPoints.add(new Point(module.getPosX(),
											module.getPosY()));
								}
							}
							if (moved.size() > 0) {
								undoManager.addEdit(new RekcahUndoableEdit(
										"MoveModule") {

									@Override
									protected void init() {
										super.init();
									}

									@Override
									public void undo()
											throws CannotUndoException {
										super.undo();
										for (int i = 0; i < moved.size(); i++) {
											moved.get(i).setPosition(
													oldPoints.get(i));
										}
										repaint();
									}

									@Override
									public void redo()
											throws CannotRedoException {
										super.redo();
										for (int i = 0; i < moved.size(); i++) {
											moved.get(i).setPosition(
													newPoints.get(i));
										}
										repaint();
									}
								});
							}
						}
					}
					repaint();
					if (moduleSelectedListener != null) {
						moduleSelectedListener.atChanged();
					}
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (editor.isPlaying()) {
					return;
				}
				if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0
						|| selectStartPoint == null) {
					return;
				}
				Point p1 = selectStartPoint;
				Point p2 = convertPoint(e.getPoint());
				int offX = p2.x - p1.x;
				int offY = p2.y - p1.y;
				if (moveMode) {
					Vector<FLModule> modules = frame.getModules();
					for (FLModule module : modules) {
						if (module.isSelected()) {
							module.setPosX(module.getPrevX() + offX);
							module.setPosY(module.getPrevY() + offY);
						}
					}
				} else if (selectArea != null) {
					int x1 = Math.min(p1.x, p2.x);
					int x2 = Math.max(p1.x, p2.x);
					int y1 = Math.min(p1.y, p2.y);
					int y2 = Math.max(p1.y, p2.y);
					selectArea.setRect(x1, y1, x2 - x1, y2 - y1);
					selectAlpha = 0x33;
				}
				ModuleCanvas.this.repaint();
			}
		});
	}

	public Point convertPoint(Point p) {
		int offsetX = (getSize().width - getPreferredSize().width) / 2;
		int offsetY = (getSize().height - getPreferredSize().height) / 2;
		int centerX = getPreferredSize().width / 2 + offsetX;
		int centerY = getPreferredSize().height / 2 + offsetY;
		p = new Point((int) ((p.getX() - centerX) / zoomFactor),
				(int) ((p.getY() - centerY) / zoomFactor));
		return p;
	}

	RekcahAction actionFlipX = new RekcahAction("FlipX") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					changed.add(module);
					module.flipX();
					newValue.add(module.getTrans());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("FlipX") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};

	RekcahAction actionFlipY = new RekcahAction("FlipY") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					changed.add(module);
					module.flipY();
					newValue.add(module.getTrans());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("FlipY") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};
	RekcahAction actionAxisFlipX = new RekcahAction("AxisFlipX") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			final Vector<Integer> oldPosX = new Vector<Integer>();
			final Vector<Integer> newPosX = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					oldPosX.add(module.getPosX());
					changed.add(module);
					module.axisFlipX();
					newValue.add(module.getTrans());
					newPosX.add(module.getPosX());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("AxisFlipX") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
						changed.get(i).setPosX(oldPosX.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
						changed.get(i).setPosX(newPosX.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};
	RekcahAction actionAxisFlipY = new RekcahAction("AxisFlipY") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			final Vector<Integer> oldPosY = new Vector<Integer>();
			final Vector<Integer> newPosY = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					oldPosY.add(module.getPosY());
					changed.add(module);
					module.axisFlipY();
					newValue.add(module.getTrans());
					newPosY.add(module.getPosY());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("AxisFlipY") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
						changed.get(i).setPosY(oldPosY.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
						changed.get(i).setPosY(newPosY.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};

	RekcahAction actionRotateClockwise = new RekcahAction("RotateClockwise") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					changed.add(module);
					module.rotateClockwise();
					newValue.add(module.getTrans());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("RotateClockwise") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};
	RekcahAction actionRotateAntiClockwise = new RekcahAction(
			"RotateAntiClockwise") {
		@Override
		public void actionPerformed(ActionEvent e) {
			Vector<FLModule> modules = frame.getModules();
			final Vector<FLModule> changed = new Vector<FLModule>();
			final Vector<Integer> oldValue = new Vector<Integer>();
			final Vector<Integer> newValue = new Vector<Integer>();
			for (FLModule module : modules) {
				if (module.isSelected()) {
					oldValue.add(module.getTrans());
					changed.add(module);
					module.rotateAntiClockwise();
					newValue.add(module.getTrans());
				}
			}
			undoManager.addEdit(new RekcahUndoableEdit("RotateAntiClockwise") {

				@Override
				protected void init() {
					super.init();
				}

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(oldValue.get(i));
					}
					repaint();
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					for (int i = 0; i < changed.size(); i++) {
						changed.get(i).setTrans(newValue.get(i));
					}
					repaint();
				}
			});
			repaint();
			if (moduleSelectedListener != null) {
				moduleSelectedListener.atChanged();
			}
		}
	};

	@Override
	protected void paintComponent(Graphics gg) {
		super.paintComponent(gg);
		Graphics2D g = (Graphics2D) gg;
		//Background
		final int width = getPreferredSize().width;
		final int height = getPreferredSize().height;
		final int offsetX = (getWidth() - width) / 2;
		final int offsetY = (getHeight() - height) / 2;
		final int centerX = width / 2;
		final int centerY = height / 2;
		g.translate(offsetX + centerX, offsetY + centerY);
		g.setColor(AppConfig.config().getCanvasCellColor());
		final int CELL = 10;
		for (int i = 0; i < centerX; i += CELL * zoomFactor) {
			g.drawLine(i, -centerY, i, centerY);
			g.drawLine(-i, -centerY, -i, centerY);
		}
		for (int i = 0; i < centerY; i += CELL * zoomFactor) {
			g.drawLine(-centerX, i, centerX, i);
			g.drawLine(-centerX, -i, centerX, -i);
		}
		g.setColor(AppConfig.config().getCanvasBorderColor());
		g.drawRect(-centerX, -centerY, width - 1, height - 1);
		g.setColor(AppConfig.config().getCanvasAxisColor());
		g.drawLine(0, -height, 0, height);
		g.drawLine(-width, 0, width, 0);
		//draw frame
		g.scale(zoomFactor, zoomFactor);
		if (frame != null) {
			Vector<FLModule> modules = frame.getModules();
			for (FLModule module : modules) {
				module.draw(g);
			}
		}
		//draw selected Area
		if (selectArea != null && !selectArea.isEmpty()) {
			if (selectAlpha >= 0 && selectAlpha <= 0x33) {
				g.setColor(new Color(0, 0, 255, selectAlpha));
				g.fillRect(selectArea.x, selectArea.y, selectArea.width,
						selectArea.height);
			}
		}
		g.scale(1 / zoomFactor, 1 / zoomFactor);
	}

	private FLModule getModuleAt(Point p) {
		if (frame == null) {
			return null;
		}
		Vector<FLModule> modules = frame.getModules();
		FLModule ret = null;
		for (FLModule module : modules) {
			Rectangle rect = module.getBounds();
			if (rect.contains(p)) {
				if(module.isSelected()) {
					return module;
				} else {
					ret = module;
				}
			}
		}
		return ret;
	}

	public void unselectAll() {
		if (frame == null) {
			return;
		}
		Vector<FLModule> modules = frame.getModules();
		for (FLModule m : modules) {
			m.setSelected(false);
		}
	}

	public FLFrame getFrame() {
		return frame;
	}

	public void setFrame(FLFrame frame) {
		if (frame != this.frame) {
			this.frame = frame;
			repaint();
		}
	}

	public float getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(float zoomFactor) {
		if (zoomFactor <= 0) {
			return;
		}
		this.zoomFactor = zoomFactor;
		this.setPreferredSize(new Dimension((int) (AppConfig.config().getStageWidth() * zoomFactor),
				(int) (AppConfig.config().getStageHeight() * zoomFactor)));
		revalidate();
		this.updateUI();
	}
	
	public void resetStage() {
		setZoomFactor(zoomFactor);
		this.scrollRectToVisible(new Rectangle());
		Rectangle rect = this.getVisibleRect();
		int width = getPreferredSize().width;
		int height = getPreferredSize().height;
		rect.setLocation((width - rect.width) / 2, (height - rect.height) / 2);
		this.scrollRectToVisible(rect);
	}
	
	public ModuleSelectedListener getListener() {
		return moduleSelectedListener;
	}

	public void setModuleSelectedListener(ModuleSelectedListener listener) {
		this.moduleSelectedListener = listener;
	}

	public ModuleSelectedListener getModuleSelectedListener() {
		return moduleSelectedListener;
	}
}