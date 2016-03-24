package chat;


import javax.swing.*;

public class ClientViewGui extends JFrame implements ClientModelUpdate {
    private ClientController controller;

    public ClientViewGui(ClientController controller) {
        this.controller = controller;
        this.initView();
    }
    public ClientViewGui() {

    }

    public static void main(String[] args) {
        new ClientViewGui();
    }


    public void initView() {
    }

    @Override
    public void isConnected(boolean status) {

    }

    @Override
    public void newMessage(String msg) {

    }

    @Override
    public void updateUserList() {
    }

    @Override
    public String enterNickname() {
        return null;
    }
}
