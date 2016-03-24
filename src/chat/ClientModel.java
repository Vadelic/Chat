package chat;

import java.io.IOException;
import java.net.Socket;
import java.util.*;


public class ClientModel {
    private ClientModelUpdate view;
    private Connection connection;
    private volatile boolean clientConnected = false;
    private Set<String> users = new TreeSet<>();

    public ClientModel(ClientModelUpdate view) {
        this.view = view;
    }

    private class ListenerIncomingMessages extends Thread {
        public ListenerIncomingMessages() {
            this.setDaemon(true);
            this.start();
        }

        @Override
        public void run() {

            try {
                Message incomeMsg;
                while (true) {
                    incomeMsg = connection.receive();
                    switch (incomeMsg.getTYPE()) {
                        case TEXT_PRIVATE:
                        case TEXT:
                            view.newMessage(incomeMsg.getDATA());
                            break;
                        case USER_ADDED:
                            users.add(incomeMsg.getDATA());
                            view.newMessage(incomeMsg.getDATA() + " in chat");
                            view.updateUserList();
                            break;
                        case USER_REMOVED:
                            users.remove(incomeMsg.getDATA());
                            view.newMessage(incomeMsg.getDATA() + " has left the chat");
                            view.updateUserList();
                            break;
                    }
                }
            } catch (IOException e) {
                //TODO реализовать логгирование
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void runClient() {
        try {
            String host = "localhost";
            int port = 500;
            connection = new Connection(new Socket(host, port));
            clientHandshake();
        } catch (Exception e) {
            notifyConnectionStatusChanged(false);
            e.printStackTrace();

        }
    }

    private void clientHandshake() throws IOException, ClassNotFoundException {
        Message answer;
        boolean flag = true;
        while (flag) {
            answer = connection.receive();
            switch (answer.getTYPE()) {
                case NAME_REQUEST:
                    connection.send(new Message(MessageType.USER_NAME, view.enterNickname()));
                    break;
                case NAME_ACCEPTED:
                    new ListenerIncomingMessages();
                    notifyConnectionStatusChanged(true);
                    flag = false;
                    break;
                default:
                    throw new IOException("Unexpected chat.MessageType");
            }
        }
    }

    private void notifyConnectionStatusChanged(boolean status) {
        clientConnected = status;
        view.isConnected(status);
    }

    /**
     * Анализ введенного текста и создание сообщения, общего или приватного.
     *
     * @param text отправлемый текст. Может содержат команды
     */
    public void sendTextMessage(String text, String... target) {

        int indexPoint = text.indexOf("@");
        try {
            if (indexPoint != -1)
                connection.send(new Message(
                        MessageType.TEXT_PRIVATE,
                        text.substring(indexPoint + 1).trim(),
                        text.substring(0, indexPoint)
                ));
            else
                connection.send(new Message(
                        MessageType.TEXT,
                        text
                ));

        } catch (IOException e) {

            clientConnected = false;
            System.out.println("chat.Message is not send");
        }
    }

    /**
     * Возвращает список всех подключённых пользователей
     *
     * @return read-only коллекция
     */
    public Set<String> getUsers() {
        return Collections.unmodifiableSet(users);
    }

    public boolean isClientConnected() {
        return clientConnected;
    }
}