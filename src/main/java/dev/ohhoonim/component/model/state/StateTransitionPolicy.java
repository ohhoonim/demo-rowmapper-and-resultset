package dev.ohhoonim.component.model.state;

// S : state 
// T : event (transition) 
// C : context (entity)
public interface StateTransitionPolicy<S extends Status<S, T, C>, T extends TransitionEvent<C>, C> {

    public default TransitionResult<S, C> transition(S status, T transitionEvent){
        return status.trigger(transitionEvent);
    }
}
