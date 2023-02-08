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
 *   Dec 15, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.preproc.column.renamer;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.webui.node.dialog.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.dialog.impl.WebUINodeModel;

/**
 * NodeModel of the ColumnRenamer node.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("restriction")
final class ColumnRenamerNodeModel extends WebUINodeModel<ColumnRenamerSettings> {

    protected ColumnRenamerNodeModel(final WebUINodeConfiguration configuration) {
        super(configuration, ColumnRenamerSettings.class);
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs, final ColumnRenamerSettings modelSettings)
        throws InvalidSettingsException {
        var spec = inSpecs[0];
        var renamer = new Renamer(modelSettings);
        return new DataTableSpec[]{renamer.rename(spec)};
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec,
        final ColumnRenamerSettings modelSettings) throws Exception {
        var table = inData[0];
        var renamer = new Renamer(modelSettings);
        return new BufferedDataTable[]{exec.createSpecReplacerTable(table, renamer.rename(table.getDataTableSpec()))};
    }

    @Override
    protected void validateSettings(final ColumnRenamerSettings settings) throws InvalidSettingsException {
        // TODO validate the settings here once the validation behavior is fixed
    }

    private final class Renamer {

        private final Map<String, String> m_nameMap;

        Renamer(final ColumnRenamerSettings settings) throws InvalidSettingsException {
            m_nameMap = new HashMap<>();
            var newNames = new HashSet<String>();
            for (var renaming : settings.m_renamings) {
                var oldName = renaming.getOldName();
                CheckUtils.checkSetting(m_nameMap.put(oldName, renaming.m_newName) == null,
                    "The column '%s' is renamed more than once.", oldName);
                var newName = renaming.getNewName();
                CheckUtils.checkSetting(StringUtils.isNotBlank(newName), "The new name for '%s' is invalid because it is blank.",
                    oldName);
                CheckUtils.checkSetting(newNames.add(newName), "Multiple columns are renamed to '%s'.", newName);
            }
        }

        DataTableSpec rename(final DataTableSpec spec) throws InvalidSettingsException {
            // initialize with spec to persist table properties
            var specCreator = new DataTableSpecCreator(spec);
            specCreator.dropAllColumns();
            specCreator.addColumns(renameColumns(spec));
            return specCreator.createSpec();
        }

        private DataColumnSpec[] renameColumns(final DataTableSpec spec) throws InvalidSettingsException {
            var columnsToRename = new HashSet<>(m_nameMap.keySet());
            var columns = new ArrayList<DataColumnSpec>(spec.getNumColumns());
            var newNames = new HashSet<String>();
            for (var column : spec) {
                var oldName = column.getName();
                columnsToRename.remove(oldName);
                var newName = rename(oldName);
                CheckUtils.checkSetting(newNames.add(newName), "The column name '%s' is duplicated.", newName);
                columns.add(renameColumn(column, newName));
            }
            if (!columnsToRename.isEmpty()) {
                setWarningMessage(createMissingColumnsWarning(columnsToRename));
            }
            return columns.toArray(DataColumnSpec[]::new);
        }

        private String createMissingColumnsWarning(final Set<String> missingColumns) {
            if (missingColumns.size() == 1) {
                return String.format("The following column is configured but no longer exists: '%s'",
                    missingColumns.iterator().next());
            } else {
                return missingColumns.stream()//
                    .map(n -> "'" + n + "'")//
                    .collect(joining(", ", "The following columns are configured but no longer exist: ", ""));
            }
        }

        private DataColumnSpec renameColumn(final DataColumnSpec column, final String name) {
            var creator = new DataColumnSpecCreator(column);
            creator.setName(name);
            return creator.createSpec();
        }

        private String rename(final String name) {
            var newName = m_nameMap.get(name);
            return newName == null ? name : newName;
        }
    }

}
