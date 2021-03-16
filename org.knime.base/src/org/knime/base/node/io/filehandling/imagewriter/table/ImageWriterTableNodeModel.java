/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   15 Mar 2021 (Laurin Siefermann, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.io.filehandling.imagewriter.table;

import java.io.File;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Node model of the image writer table node.
 *
 * @author Laurin Siefermann, KNIME GmbH, Konstanz, Germany
 */
final class ImageWriterTableNodeModel extends NodeModel {

    private final int m_inputTableIdx;

    /**
     * Constructor.
     *
     * @param config the {@link PortsConfiguration} to initialize the ImageWriterTableNodeModel
     * @param inputTableIdx the index of the input table
     */
    ImageWriterTableNodeModel(final PortsConfiguration config, final int inputTableIdx) {
        super(config.getInputPorts(), config.getOutputPorts());
        m_inputTableIdx = inputTableIdx;
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) {
        final ColumnRearranger c = createColumnRearranger((DataTableSpec)inSpecs[m_inputTableIdx]);
        final DataTableSpec inputTableSpec = c.createSpec();
        return new PortObjectSpec[]{inputTableSpec};
    }

    @Override
    protected BufferedDataTable[] execute(final PortObject[] inObjects, final ExecutionContext exec)
        throws CanceledExecutionException {
        final BufferedDataTable inputDataTable = (BufferedDataTable)inObjects[m_inputTableIdx];
        final ColumnRearranger c = createColumnRearranger((DataTableSpec)inObjects[m_inputTableIdx].getSpec());
        final BufferedDataTable out = exec.createColumnRearrangeTable(inputDataTable, c, exec);
        return new BufferedDataTable[]{out};
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) {
        // Nothing to do
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) {
        // Nothing to do
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // Nothing to do
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) {
        // Nothing to do
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) {
        // Nothing to do
    }

    @Override
    protected void reset() {
        // Nothing to do
    }

    private static final ColumnRearranger createColumnRearranger(final DataTableSpec in) {
        return new ColumnRearranger(in);
    }

}
