package frame;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import model.FLFrame;

public class FrameTransferable implements Transferable {
	Integer frameId;

	public FrameTransferable(int frameId) {
		this.frameId = frameId;
	}

	public final static DataFlavor FRAME_FLAVOR = new DataFlavor(FLFrame.class,
			"FLFrame");
	public DataFlavor[] flavors = new DataFlavor[] { FRAME_FLAVOR };

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor df : flavors) {
			if (df.equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor df)
			throws UnsupportedFlavorException, IOException {
		if (df.equals(FRAME_FLAVOR)) {
			return frameId;
		}
		return null;
	}
}
