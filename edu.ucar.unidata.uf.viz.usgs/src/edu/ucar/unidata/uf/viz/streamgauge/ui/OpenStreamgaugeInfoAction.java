package edu.ucar.unidata.uf.viz.streamgauge.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.dialogs.ICloseCallback;


public class OpenStreamgaugeInfoAction extends AbstractRightClickAction {

    private StreamgaugeInfoDialog dialog;

    @Override
    public void run() {
        if (dialog == null) {
            dialog = new StreamgaugeInfoDialog(new Shell(Display.getCurrent()),
                    getTopMostSelectedResource());
            dialog.setCloseCallback(new ICloseCallback() {
                @Override
                public void dialogClosed(Object returnValue) {
                    dialog = null;
                }
            });
        }
        dialog.open();
    }

    @Override
    public String getText() {
        return "River Gauge Info...";
    }

}
