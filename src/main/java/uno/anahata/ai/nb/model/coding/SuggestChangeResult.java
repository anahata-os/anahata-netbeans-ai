/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.coding;

import uno.anahata.ai.tools.spi.pojos.FileInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uno.anahata.ai.context.stateful.StatefulResource;
import uno.anahata.ai.tools.UserFeedback;

/**
 * Represents the result of a proposeChange operation, indicating whether the user
 * accepted or cancelled the change, and including the updated file information if accepted.
 *
 * @author Anahata
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents the result of a suggestChange operation, indicating:"
        + "- The user's approval (Accepted / Cancelled), "
        + "- Any message from the user regarding the proposed change (regardless of whether the user approved it or not)  "
        + "- The updated file details (content and metadata) if accepted.")
public class SuggestChangeResult implements StatefulResource, UserFeedback {

    public enum Status {
        /** The user accepted the proposed file change. */
        ACCEPTED,
        /** The user cancelled the operation. */
        CANCELLED,
        /** An error occurred during the operation. */
        ERROR
    }

    /** The outcome of the operation. */
    @Schema(description = "The final outcome of the operation, indicating whether the user accepted or cancelled the change.", required = true)
    private Status status;

    /** A descriptive userMessage from the user. */
    @Schema(description = "A message from the user when reviewing the diff", required = true)
    private String userMessage;

    /** The updated file information, only present if the status is ACCEPTED. */
    @Schema(description = "The updated file information, which is present only if the status is 'ACCEPTED'.")
    private FileInfo fileInfo;

    /**
     * Gets the resource ID from the nested FileInfo object.
     * This allows the ContextManager to treat this result as a stateful resource
     * only when a file was actually modified.
     *
     * @return The file path if the change was accepted, otherwise null.
     */
    @Override
    public String getResourceId() {
        return (fileInfo != null) ? fileInfo.getResourceId() : null;
    }

    @Override
    public long getLastModified() {
        return (fileInfo != null) ? fileInfo.getLastModified() : 0;
    }

    @Override
    public long getSize() {
        return (fileInfo != null) ? fileInfo.getSize() : 0;
    }

    @Override
    public String getUserFeedback() {
        if (status == Status.CANCELLED) {
            String prefix = "Cancelled, no changes have been written to disk.";
            return StringUtils.isBlank(userMessage) ? prefix : prefix + " " + userMessage;
        }
        return userMessage;
    }

    @Override
    public String toString() {
        return "ProposeChangeResult{" +
                "status=" + status +
                ", message='" + userMessage + '\'' +
                ", fileInfo=" + (fileInfo != null ? fileInfo.toString() : "null") +
                '}';
    }
}
