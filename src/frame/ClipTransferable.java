package frame;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

import model.FLClip;

public class ClipTransferable implements Transferable {
	Vector<String> objs;

	public ClipTransferable(Vector<String> keys) {
		objs = keys;
	}

	public final static DataFlavor CLIP_FLAVOR = new DataFlavor(FLClip.class,
			"FLClip");
	public DataFlavor[] flavors = new DataFlavor[] { CLIP_FLAVOR };

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
		if (df.equals(CLIP_FLAVOR)) {
			return objs;
		}
		return null;
	}
}
