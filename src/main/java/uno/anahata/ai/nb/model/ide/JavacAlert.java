/* Licensed under the Apache License, Version 2.0 */
package uno.anahata.ai.nb.model.ide;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JavacAlert {
    private final String filePath;
    private final String kind;
    private final int lineNumber;
    private final int columnNumber;
    private final String message;
}