package uno.anahata.nb.ai.functions.spi.pojos;

/**
 * A POJO containing detailed information about a single open TopComponent (window) in the NetBeans IDE.
 */
public record TopComponentInfo(
    String id,
    String name,
    boolean selected,
    String displayName,
    String htmlDisplayName,
    String tooltip,
    String className,
    String mode,
    String activatedNodes,
    String supportedActions,
    String filePath,
    String primaryFilePath,
    long sizeInBytes
) {}
