/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package edu.ucar.unidata.uf.viz.aqi.rsc.handler;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.time.BinOffset;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceProperties;

import edu.ucar.unidata.common.dataplugin.aqi.AQIRecord;
import edu.ucar.unidata.uf.viz.aqi.rsc.AQIResourceData;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 17, 2014            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */

public class AQIResourceLoader extends AbstractHandler {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // First get active window
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window != null) {
            // Have to check for null since this handler could be triggered in
            // any fashion, not just by a menu click
            IEditorPart editor = HandlerUtil.getActiveEditor(event);
            if (editor != null) {
                // Have to check for null since all editors may be closed
                if (editor instanceof IDisplayPaneContainer) {
                    // Check if editor is interface we can load to
                    IDisplayPaneContainer container = (IDisplayPaneContainer) editor;
                    for (IDisplayPane pane : container.getDisplayPanes()) {
                        // Load to all panes
                        HashMap<String, RequestConstraint> metadataMap = new HashMap<String, RequestConstraint>();
                        metadataMap.put(PluginDataObject.PLUGIN_NAME_ID,
                                new RequestConstraint(AQIRecord.PLUGIN_NAME));
                        AQIResourceData data = new AQIResourceData();
                        data.setMetadataMap(metadataMap);
                        data.setBinOffset(new BinOffset(1800, 1800));
                        LoadProperties lProps = new LoadProperties();
                        ResourceProperties rProps = new ResourceProperties();
                        ResourcePair rp = new ResourcePair();
                        rp.setResourceData(data);
                        rp.setLoadProperties(lProps);
                        rp.setProperties(rProps);
                        pane.getDescriptor().getResourceList().add(rp);
                        pane.getDescriptor()
                                .getResourceList()
                                .instantiateResources(pane.getDescriptor(),
                                        true);
                    }
                }
            }
        }
        return null;
    }

}
