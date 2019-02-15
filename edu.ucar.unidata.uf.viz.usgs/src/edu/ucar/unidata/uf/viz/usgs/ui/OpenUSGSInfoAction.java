package edu.ucar.unidata.uf.viz.usgs.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2019            mjames      Initial creation
 * 
 * </pre>
 * 
 * @author mjames
 * @version 1.0
 */

public class OpenUSGSInfoAction extends AbstractRightClickAction {

    private USGSInfoDialog dialog;

    @Override
    public void run() {
        if (dialog == null) {
            dialog = new USGSInfoDialog(new Shell(Display.getCurrent()),
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
        return "USGS Info...";
    }

}
