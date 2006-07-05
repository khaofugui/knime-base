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
 *   29.06.2005 (ohl): created
 */
package de.unikn.knime.base.node.filter.row.rowfilter;

import de.unikn.knime.core.data.DataRow;
import de.unikn.knime.core.data.DataTableSpec;
import de.unikn.knime.core.node.InvalidSettingsException;
import de.unikn.knime.core.node.NodeSettings;

/**
 * A row filter for the row filter data table ANDing two other row filters.
 * 
 * @author ohl, University of Konstanz
 */
public class AndRowFilter extends RowFilter {

    private static final String CFG_FILTER1 = "ConfigFilter1";

    private static final String CFG_FILTER2 = "ConfigFilter2";

    private RowFilter m_in1;

    private RowFilter m_in2;

    // flags indicating a "IncludeFromNowOn" from the corresp. filter.
    private boolean m_in1True;

    private boolean m_in2True;

    /**
     * Implements a RowFilter that takes two other RowFilters and combines their
     * results with a logical AND. If filter in1 returns a no match the
     * matches() method of filter in2 will not be invoked!
     * 
     * @param in1 RowFilter as first input into the AND result
     * @param in2 RowFilter for the second input of the AND result. Might be
     *            short cutted.
     */
    public AndRowFilter(final RowFilter in1, final RowFilter in2) {
        if (in1 == null) {
            throw new NullPointerException("RowFilter in1 must not be null");
        }
        if (in2 == null) {
            throw new NullPointerException("RowFilter in2 must not be null");
        }
        m_in1 = in1;
        m_in2 = in2;
        m_in1True = false;
        m_in2True = false;
    }

    /**
     * The filter created by this constructor cannot be used without loading
     * settings through a config object.
     */
    public AndRowFilter() {
        m_in1 = null;
        m_in2 = null;
        m_in1True = false;
        m_in2True = false;
    }

    /**
     * @return the row filter connected to one of the inputs of the logical AND.
     *         Returns the one that is not short cutted.
     */
    public RowFilter getInput1() {
        return m_in1;
    }

    /**
     * @return the row filter connected to one of the inputs of the logical AND.
     *         Returns the one that could be short cutted.
     */
    public RowFilter getInput2() {
        return m_in2;
    }

    /**
     * @see RowFilter#matches(DataRow, int)
     */
    public boolean matches(final DataRow row, final int rowIndex)
            throws EndOfTableException, IncludeFromNowOn {

        /*
         * note: if one of these filters throws an EOTexception, we can let that
         * go through. If that filter will not match any rows anymore, we won't
         * either.
         */

        boolean result1 = true;
        boolean result2 = true;

        if (!m_in1True) {
            try {
                result1 = m_in1.matches(row, rowIndex);
            } catch (IncludeFromNowOn ifno) {
                m_in1True = true;
                if (m_in2True) {
                    // now both inputs are always true - so are we.
                    throw new IncludeFromNowOn();
                }
            }
        }
        if (!m_in2True) {
            try {
                result2 = m_in2.matches(row, rowIndex);
            } catch (IncludeFromNowOn ifno) {
                m_in2True = true;
                if (m_in1True) {
                    // now both inputs are always true - so are we.
                    throw new IncludeFromNowOn();
                }
            }
        }
        return result1 & result2;
    }

    /**
     * @see RowFilter#loadSettingsFrom(NodeSettings)
     */
    public void loadSettingsFrom(final NodeSettings cfg)
            throws InvalidSettingsException {

        NodeSettings cfg1 = cfg.getConfig(CFG_FILTER1);
        NodeSettings cfg2 = cfg.getConfig(CFG_FILTER2);

        m_in1 = RowFilterFactory.createRowFilter(cfg1);
        m_in2 = RowFilterFactory.createRowFilter(cfg2);
    }

    /**
     * @see RowFilter#saveSettings(NodeSettings)
     */
    protected void saveSettings(final NodeSettings cfg) {
        if (m_in1 != null) {
            NodeSettings cfg1 = cfg.addConfig(CFG_FILTER1);
            m_in1.saveSettingsTo(cfg1);
        }
        if (m_in2 != null) {
            NodeSettings cfg2 = cfg.addConfig(CFG_FILTER2);
            m_in2.saveSettingsTo(cfg2);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "AND-Filter:\nINPUT1: " + m_in1.toString() + "\nINPUT2: "
                + m_in2.toString();
    }

    /**
     * @see de.unikn.knime.base.node.filter.row.rowfilter.RowFilter
     *      #configure(de.unikn.knime.core.data.DataTableSpec)
     */
    public DataTableSpec configure(final DataTableSpec inSpec)
            throws InvalidSettingsException {
        DataTableSpec spec1 = null;
        DataTableSpec spec2 = null;
        
        if (m_in1 != null) {
            spec1 = m_in1.configure(inSpec);
        } else {
            throw new InvalidSettingsException(
                    "AND-rowfilter: no input filter set");
        }
        if (m_in2 != null) {
            spec2 = m_in2.configure(inSpec);
        } else {
            throw new InvalidSettingsException(
            "AND-rowfilter: no input filter set");
        }
        if ((spec1 != null) || (spec2 != null)) {
            // TODO: how in the world do we AND two specs?!?
            return null;
        }
        return null;
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        AndRowFilter arf = (AndRowFilter)super.clone();
        if (m_in1 != null) {
            arf.m_in1 = (RowFilter)m_in1.clone();
        }
        if (m_in2 != null) {
            arf.m_in2 = (RowFilter)m_in2.clone();
        }
        arf.m_in1True = false;
        arf.m_in2True = false;
        return arf;
    }
}
