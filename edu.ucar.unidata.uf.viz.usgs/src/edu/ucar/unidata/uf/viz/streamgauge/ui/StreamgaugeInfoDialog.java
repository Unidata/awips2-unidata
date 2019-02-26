package edu.ucar.unidata.uf.viz.streamgauge.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

public class StreamgaugeInfoDialog extends CaveSWTDialog {

    private final StreamgaugeInfoControl control;

    /**
     * @param parentShell
     */
    protected StreamgaugeInfoDialog(Shell parentShell,
            AbstractVizResource<?, ?> resource) {
        super(parentShell);
        this.control = new StreamgaugeInfoControl(resource);
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
        comp.setText("River Gauge Table");
        comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        control.initializeControl(comp);
    }

    @Override
    protected void disposed() {
        control.dispose();
    }

}
