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
 *   15.03.2016 (adrian): created
 */
package org.knime.base.node.meta.feature.selection;

import org.knime.base.node.meta.feature.selection.FeatureSelectionStrategies.Strategy;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataTypeColumnFilter;

/**
 *
 * @author Adrian Nembach, KNIME.com
 */
public class FeatureSelectionLoopStartSettings {
    private static final String CFG_CONSTANT_COLUMNS_FILTER_CONFIG = "constantColumnsFilterConfig";

    private static final String CFG_NR_FEATURES_THRESHOLD = "nrFeatureThreshold";

    private static final Strategy DEF_SELECTION_STRATEGY = Strategy.ForwardFeatureSelection;

    // -1 stands for no threshold!
    private static final int DEF_NR_FEATURES_THRESHOLD = -1;


    private Strategy m_selectionStrategy = DEF_SELECTION_STRATEGY;

    private int m_nrFeaturesThreshold = DEF_NR_FEATURES_THRESHOLD;

    private DataColumnSpecFilterConfiguration m_constantColumnsFilterConfig = new DataColumnSpecFilterConfiguration(
        CFG_CONSTANT_COLUMNS_FILTER_CONFIG, new DataTypeColumnFilter(DataValue.class));

    /**
     * This method should be called before the getNrFeaturesThreshold() method
     *
     * @return true if a threshold for the number of features is set
     */
    public boolean useNrFeaturesThreshold() {
        return m_nrFeaturesThreshold >= 0;
    }

    /**
     *
     * @return the currently set threshold for the number of features (-1 if no threshold is set)
     */
    public int getNrFeaturesThreshold() {
        return m_nrFeaturesThreshold;
    }

    /**
     * @param nrFeaturesThreshold the threshold that should be set
     */
    public void setNrFeaturesThreshold(final int nrFeaturesThreshold) {
        m_nrFeaturesThreshold = nrFeaturesThreshold;
    }


    /**
     * @return the {@link DataColumnSpecFilterConfiguration} for the static column filter
     */
    public DataColumnSpecFilterConfiguration getStaticColumnsFilterConfiguration() {
        return m_constantColumnsFilterConfig;
    }


    /**
     * @return the name of the feature selection strategy
     */
    public Strategy getSelectionStrategy() {
        return m_selectionStrategy;
    }

    /**
     * @param selectionStrategy the name of the feature selection strategy
     */
    public void setSelectionStrategy(final Strategy selectionStrategy) {
        m_selectionStrategy = selectionStrategy;
    }

    /**
     * Saves the settings in the <b>settings</b> object
     *
     * @param settings
     */
    public void save(final NodeSettingsWO settings) {
        m_selectionStrategy.save(settings);
        m_constantColumnsFilterConfig.saveConfiguration(settings);
        settings.addInt(CFG_NR_FEATURES_THRESHOLD, m_nrFeaturesThreshold);
    }

    /**
     * Loads settings in the dialog
     *
     * @param settings the settings to load from
     * @param spec the input {@link DataTableSpec}
     */
    public void loadInDialog(final NodeSettingsRO settings, final DataTableSpec spec) {
        try {
            m_selectionStrategy = Strategy.load(settings);
        } catch (Exception e) {
            m_selectionStrategy = DEF_SELECTION_STRATEGY;
        }
        m_constantColumnsFilterConfig.loadConfigurationInDialog(settings, spec);
        m_nrFeaturesThreshold = settings.getInt(CFG_NR_FEATURES_THRESHOLD, DEF_NR_FEATURES_THRESHOLD);
    }

    /**
     * Loads settings in model
     *
     * @param settings
     * @throws InvalidSettingsException
     */
    public void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        try {
            m_selectionStrategy = Strategy.load(settings);
        } catch (Exception e) {
            m_selectionStrategy = DEF_SELECTION_STRATEGY;
        }
        m_constantColumnsFilterConfig.loadConfigurationInModel(settings);
        m_nrFeaturesThreshold = settings.getInt(CFG_NR_FEATURES_THRESHOLD);
    }
}
