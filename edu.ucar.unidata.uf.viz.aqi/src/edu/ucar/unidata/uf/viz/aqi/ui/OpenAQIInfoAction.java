package edu.ucar.unidata.uf.viz.aqi.ui;

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
 * Mar 18, 2014            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class OpenAQIInfoAction extends AbstractRightClickAction {

    private AQIInfoDialog dialog;

    @Override
    public void run() {
        if (dialog == null) {
            dialog = new AQIInfoDialog(new Shell(Display.getCurrent()),
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
        return "AQI Info...";
    }

}
