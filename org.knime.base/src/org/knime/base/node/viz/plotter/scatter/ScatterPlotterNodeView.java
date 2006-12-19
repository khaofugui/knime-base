/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   26.09.2006 (Fabian Dill): created
 */
package org.knime.base.node.viz.plotter.scatter;

import org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeView;
import org.knime.core.node.NodeModel;

/**
 * Extends the 
 * {@link org.knime.base.node.viz.plotter.node.DefaultVisualizationNodeView} by
 * adding an additional menu to show, fade or hide unhilited dots.
 * 
 * @author Fabian Dill, University of Konstanz
 */
public class ScatterPlotterNodeView extends DefaultVisualizationNodeView {
    
    /**
     * Adds  a show/hide menu to the menu bar.
     * 
     * @param model the node model
     * @param plotter the plotter
     */
    public ScatterPlotterNodeView(final NodeModel model, 
            final ScatterPlotter plotter) {
        super(model, plotter);
        getJMenuBar().add(plotter.getShowHideMenu());
    }
    
}
