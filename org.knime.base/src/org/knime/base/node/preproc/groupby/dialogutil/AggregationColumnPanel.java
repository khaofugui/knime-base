/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * -------------------------------------------------------------------
 *
 * History
 *    27.08.2008 (Tobias Koetter): created
 */

package org.knime.base.node.preproc.groupby.dialogutil;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;

import org.knime.base.node.preproc.groupby.aggregation.AggregationMethods;
import org.knime.base.node.preproc.groupby.aggregation.ColumnAggregator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;


/**
 * This class creates the aggregation column panel that allows the user to
 * define the aggregation columns and their aggregation method.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class AggregationColumnPanel extends MouseAdapter {

    /**The initial dimension of this panel.*/
    public static final Dimension PANEL_DIMENSION = new Dimension(650, 200);

    private static final int BUTTON_WIDTH = 125;

    private static final int COMPONENT_HEIGHT = 155;

    private final JPanel m_panel = new JPanel();

    private final List<DataColumnSpec> m_avAggrColSpecs =
        new LinkedList<DataColumnSpec>();

    private final DefaultListModel m_avAggrColListModel =
            new DefaultListModel();

    private final JList m_avAggrColList;

    private final AggregationColumnTableModel m_aggrColTableModel
        = new AggregationColumnTableModel();

    private final JTable m_aggrColTable;

    /**
     * This class implements the context menu functionality of the aggregation
     * column table.
     * @author Tobias Koetter, University of Konstanz
     */
    private class AggregationColumnTableListener extends MouseAdapter {

        /**Constructor for class
         * AggregationColumnPanel.AggregationColumnTableListener.
         */
        AggregationColumnTableListener() {
            //nothing to do
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                onRemIt();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mousePressed(final MouseEvent e) {
            maybeShowContextMenu(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mouseReleased(final MouseEvent e) {
            maybeShowContextMenu(e);
        }

        private void maybeShowContextMenu(final MouseEvent e) {
            if (e.isPopupTrigger()) {
//                final int rowIdx = m_aggrColTable.rowAtPoint(e.getPoint());
                final JPopupMenu menu = createPopupMenu();
                menu.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }

        private JPopupMenu createPopupMenu() {
            final JPopupMenu menu = new JPopupMenu();
            if (getAggregationColumnCount() == 0) {
                //the table contains no rows
                final JMenuItem item =
                    new JMenuItem("No column(s) available");
                item.setEnabled(false);
                menu.add(item);
                return menu;
            }
            final JMenuItem selectNominal =
                new JMenuItem("Select all none numerical columns");
            selectNominal.addActionListener(new ActionListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    selectAllNoneNumericalRows();
                }
            });
            menu.add(selectNominal);
            final JMenuItem selectNumerical =
                new JMenuItem("Select all numerical columns");
            selectNumerical.addActionListener(new ActionListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    selectAllNumericalRows();
                }
            });
            menu.add(selectNumerical);
            final JMenuItem selectAll =
                new JMenuItem("Select all columns");
            selectAll.addActionListener(new ActionListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    selectAllRows();
                }
            });
            menu.add(selectAll);
            menu.addSeparator();

            final JMenu aggrMenu = new JMenu("Aggregation method");

            if (!rowsSelected()) {
                    final JMenuItem noneSelected =
                        new JMenuItem("No column selected");
                    noneSelected.setEnabled(false);
                    aggrMenu.add(noneSelected);
            } else {
                final List<String> labels = getMethods4SelectedItems();
                for (final String label : labels) {
                    final JMenuItem methodItem =
                        new JMenuItem(label);
                    methodItem.addActionListener(new ActionListener() {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            changeAggregationMethod(label);
                        }
                    });
                    aggrMenu.add(methodItem);
                }
            }
            menu.add(aggrMenu);
            return menu;
        }
    }

    /**Constructor for class AggregationColumnPanel.
     *
     */
    public AggregationColumnPanel() {
        m_avAggrColList = new JList(m_avAggrColListModel);
//        m_avAggrColList.setVisibleRowCount(8);
        m_avAggrColList.setCellRenderer(new DataColumnSpecListCellRenderer());
        m_avAggrColList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1
                        && e.getClickCount() == 2) {
                    onAddIt();
                }
            }
        });

        m_aggrColTable = new JTable(m_aggrColTableModel);
        m_aggrColTable.setFillsViewportHeight(true);
        m_aggrColTable.getTableHeader().setReorderingAllowed(false);
        m_aggrColTable.getColumnModel().getColumn(0).setCellRenderer(
                new DataColumnSpecTableCellRenderer());
        m_aggrColTable.getColumnModel().getColumn(1).setCellEditor(
                new AggregationMethodTableCellEditor(m_aggrColTableModel));
        m_aggrColTable.getColumnModel().getColumn(1).setCellRenderer(
                new AggregationMethodTableCellRenderer());
        m_aggrColTable.addMouseListener(new AggregationColumnTableListener());

        m_panel.setMinimumSize(PANEL_DIMENSION);
        m_panel.setPreferredSize(PANEL_DIMENSION);
        m_panel.setLayout(new BoxLayout(m_panel, BoxLayout.X_AXIS));
        final Box rootBox = new Box(BoxLayout.X_AXIS);
        final Border border = BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), " Aggregation settings ");
        rootBox.setBorder(border);
        rootBox.add(Box.createHorizontalGlue());
        rootBox.add(createAggrColBox());
        rootBox.add(Box.createHorizontalGlue());
        rootBox.add(createButtonBox());
        rootBox.add(Box.createHorizontalGlue());
        rootBox.add(createAggrColTable());
        rootBox.add(Box.createHorizontalGlue());
        m_panel.add(rootBox);
    }

    private Box createAggrColBox() {
        final Box aggrColBox = new Box(BoxLayout.X_AXIS);
        final Border border =
            BorderFactory.createTitledBorder(" Available columns ");
        aggrColBox.setBorder(border);
        aggrColBox.add(Box.createHorizontalGlue());
        final JScrollPane avColList = new JScrollPane(m_avAggrColList);
        final Dimension dimension = new Dimension(125, COMPONENT_HEIGHT);
        avColList.setMinimumSize(dimension);
        avColList.setPreferredSize(dimension);
        aggrColBox.add(avColList);
        aggrColBox.add(Box.createHorizontalGlue());
        return aggrColBox;
    }

    private Component createButtonBox() {
        final Box buttonBox = new Box(BoxLayout.Y_AXIS);
        buttonBox.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        buttonBox.add(Box.createVerticalGlue());

        final JButton addButton = new JButton("add >>");
        addButton.setMaximumSize(new Dimension(BUTTON_WIDTH, 25));
        buttonBox.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                onAddIt();
            }
        });
        buttonBox.add(Box.createVerticalGlue());

        final JButton addAllButton = new JButton("add all >>");
        addAllButton.setMaximumSize(new Dimension(BUTTON_WIDTH, 25));
        buttonBox.add(addAllButton);
        addAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                onAddAll();
            }
        });
        buttonBox.add(Box.createVerticalGlue());

        final JButton remButton = new JButton("<< remove");
        remButton.setMaximumSize(new Dimension(BUTTON_WIDTH, 25));
        buttonBox.add(remButton);
        remButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                onRemIt();
            }
        });
        buttonBox.add(Box.createVerticalGlue());

        final JButton remAllButton = new JButton("<< remove all");
        remAllButton.setMaximumSize(new Dimension(BUTTON_WIDTH, 25));
        buttonBox.add(remAllButton);
        remAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent ae) {
                onRemAll();
            }
        });
        buttonBox.add(Box.createVerticalGlue());
        return buttonBox;
    }

    private Component createAggrColTable() {
        final Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createVerticalGlue());
        final Box labelBox = new Box(BoxLayout.X_AXIS);
        labelBox.add(Box.createHorizontalGlue());
        labelBox.add(new JLabel("To change multiple columns use "
                + "right mouse click for context menu."));
        labelBox.add(Box.createHorizontalGlue());
        box.add(labelBox);
        box.add(Box.createVerticalGlue());
        final JScrollPane pane = new JScrollPane(m_aggrColTable);
//        final int width =
//            (int)Math.ceil(
//                    (GroupByNodeDialog.COMPONENT_WIDTH - BUTTON_WIDTH) / 1.5);
//        final Dimension dimension =
//            new Dimension(width , COMPONENT_HEIGHT);
//        pane.setMinimumSize(dimension);
//        pane.setPreferredSize(dimension);
        box.add(pane);
        box.add(Box.createVerticalGlue());
        return box;
    }

    /**
     * Removes all columns from the aggregation column table.
     */
    protected void onRemAll() {
        m_aggrColTableModel.removeAll();
    }

    /**
     * Removes the selected columns from the aggregation column table.
     */
    protected void onRemIt() {
        m_aggrColTableModel.removeColumn(m_aggrColTable.getSelectedRows());
    }

    /**
     *  Adds all columns to the aggregation column table.
     */
    protected void onAddAll() {
        final DataColumnSpec[] specs =
            new DataColumnSpec[m_avAggrColListModel.getSize()];
        for (int i = 0, size = m_avAggrColListModel.getSize(); i < size; i++) {
            specs[i] = (DataColumnSpec)m_avAggrColListModel.get(i);
        }
        m_aggrColTableModel.addColumn(specs);
    }

    /**
     * Adds all selected columns to the aggregation column table.
     */
    protected void onAddIt() {
        final Object[] values = m_avAggrColList.getSelectedValues();
        if (values == null || values.length < 1) {
            return;
        }
        final DataColumnSpec[] specs = new DataColumnSpec[values.length];
        for (int i = 0, length = values.length; i < length; i++) {
            specs[i] = (DataColumnSpec)values[i];
        }
        m_aggrColTableModel.addColumn(specs);
    }

    /**
     * Changes the aggregation method of all selected rows to the method
     * with the given label.
     * @param methodLabel the label of the aggregation method
     */
    protected void changeAggregationMethod(final String methodLabel) {
        final int[] selectedRows = m_aggrColTable.getSelectedRows();
        m_aggrColTableModel.setAggregationMethod(selectedRows,
                AggregationMethods.getMethod4Label(methodLabel));
        final Collection<Integer> idxs = new LinkedList<Integer>();
        for (final int i : selectedRows) {
            idxs.add(new Integer(i));
        }
        updateSelection(idxs);
    }

    /**
     * Selects all rows.
     */
    protected void selectAllRows() {
        m_aggrColTable.selectAll();
    }
    /**
     * Selects all numerical rows.
     */
    protected void selectAllNumericalRows() {
        final Collection<Integer> idxs =
            m_aggrColTableModel.getNumericalRowIdxs();
        updateSelection(idxs);
    }

    /**
     * Selects all none numerical rows.
     */
    protected void selectAllNoneNumericalRows() {
        final Collection<Integer> idxs =
            m_aggrColTableModel.getNoneNumericalRowIdxs();
        updateSelection(idxs);
    }

    /**
     * @return <code>true</code> if at least one row is selected
     */
    protected boolean rowsSelected() {
         final int[] selectedRows = m_aggrColTable.getSelectedRows();
         return (selectedRows != null && selectedRows.length > 0);
    }


    /**
     * @return the number of aggregation columns
     */
    protected int getAggregationColumnCount() {
        return m_aggrColTable.getRowCount();
    }

    /**
     * @param idxs the indices to select
     */
    private void updateSelection(final Collection<Integer> idxs) {
        if (idxs == null || idxs.isEmpty()) {
            m_aggrColTable.clearSelection();
            return;
        }
        boolean first = true;
        for (final Integer idx : idxs) {
            if (idx.intValue() < 0) {
                continue;
            }
            if (first) {
                first = false;
                m_aggrColTable.setRowSelectionInterval(idx.intValue(),
                        idx.intValue());
            } else {
                m_aggrColTable.addRowSelectionInterval(idx.intValue(),
                        idx.intValue());
            }
        }
    }

    /**
     * @return the panel in which all sub-components of this component are
     *         arranged. This panel can be added to the dialog pane.
     */
    public JPanel getComponentPanel() {
        return m_panel;
    }

    /**
     * @param excludeCols the name of all columns that should be excluded from
     * the aggregation panel
     */
    public void excludeColsChange(final Collection<String> excludeCols) {
        final Set<String> excludeColSet =
            new HashSet<String>(excludeCols);
        //update the available aggregation column list
        m_avAggrColListModel.removeAllElements();
        for (final DataColumnSpec colSpec : m_avAggrColSpecs) {
            if (!excludeColSet.contains(colSpec.getName())) {
                m_avAggrColListModel.addElement(colSpec);
            }
        }
        //remove all columns to be excluded from the aggregation column table
        m_aggrColTableModel.removeColumns(excludeCols);
    }

    /**
     * @param settings the settings object to write to
     */
    public void saveSettingsTo(final NodeSettingsWO settings) {
        ColumnAggregator.saveColumnAggregators(settings,
        m_aggrColTableModel.getColumnAggregators());
    }

    /**
     * @param settings the settings to read from
     * @param spec initializes the component
     * @throws InvalidSettingsException if the settings are invalid
     */
    public void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec spec)
    throws InvalidSettingsException {
        initialize(spec, ColumnAggregator.loadColumnAggregators(settings));
    }

    /**
     * Initializes the panel.
     * @param spec the {@link DataTableSpec} of the input table
     * @param colAggrs the {@link List} of {@link ColumnAggregator}s that are
     * initially used
     */
    public void initialize(final DataTableSpec spec,
            final List<ColumnAggregator> colAggrs) {
        m_avAggrColSpecs.clear();
        for (final DataColumnSpec colSpec : spec) {
            m_avAggrColSpecs.add(colSpec);
        }
      //remove all invalid column aggregator
        final List<ColumnAggregator> colAggrs2Use =
            new ArrayList<ColumnAggregator>(colAggrs.size());
        for (final ColumnAggregator colAggr : colAggrs) {
            final DataColumnSpec colSpec =
                spec.getColumnSpec(colAggr.getColName());
            if (colSpec != null
                    && colSpec.getType().equals(colAggr.getDataType())) {
                colAggrs2Use.add(colAggr);
            }
        }
        m_aggrColTableModel.initialize(colAggrs2Use);
    }

    /**
     * @return a label list of all supported methods for the currently
     * selected rows
     */
    protected List<String> getMethods4SelectedItems() {
        final int[] selectedColumns =
            m_aggrColTable.getSelectedRows();
        final Set<DataType> types =
            new HashSet<DataType>(selectedColumns.length);
        for (final int row : selectedColumns) {
            final ColumnAggregator aggregator =
                m_aggrColTableModel.getColumnAggregator(row);
            types.add(aggregator.getDataType());
        }
        final DataType superType = CollectionCellFactory.getElementType(
                types.toArray(new DataType[0]));
        final List<String> labels =
            AggregationMethods.getCompatibleMethodLabels(superType);
        return labels;
    }
}
