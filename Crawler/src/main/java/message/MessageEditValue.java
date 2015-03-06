package message;

/**
 * A Message is the object that the Actors (Threads) pass around to communicate with each other.
 * <p/>
 * Created by KrisMinkjan on 14-2-2015.
 */
public class MessageEditValue extends Message {
    public enum ValueType {
        DELAY
    }

    private final ValueType valueType;
    private final int value;

    public MessageEditValue(ValueType valueType, int value) {
        super(MessageType.EDIT_VALUE);
        this.valueType = valueType;
        this.value = value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public int getValue() {
        return value;
    }
}
