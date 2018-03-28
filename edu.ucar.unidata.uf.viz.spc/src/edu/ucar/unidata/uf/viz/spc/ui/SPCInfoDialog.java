package edu.ucar.unidata.uf.viz.spc.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2018            mjames@ucar Initial creation
 * 
 * @author mjames
 */

public class SPCInfoDialog extends CaveSWTDialog {

    private final SPCInfoControl control;

    /**
     * @param parentShell
     */
    protected SPCInfoDialog(Shell parentShell,
            AbstractVizResource<?, ?> resource) {
        super(parentShell);
        this.control = new SPCInfoControl(resource);
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
        comp.setText("SPC Info");
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        control.initializeControl(comp);
    }

    @Override
    protected void disposed() {
        control.dispose();
    }

}
