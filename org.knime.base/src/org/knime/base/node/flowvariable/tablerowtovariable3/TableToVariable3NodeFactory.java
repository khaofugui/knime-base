/*
 * ------------------------------------------------------------------------
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
 *   Apr 28, 2008 (wiswedel): created
 */
package org.knime.base.node.flowvariable.tablerowtovariable3;

import org.knime.core.webui.node.dialog.impl.WebUINodeConfiguration;
import org.knime.core.webui.node.dialog.impl.WebUINodeFactory;

/**
 * The node factory for the table row to variable node.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
@SuppressWarnings("restriction")
public final class TableToVariable3NodeFactory extends WebUINodeFactory<TableToVariable3NodeModel> {

    private static final WebUINodeConfiguration CONFIG = WebUINodeConfiguration.builder()//
        .name("Table Row to Variable")//
        .icon("./table2variable.png")//
        .shortDescription("""
                Defines new flow variables based on the first row of the input table and exposes
                them using a variable connection.
                """)//
        .fullDescription("""
                This node uses the first row of a data table to define new flow variables. The names of the variables
                are defined by the column names and the variable assignments (i.e. the values) are given by the values
                in the row. The variables are exposed using a variables out connection.
                """)//
        .modelSettingsClass(TableToVariable3NodeSettings.class)//
        .addInputTable("Parameters table", "The table whose first row will be converted to variables.")//
        .addOutputTable("Variables Connection", "Holds created flow variables.")//
        // no since version, not sure since when this node is available
        .build();

    /** Set the node description */
    public TableToVariable3NodeFactory() {
        super(CONFIG);
    }

    @Override
    public TableToVariable3NodeModel createNodeModel() {
        return new TableToVariable3NodeModel();
    }

}
