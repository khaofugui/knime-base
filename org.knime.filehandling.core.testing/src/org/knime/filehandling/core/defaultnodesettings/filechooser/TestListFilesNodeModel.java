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
 *   Apr 16, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.filechooser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.data.location.FSLocationValueMetaData;
import org.knime.filehandling.core.data.location.cell.FSLocationCellFactory;
import org.knime.filehandling.core.defaultnodesettings.status.PriorityStatusConsumer;
import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage;

/**
 * Node model for the Test List Files node.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class TestListFilesNodeModel extends NodeModel {

    private final SettingsModelFileChooser3 m_settings;

    TestListFilesNodeModel(final PortsConfiguration portsConfig, final SettingsModelFileChooser3 settings) {
        super(portsConfig.getInputPorts(), portsConfig.getOutputPorts());
        m_settings = settings;
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        m_settings.configureInModel(inSpecs, m -> setWarningMessage(m.getMessage()));
        DataTableSpec dataTableSpec = createOutputSpec();
        return new PortObjectSpec[]{dataTableSpec};
    }

    private DataTableSpec createOutputSpec() {
        final DataColumnSpecCreator colCreator = new DataColumnSpecCreator("Location", FSLocationCellFactory.TYPE);
        final FSLocation location = m_settings.getLocation();
        final FSLocationValueMetaData metaData =
            new FSLocationValueMetaData(location.getFileSystemType(), location.getFileSystemSpecifier().orElse(null));
        colCreator.addMetaData(metaData, true);
        return new DataTableSpec(colCreator.createSpec());
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

        final BufferedDataContainer container = exec.createDataContainer(createOutputSpec());
        try (final ReadPathAccessor accessor = m_settings.createReadPathAccessor()) {
            final PriorityStatusConsumer statusConsumer = new PriorityStatusConsumer();
            final Iterator<FSPath> paths = accessor.getPaths(statusConsumer).iterator();
            final Optional<StatusMessage> statusMessage = statusConsumer.get();
            if (statusMessage.isPresent()) {
                setWarningMessage(statusMessage.get().getMessage());
            }
            final FSLocationCellFactory cellFactory =
                new FSLocationCellFactory(FileStoreFactory.createFileStoreFactory(exec), m_settings.getLocation());
            for (long i = 0; paths.hasNext(); i++) {
                container.addRowToTable(
                    new DefaultRow(RowKey.createRowKey(i), cellFactory.createCell(paths.next().toFSLocation())));
            }
        }
        container.close();

        return new PortObject[]{container.getTable()};
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to load
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to save

    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.validateSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettingsForModel(settings);
    }

    @Override
    protected void reset() {
        // nothing to reset
    }

}
