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
 *   Jun 23, 2021 (bjoern): created
 */
package org.knime.filehandling.core.util;

import java.util.Optional;

import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.NodeContext;
import org.knime.core.node.workflow.WorkflowContext;
import org.knime.core.node.workflow.contextv2.HubSpaceLocationInfo;
import org.knime.core.node.workflow.contextv2.JobExecutorInfo;
import org.knime.core.node.workflow.contextv2.WorkflowContextV2;
import org.knime.core.node.workflow.contextv2.WorkflowContextV2.LocationType;
import org.knime.core.ui.node.workflow.WorkflowContextUI;

/**
 * Utility class that allows to reason about the current {@link WorkflowContext}.
 *
 * @author Bjoern Lohrmann, KNIME GmbH
 */
@SuppressWarnings("restriction")
public final class WorkflowContextUtil {

    private WorkflowContextUtil() {
    }

    /**
     * Retrieves the {@link WorkflowContext} via the {@link NodeContext} (if a node context is set). It is recommended
     * to first check for the existence of the workflow context ({@link #hasWorkflowContext()} or use
     * {@link #getWorkflowContextOptional()} instead.
     *
     * @return current {@link WorkflowContext} from the {@link NodeContext}
     * @throws IllegalStateException if no {@link WorkflowContext} is available (see
     *             {@link #getWorkflowContextOptional()} for possible reasons)
     */
    public static WorkflowContext getWorkflowContext() {
        return getWorkflowContextV2().toLegacyWorkflowContext();
    }

    /**
     * Retrieves the {@link WorkflowContextV2} via the {@link NodeContext} (if a node context is set). It is recommended
     * to first check for the existence of the workflow context ({@link #hasWorkflowContext()}.
     *
     * @return current {@link WorkflowContextV2} from the {@link NodeContext}
     * @throws IllegalStateException if no {@link WorkflowContextV2} is available
     */
    public static WorkflowContextV2 getWorkflowContextV2() {
        final var optWorkflowContext = getWorkflowContextV2Optional();
        CheckUtils.checkState(optWorkflowContext.isPresent(), "Workflow context required.");
        return optWorkflowContext.get(); // NOSONAR
    }

    /**
     * Returns the {@link WorkflowContext} or an empty optional if no workflow context is available. The lack of a
     * workflow context can have multiple reasons:
     * <ul>
     * <li>node {@link NodeContext} is set</li>
     * <li>no workflow manager instance is available (e.g. because the workflow is opened in the remote workflow
     * editor)</li>
     * <li>there is no workflow context set for the workflow (most likely an implementation error)</li>
     * </ul>
     *
     * @return the {@link WorkflowContext} or an empty optional if no workflow context is available
     */
    public static Optional<WorkflowContext> getWorkflowContextOptional() {
        return getWorkflowContextV2Optional().map(WorkflowContextV2::toLegacyWorkflowContext);
    }


    /**
     * Returns the {@link WorkflowContextV2} or an empty optional if no workflow context is available. The lack of a
     * workflow context can have multiple reasons:
     * <ul>
     * <li>node {@link NodeContext} is set</li>
     * <li>no workflow manager instance is available (e.g. because the workflow is opened in the remote workflow
     * editor)</li>
     * <li>there is no workflow context set for the workflow (most likely an implementation error)</li>
     * </ul>
     *
     * @return the {@link WorkflowContextV2} or an empty optional if no workflow context is available
     * @since 4.7
     */
    public static Optional<WorkflowContextV2> getWorkflowContextV2Optional() {
        return WorkflowContextUI.getWorkflowContextFromNodeContext();
    }

    /**
     * @return true, when we currently have a workflow context, false otherwise.
     */
    public static boolean hasWorkflowContext() {
        return getWorkflowContextV2Optional().isPresent();
    }

    /**
     * Validates if the given context is running in a server context.
     *
     * @param context workflow context to validate
     * @return {@code true} if the given context is a server context
     */
    public static boolean isServerContext(final WorkflowContext context) {
        return context.getRemoteRepositoryAddress().isPresent() && context.getServerAuthenticator().isPresent();
    }

    /**
     * Checks whether the calling thread is running in a KNIME node as part of a workflow running on KNIME Server.
     *
     * @return true if the current {@link WorkflowContext} belongs to a workflow running on KNIME Server, false
     *         otherwise.
     * @throws IllegalStateException If there is no current {@link NodeContext} or {@link WorkflowContext}.
     */
    public static boolean isServerContext() {
        return isServerContext(getWorkflowContext());
    }

    /**
     * Checks whether the currently executing workflow is running on KNIME Server, and whether the given mountID belongs
     * to its remote workflow repository.
     *
     * @param mountID The mount ID to check.
     * @return true, if the currently executing workflow is running on KNIME Server and whether the given mountID
     *         belongs to its remote workflow repository; false otherwise.
     */
    public static boolean isServerWorkflowConnectingToRemoteRepository(final String mountID) {
        return hasWorkflowContext() && //
            isServerContext() && //
            getWorkflowContext() //
                .getRemoteMountId() //
                .orElseThrow(
                    () -> new IllegalStateException("Workflow context on Server does not contain remote mount ID")) //
                .equals(mountID);
    }

    /**
     * Checks whether the currently executing workflow resides on Hub, and whether the given mountID is the default
     * mount ID of the Hub instance.
     *
     * @param mountID The mount ID to check.
     * @return true if the currently executing workflow resides on Hub and if the given mountID is the default mount ID
     *         of the Hub instance; false otherwise.
     */
    public static boolean isHubWorkflowConnectingToRemoteRepository(final String mountID) {
        if (!getWorkflowContextV2Optional().isPresent()) {
            return false;
        }

        if (getWorkflowContextV2().getLocationType() != LocationType.HUB_SPACE) {
            return false;
        }

        var hubLocInfo = (HubSpaceLocationInfo)getWorkflowContextV2().getLocationInfo();
        return hubLocInfo.getDefaultMountId().equals(mountID);
    }

    /**
     * Checks whether the calling thread is running in a KNIME node as part of a workflow located on the Hub.
     *
     * @return true if the current {@link WorkflowContext} belongs to a workflow located on the Hub, false
     *         otherwise.
     */
    public static boolean isCurrentWorkflowOnHub() {
        return hasWorkflowContext() && getWorkflowContextV2().getLocationType() == LocationType.HUB_SPACE;
    }

    /**
     * The remote workflow editor allows to open a workflow job in AP, while the job is executed in a Server or Hub
     * executor. For that workflow job, we then have two workflow contexts: One in AP where this field is true, and one
     * in the Server/Hub executor, where this field is false.
     * @return true if remote job view otherwise false
     */
    public static boolean isRemoteJobView() {
        return getWorkflowContextV2Optional().map(WorkflowContextV2::getExecutorInfo)
                .filter(JobExecutorInfo.class::isInstance)
                .map(JobExecutorInfo.class::cast)
                .map(JobExecutorInfo::isRemote).orElse(false);
    }

}
