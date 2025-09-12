package de.luisagrether.idisguise.api;

/**
 * This exception is thrown when an attempt to disguise or undisguise a player via the <code>DisguiseAPI</code> fails because the fired event is cancelled by another plugin.
 * 
 * @since 6.0.1
 * @author LuisaGrether
 */
public class EventCancelledException extends RuntimeException {
    
    public EventCancelledException() {
        super();
    }

    public EventCancelledException(String message) {
        super(message);
    }

}
