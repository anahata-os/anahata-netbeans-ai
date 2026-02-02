/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.java2;

import lombok.Getter;

/**
 * A rich result object that represents the outcome of a Javadoc retrieval operation for a JavaMember.
 * It extends JavaTypeDocs to maintain consistency in the polymorphic API.
 */
@Getter
public class JavaMemberDocs extends JavaTypeDocs {

    /**
     * Constructs a new JavaMemberDocs and attempts to retrieve the Javadoc for the given JavaMember.
     * @param member the member to retrieve Javadoc for.
     * @throws Exception if the Javadoc cannot be retrieved.
     */
    public JavaMemberDocs(JavaMember member) throws Exception {
        super(member);
    }
    
    public JavaMember getMember() {
        return (JavaMember) javaType;
    }
}
