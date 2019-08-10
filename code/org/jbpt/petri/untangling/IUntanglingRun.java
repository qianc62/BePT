package org.jbpt.petri.untangling;

import org.jbpt.petri.*;

public interface IUntanglingRun<F extends IFlow<N>, N extends INode, P extends IPlace, T extends ITransition, M extends IMarking<F, N, P, T>>
        extends IRun<F, N, P, T, M> {

    public boolean isSignificant();

    public IUntanglingRun<F, N, P, T, M> clone();

}
