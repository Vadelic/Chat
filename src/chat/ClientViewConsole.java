package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientViewConsole implements ClientModelUpdate {
    private ClientController controller;

    public ClientViewConsole(ClientController controller) {
        this.controller = controller;
//
    }

    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    private void commandClient(String command) {
        switch (command) {
            case "help":
                System.out.println("\t:::Chat system command:::\n" +
                        "\t\t#help - print all command\n" +
                        "\t\t#exit - exit from chat\n" +
                        "\t\t#users - print all users\n" +
                        "\n-for private msg: username@ type_message"
                );
                break;
            case "exit":
                //    clientConnected = false;
                break;
            case "users":
                System.out.println("User in chat:");
                for (String user : controller.getAllUsers()) {
                    System.out.println(user);
                }
                break;
        }
    }

    public static String readString() {
        while (true) {
            try {
                return bufferedReader.readLine();
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }

    public void mainLoopReader() {

        System.out.println("writer started");
        while (true) {
            String text = readString();
            if (text.startsWith("#")) {
                commandClient(text.substring(1));
            } else {
                controller.newMsgRead(text);
            }
        }
    }


    @Override
    public void isConnected(boolean status) {

        if (status) {
            mainLoopReader();
        } else {
            System.out.println("Connection failed");
        }
    }

    @Override
    public void newMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void updateUserList() {
        //в данном представлении это делается по запросу через контроллер
    }

    @Override
    public String enterNickname() {
        System.out.println("Enter your name:");
        return readString();
    }


}
