package chat;

import java.util.Set;

/**
 * Контроль и представление реализованы в одном классе.
 */
public class ClientController {
    private ClientModelUpdate view;

    private ClientModel model;


    public static void main(String[] args) {
        new ClientController();
    }

    public ClientController() {
        view = new ClientViewConsole(this);
        model = new ClientModel(view);
        model.runClient();

    }



    public void newMsgRead(String msg) {
        model.sendTextMessage(msg);
    }


    public Set<String> getAllUsers() {
        return model.getUsers();
    }
    public boolean checkStatus() {
        return model.isClientConnected();
    }

}
