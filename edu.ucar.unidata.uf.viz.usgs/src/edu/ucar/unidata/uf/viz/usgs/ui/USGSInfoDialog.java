package edu.ucar.unidata.uf.viz.usgs.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

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

public class USGSInfoDialog extends CaveSWTDialog {

    private final USGSInfoControl control;

    /**
     * @param parentShell
     */
    protected USGSInfoDialog(Shell parentShell,
            AbstractVizResource<?, ?> resource) {
        super(parentShell);
        this.control = new USGSInfoControl(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        Group comp = new Group(shell, SWT.NONE);
        comp.setText("USGS Info");
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        control.initializeControl(comp);
    }

    @Override
    protected void disposed() {
        control.dispose();
    }

}
