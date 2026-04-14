package gui.dialogs_and_toast;

import dxf.PNEZDPoint;

public interface OnPNEZDItemActionListener {
    void onItemClick(PNEZDPoint point);

    void onItemDelete(PNEZDPoint point);
}