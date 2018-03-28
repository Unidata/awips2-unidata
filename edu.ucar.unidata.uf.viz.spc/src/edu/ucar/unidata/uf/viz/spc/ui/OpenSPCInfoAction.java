package edu.ucar.unidata.uf.viz.spc.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2018            mjames@ucar Initial creation
 * 
 * @author mjames
 */

public class OpenSPCInfoAction extends AbstractRightClickAction {

    private SPCInfoDialog dialog;

    @Override
    public void run() {
        if (dialog == null) {
            dialog = new SPCInfoDialog(new Shell(Display.getCurrent()),
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
        return "SPC Info...";
    }

}
