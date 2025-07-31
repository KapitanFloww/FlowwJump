package com.github.kapitanfloww.jump.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@With
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Jump implements Serializable {

    @Serial
    private static final long serialVersionUID = 10001;

    private UUID id;

    // Unique name
    private String name;

    private JumpLocation start;

    private List<JumpLocation> checkpoints = new ArrayList<>();

    private JumpLocation finish;

    private JumpLocation reset;

    public void addCheckpoints(JumpLocation location) {
        checkpoints.add(location);
    }

    public boolean isSetupComplete() {
        return getStart() != null
                && getFinish() != null
                && getReset() != null
                && getName() != null;
    }
}
