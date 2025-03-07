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
 *   19 Dec 2022 (Manuel Hotz, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.preproc.rowagg;

import java.util.stream.Stream;

import org.knime.base.node.preproc.rowagg.RowAggregatorNodeModel.AggregationFunction;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.webui.node.dialog.impl.ChoicesProvider;
import org.knime.core.webui.node.dialog.impl.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.impl.ColumnFilter;
import org.knime.core.webui.node.dialog.impl.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.impl.Schema;

/**
 * Settings for the Row Aggregator node model.
 *
 * @author Manuel Hotz, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("restriction")
final class RowAggregatorSettings implements DefaultNodeSettings {

    @Schema(title = "Aggregation",
            description = "Select the aggregation function to be applied on all rows belonging to the same category."
                + "<ul>"
                // COUNT
                + "<li><i>Occurrence count:</i>"
                + " Count how many rows occur</li>"
                // SUM
                + "<li><i>Sum:</i>"
                + " Sum up values, optionally weighted by the value from the weight column</li>"
                // AVERAGE
                + "<li><i>Average:</i>"
                + " Calculate the mean value, optionally weighted by the value from the weight column</li>"
                // MIN
                + "<li><i>Minimum:</i>"
                + " Calculate the minimum value</li>"
                // MAX
                + "<li><i>Maximum:</i>"
                + " Calculate the maximum value</li>"
                + "</ul>")
    AggregationFunction m_aggregationMethod = AggregationFunction.SUM;

    @Schema(title = "Aggregation columns", description = "Select the columns to apply the aggregation function to.",
            choices = AggregatableColumns.class)
    ColumnFilter m_frequencyColumns;

    static final class AggregatableColumns implements ColumnChoicesProvider {

        @Override
        public DataColumnSpec[] columnChoices(final SettingsCreationContext context) {
            return context.getDataTableSpec(0)
                    .map(DataTableSpec::stream)//
                    .orElseGet(Stream::empty)//
                    .filter(RowAggregatorNodeModel::isAggregatableColumn)//
                    .toArray(DataColumnSpec[]::new);
        }

    }

    @Schema(title = "Weight column", description = "Select the column that defines the weight with which a "
        + "value is multiplied before aggregation. Note, that only the aggregation functions \"Sum\" and \"Average\" "
        + "support a weight column",
            choices = WeightColumns .class)
    String m_weightColumn;

    static final class WeightColumns implements ChoicesProvider {

        @Override
        public String[] choices(final SettingsCreationContext context) {
            return context.getDataTableSpec(0)
                    .map(DataTableSpec::stream)//
                    .orElseGet(Stream::empty)//
                    .filter(RowAggregatorNodeModel::isWeightColumn)//
                    .map(DataColumnSpec::getName)
                    .toArray(String[]::new);
        }

    }

    @Schema(title = "Category column", description = "Select the column that defines the category on which rows "
        + "are grouped. If no category column is selected, \"grand total\" values in which all rows belong to the same "
        + "group will be calculated.",
            choices = CategoryColumns.class)
    String m_categoryColumn;

    static final class CategoryColumns implements ChoicesProvider {
        @Override
        public String[] choices(final SettingsCreationContext context) {
            return context.getDataTableSpec(0).map(DataTableSpec::getColumnNames).orElse(new String[0]);
        }
    }

    @Schema(title = "Additional \"grand totals\" at second output port",
            description = "If a category column is selected, additionally compute the aggregations <i>without</i> the "
                + "category column (\"grand totals\") and output them in the second output table. "
                + "The second output is inactive if no category "
                + "column is selected or this setting is not enabled.")
    boolean m_grandTotals;

    /**
     * Constructor for de/serialization.
     */
    RowAggregatorSettings() {
        // required by interface
    }

    RowAggregatorSettings(final SettingsCreationContext ctx) {
        m_frequencyColumns = ColumnFilter.createDefault(AggregatableColumns.class, ctx);
    }
}
