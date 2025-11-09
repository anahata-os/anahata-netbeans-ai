package uno.anahata.nb.ai.model.ide;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutputTabInfo {
    private long id;
    private String displayName;
    private int contentSize;
    private int totalLines;
    private boolean isRunning;
}
