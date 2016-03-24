package chat;


public interface ClientModelUpdate {

    void isConnected(boolean status);

    void newMessage(String msg);

    void updateUserList();

    String enterNickname();
}
