package chat;



import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType TYPE;
    private final String DATA;
    private final String TARGET;

    public Message(MessageType TYPE, String DATA) {
        this.TYPE = TYPE;
        this.DATA = DATA;
        this.TARGET = null;
    }

    public Message(MessageType TYPE) {
        this.TYPE = TYPE;
        this.DATA = null;
        this.TARGET = null;
    }

    public Message(MessageType TYPE, String DATA, String TARGET) {
        this.TYPE = TYPE;
        this.DATA = DATA;
        this.TARGET = TARGET;
    }

    public MessageType getTYPE() {
        return TYPE;
    }

    public String getDATA() {
        return DATA;
    }

    public String getTARGET() {
        return TARGET;
    }
}
