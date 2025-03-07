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
 * ------------------------------------------------------------------------
 *
 * History
 *   16.04.2010 (hofer): created
 */
package org.knime.base.node.preproc.sorter;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.knime.base.node.preproc.sorter.dialog.DynamicSorterPanel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DefaultTable;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.DefaultNodeProgressMonitor;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.Node;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.workflow.SingleNodeContainer;

/**
 * Junit4 Test for {@link org.knime.base.node.preproc.sorter.SorterNodeModel}
 *
 * @author Heiko Hofer
 */
public class SorterNodeModelTest {
    private static ExecutionContext EXEC_CONTEXT;

    private SorterNodeModel m_snm;
    private NodeSettings m_settings;

    private static final String[] DEFAULT_TWO_COLS = {"TestCol1", "TestCol2"};

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        EXEC_CONTEXT = new ExecutionContext(
           new DefaultNodeProgressMonitor(), new Node(new SorterNodeFactory()),
                    SingleNodeContainer.MemoryPolicy.CacheSmallInMemory, new HashMap<Integer, ContainerTable>());
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        EXEC_CONTEXT = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        m_snm = new SorterNodeModel();
        m_settings = new NodeSettings("Sorter");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        m_snm = null;
        m_settings = null;
    }


    /**
     * Test method for {@link org.knime.base.node.preproc.sorter.SorterNodeModel#saveSettingsTo(...)}.
     */
    @Test
    public final void testSaveSettingsTo() throws InvalidSettingsException {
        Assert.assertFalse(m_settings.containsKey(
                SorterNodeModel.INCLUDELIST_KEY));
        Assert.assertFalse(m_settings.containsKey(
                SorterNodeModel.SORTORDER_KEY));
        Assert.assertFalse(m_settings.containsKey(
                SorterNodeModel.SORTINMEMORY_KEY));

        // save empty
        m_snm.saveSettingsTo(m_settings);

        // populate settings
        boolean[] sortOrder = {true, false, true};
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortOrder);
        String[] inclCols = {"TestCol1", "TestCol2", "-ROWKEY -"};
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, inclCols);
        boolean sortInMemory = false;
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, sortInMemory);

        m_snm.validateSettings(m_settings);
        m_snm.loadValidatedSettingsFrom(m_settings);
        m_snm.configure(new DataTableSpec[] { new DataTableSpec(new String[] {"TestCol1", "TestCol2"},
            new DataType[] {StringCell.TYPE, DoubleCell.TYPE})
            });

        // since alphanum was not present in the settings, the default should be 'false' here for backwards
        // compatibility.


        NodeSettings newsettings = new NodeSettings("Sorter");
        m_snm.saveSettingsTo(newsettings);

        boolean[] sortOrderTest = newsettings
                    .getBooleanArray(SorterNodeModel.SORTORDER_KEY);
        Assert.assertTrue(sortOrderTest[0]);
        Assert.assertFalse(sortOrderTest[1]);
        Assert.assertTrue(sortOrderTest[2]);
        Assert.assertEquals(3, sortOrderTest.length);

        boolean[] alphanum = newsettings.getBooleanArray(SorterNodeModel.ALPHANUMCOMP_KEY);
        for (var i = 0; i < alphanum.length; i++) {
            Assert.assertFalse(alphanum[i]);
        }

        String[] inclColsTest = newsettings
                    .getStringArray(SorterNodeModel.INCLUDELIST_KEY);
        Assert.assertArrayEquals(inclCols, inclColsTest);
    }

    /**
     * Test method for {@link org.knime.base.node.preproc.sorter.SorterNodeModel#validateSettings(org.knime.core.node.NodeSettingsRO)}.
     */
    @Test(expected = InvalidSettingsException.class)
    public final void testValidateSettingsEmpty() throws InvalidSettingsException{
        // try to validate an empty settings-object
        m_snm.validateSettings(m_settings);
    }

    @Test
    public final void testValidateSettingsIncorrectKey() throws InvalidSettingsException{
        // add two null objects with incorrect keys
        m_settings.addStringArray("Incorrect Key 1", (String[])null);
        m_settings.addBooleanArray("Incorrect Key 2", null);
        m_settings.addBoolean("Incorrect Key 3", false);
        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsIncorrectValue() throws InvalidSettingsException{
        // add two null objects with the correct keys
        // to the settings object
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY,
                (String[])null);
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, null);
        m_settings.addBooleanArray(SorterNodeModel.ALPHANUMCOMP_KEY, null);
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, false);

        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsNoSortOrder() throws InvalidSettingsException {
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, null);
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        boolean sortInMemory = false;
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, sortInMemory);

        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsNoAlphanum() throws InvalidSettingsException {
        m_settings.addBooleanArray(SorterNodeModel.ALPHANUMCOMP_KEY, null);
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, new boolean[] {true, true});
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, false);

        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsDuplicateColumn() throws InvalidSettingsException {
        final String[] inclCols = {"TestCol1Dup", "TestCol1Dup"};
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, inclCols);
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, new boolean[] {true, false});
        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsNoSortOrderKey() throws InvalidSettingsException {
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettingsMismatchedColumnsAndSortOrders() throws InvalidSettingsException {
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, new boolean[] {true});
        assertThrows(InvalidSettingsException.class, () -> m_snm.validateSettings(m_settings));
    }

    @Test
    public final void testValidateSettings() throws InvalidSettingsException {
        boolean[] sortOrder = {true, false};
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortOrder);
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        boolean sortInMemory = false;
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, sortInMemory);

        m_snm.validateSettings(m_settings);
    }

    /**
     * Test method for {@link org.knime.base.node.preproc.sorter.SorterNodeModel#loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)}.
     */
    @Test
    public final void testLoadValidatedSettingsFrom() throws InvalidSettingsException {
        boolean[] sortOrder = {true, false};
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortOrder);
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DEFAULT_TWO_COLS);
        boolean sortInMemory = false;
        m_settings.addBoolean(SorterNodeModel.SORTINMEMORY_KEY, sortInMemory);

        m_snm.validateSettings(m_settings);
        m_snm.loadValidatedSettingsFrom(m_settings);
    }

    @Test

    public final void testConfigureValid() throws InvalidSettingsException {
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Col1", StringCell.TYPE).createSpec());
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, "Col1");
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, false);
        m_snm.loadValidatedSettingsFrom(m_settings);
        assertEquals(1, m_snm.configure(new DataTableSpec[] { dts }).length);
    }

    @Test
    public final void testConfigureInvalid() throws InvalidSettingsException {
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Col1", StringCell.TYPE).createSpec());
        assertThrows(InvalidSettingsException.class, () -> m_snm.configure(new DataTableSpec[] { dts }));
    }

    @Test
    public final void testConfigureMissingColumns() throws InvalidSettingsException {
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, "-ROWKEY -", "Col1");
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, true, false);
        m_snm.loadValidatedSettingsFrom(m_settings);
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Missing", StringCell.TYPE).createSpec());
        assertThrows(InvalidSettingsException.class, () -> m_snm.configure(new DataTableSpec[] { dts }));
    }

    @Test
    public final void testConfigureInvalidAlphanumComp() throws InvalidSettingsException {
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, "NoAlphanum");
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, false);
        m_settings.addBooleanArray(SorterNodeModel.ALPHANUMCOMP_KEY, true);
        m_snm.loadValidatedSettingsFrom(m_settings);
        final var dts = new DataTableSpec(
            new DataColumnSpecCreator("NoAlphanum", DoubleCell.TYPE).createSpec()
            );
        assertThrows(InvalidSettingsException.class, () -> m_snm.configure(new DataTableSpec[] { dts }));
    }

    // test execute

    /**
     * Tests that for empty sort key the original table is returned.
     * @throws Exception settings invalid or execution cancelled
     */
    @Test
    public final void testExecuteEmptySortKey() throws Exception {
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Column1", StringCell.TYPE).createSpec());
        // m_snm.configure(new DataTableSpec[] {dts});
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, new String[] {});
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, new boolean[] {});
        m_snm.loadValidatedSettingsFrom(m_settings);
        final var cont = EXEC_CONTEXT.createDataContainer(dts);
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(0L), new StringCell("10")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(1L), new StringCell("2")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(2L), new StringCell("1")));
        cont.close();
        final var input = cont.getTable();
        final var sorted = m_snm.execute(
            EXEC_CONTEXT.createBufferedDataTables(new DataTable[] { input }, EXEC_CONTEXT), EXEC_CONTEXT)[0];
        // since we have no sort columns specified, we should get the original table back
        assertEquals(input, sorted);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testExecuteColumnNotFound() throws Exception {
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Column1", StringCell.TYPE).createSpec());
        // m_snm.configure(new DataTableSpec[] {dts});
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, "NotFound");
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, true);
        m_snm.loadValidatedSettingsFrom(m_settings);
        final var cont = EXEC_CONTEXT.createDataContainer(dts);
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(0L), new StringCell("10")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(1L), new StringCell("2")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(2L), new StringCell("1")));
        cont.close();
        final var input = cont.getTable();
        m_snm.execute(EXEC_CONTEXT.createBufferedDataTables(new DataTable[] { input }, EXEC_CONTEXT), EXEC_CONTEXT);
    }

    /**
     * Test for sorting with alphanumeric comparator of row keys.
     * @throws Exception while executing
     */
    @Test
    public final void testExecuteSortOnRowKeyAlphanum() throws Exception {
        final var dts = new DataTableSpec(new DataColumnSpecCreator("Column1", StringCell.TYPE).createSpec());
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, DynamicSorterPanel.ROWKEY.getName());
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, true);
        m_settings.addBooleanArray(SorterNodeModel.ALPHANUMCOMP_KEY, true);
        m_snm.loadValidatedSettingsFrom(m_settings);
        m_snm.configure(new DataTableSpec[] {dts});

        final var referenceKeys = Arrays.stream(new long[] {1, 2, 10}).iterator();
        final var cont = EXEC_CONTEXT.createDataContainer(dts);
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(10L), new StringCell("10")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(2L), new StringCell("2")));
        cont.addRowToTable(new DefaultRow(RowKey.createRowKey(1L), new StringCell("1")));
        cont.close();
        final var input = cont.getTable();
        final var sorted = m_snm.execute(
            EXEC_CONTEXT.createBufferedDataTables(new DataTable[] { input }, EXEC_CONTEXT), EXEC_CONTEXT)[0];
        try (final var it = sorted.iterator()) {
            while (it.hasNext()) {
                final var reference = referenceKeys.next();
                final var next = it.next();
                assertEquals(RowKey.createRowKey(reference), next.getKey());
            }
        }
    }

    /**
     * Test method for {@link org.knime.base.node.preproc.sorter.SorterNodeModel#execute(org.knime.core.node.BufferedDataTable[], org.knime.core.node.ExecutionContext)}.
     * @throws Exception
     * @throws CanceledExecutionException
     */
    @Test
    public final void testExecuteBufferedDataTableArrayExecutionContext() throws CanceledExecutionException, Exception {
        // try to sort a table with 1 entry
        String[] columnNames = {"col1", "col2", "col3", "col4"};
        DataType[] columnTypes = {DoubleCell.TYPE, StringCell.TYPE,
                IntCell.TYPE, DoubleCell.TYPE};
        DataRow[] rows = new DataRow[1];
        DataCell[] myRow = new DataCell[4];
        myRow[0] = new DoubleCell(2.4325);
        myRow[1] = new StringCell("Test");
        myRow[2] = new IntCell(7);
        myRow[3] = new DoubleCell(32432.324);
        rows[0] = new DefaultRow(Integer.toString(1), myRow);

        DataTable[] inputTable = {new DefaultTable(rows, columnNames,
                columnTypes)};
        DataTable[] resultTable = {new DefaultTable(rows, columnNames,
                columnTypes)};

        // set settings
        String[] includeCols = {"col1", "col2", "col3", "col4"};
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, includeCols);
        boolean[] sortorder = {true, true, true, true};
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortorder);

        m_snm.loadValidatedSettingsFrom(m_settings);
        m_snm.configure(new DataTableSpec[] {inputTable[0].getDataTableSpec()});
        resultTable = m_snm.execute(EXEC_CONTEXT.createBufferedDataTables(
                    inputTable, EXEC_CONTEXT), EXEC_CONTEXT);

        // test output

        RowIterator rowIt = resultTable[0].iterator();
        Assert.assertTrue(rowIt.hasNext());
        Assert.assertEquals(rows[0], rowIt.next());
        Assert.assertFalse(rowIt.hasNext());
        m_snm.reset();

        // *********************************************//
        // try to sort a large array of DataRows
        // In this case we generate a unit matrix
        // *********************************************//

        // start with a little one
        int dimension = 50;
        // *********************************************//
        // set settings
        includeCols = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            includeCols[i] = "col" + i;
        }
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, includeCols);
        sortorder = new boolean[dimension];
        for (int i = 0; i < dimension; i++) {
            sortorder[i] = true;
        }
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortorder);

        DataTable[] inputTable2 = {generateUnitMatrixTable(dimension)};


        m_snm.loadValidatedSettingsFrom(m_settings);

        resultTable = m_snm.execute(EXEC_CONTEXT.createBufferedDataTables(
                    inputTable2, EXEC_CONTEXT), EXEC_CONTEXT);


        // test output (should have sorted all rows in reverse order)
        rowIt = resultTable[0].iterator();
        Assert.assertTrue(rowIt.hasNext());
        int k = dimension - 1;
        while (rowIt.hasNext()) {
            RowKey rk = rowIt.next().getKey();
            int ic = Integer.parseInt(rk.getString());
            Assert.assertEquals(k, ic);
            k--;
        }
        Assert.assertFalse(rowIt.hasNext());
        m_snm.reset();

        // *********************************************//
        // try to sort a very large array of DataRows
        // In this case we generate a unit matrix
        // *********************************************//
        // dimension 300 => 15,8 secs.
        // dimension 500 => 49,7 secs.
        dimension = 100;
        // *********************************************//
        // set settings
        includeCols = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            includeCols[i] = "col" + i;
        }
        m_settings.addStringArray(SorterNodeModel.INCLUDELIST_KEY, includeCols);
        sortorder = new boolean[dimension];
        for (int i = 0; i < dimension; i++) {
            sortorder[i] = true;
        }
        m_settings.addBooleanArray(SorterNodeModel.SORTORDER_KEY, sortorder);

        DataTable[] inputTable3 = {generateUnitMatrixTable(dimension)};


        m_snm.loadValidatedSettingsFrom(m_settings);

        resultTable = m_snm.execute(EXEC_CONTEXT.createBufferedDataTables(
                    inputTable3, EXEC_CONTEXT), EXEC_CONTEXT);

        // test output (should have sorted all rows in reverse order)
        rowIt = resultTable[0].iterator();
        Assert.assertTrue(rowIt.hasNext());
        k = dimension - 1;
        while (rowIt.hasNext()) {
            RowKey rk = rowIt.next().getKey();
            int ic = Integer.parseInt(rk.getString());
            Assert.assertEquals(k, ic);
            k--;
        }
        Assert.assertFalse(rowIt.hasNext());
        m_snm.reset();
    }

    /**
     * This method produces a unit matrix -<code>DataTable</code>.
     *
     * @param dimension of the matrix
     * @return Unit matrix
     */
    public DefaultTable generateUnitMatrixTable(final int dimension) {
        // generate Column names and types
        String[] columnNames = new String[dimension];
        for (int i = 0; i < dimension; i++) {
            columnNames[i] = "col" + i;
        }
        DataType[] columnTypes = new DataType[dimension];
        for (int i = 0; i < dimension; i++) {
            columnTypes[i] = IntCell.TYPE;
        }

        DataRow[] unitmatrix = new DataRow[dimension];
        for (int i = 0; i < dimension; i++) {
            DataCell[] myRow = new DataCell[dimension];
            for (int j = 0; j < dimension; j++) {
                if (i == j) {
                    myRow[j] = new IntCell(1);
                } else {
                    myRow[j] = new IntCell(0);
                }
            }
            DataRow temprow = new DefaultRow(Integer.toString(i), myRow);
            unitmatrix[i] = temprow;
        }
        return new DefaultTable(unitmatrix, columnNames, columnTypes);
    }


}
