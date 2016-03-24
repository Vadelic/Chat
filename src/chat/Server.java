package chat;

import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static chat.MessageType.*;

public class Server {
    private static final int PORT = 500;
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread {
        private Socket socket;

        private Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String nameConnect = null;
            try (Connection connection = new Connection(socket)) {
                System.out.println("Start new connection with " + socket.getRemoteSocketAddress());

                nameConnect = serverHandshake(connection);

                sendMessageForAll(null, new Message(USER_ADDED, nameConnect));
                sendListOfUsers(connection, nameConnect);
                serverMainLoop(connection, nameConnect);
            } catch (Exception e) {
                System.out.println("error when communicating with a remote address");

            } finally {
                //при разрыве связи с соединением удаляем его из списка соединений  и уведомляем об этом остальных.
                if (nameConnect != null) {
                    connectionMap.remove(nameConnect);
                    sendMessageForAll(null, new Message(USER_REMOVED, nameConnect));
                }
                System.out.println("chat.Connection with " + socket.getRemoteSocketAddress() + "is closed");
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            Message answer;
            do {
                connection.send(new Message(NAME_REQUEST));
                answer = connection.receive();
                if (answer.getTYPE() == USER_NAME &&
                        answer.getDATA() != null &&
                        !connectionMap.containsKey(answer.getDATA()))
                {
                    connectionMap.put(answer.getDATA(), connection);
                    connection.send(new Message(NAME_ACCEPTED));
                    break;
                }
            } while (true);

            return answer.getDATA();
        }

        /**
         * Метод уведомляет всех участников о новом соединении.
         *
         * @param connection новое соединение
         * @param userName   имя нового соединения
         */
        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (String user : connectionMap.keySet()) {
                if (!user.equals(userName)) {
                    connection.send(new Message(USER_ADDED, user));
                }
            }
        }

        /**
         * Метод хэндлера, который в цикле проверяет наличие новых сообщений от соединения и
         * реагирует на них в зависимости от типа.
         *
         * @param connection новое соединение
         * @param sender     имя нового соединения
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private void serverMainLoop(Connection connection, String sender) throws IOException, ClassNotFoundException {
            while (true) {
                Message msg = connection.receive();
                String textMsg = sender + ": " + msg.getDATA();

                switch (msg.getTYPE()) {
                    case TEXT:
                        Server.sendMessageForAll(null, new Message(TEXT, textMsg));
                        break;
                    case TEXT_PRIVATE:
                        Server.sendMessageForAll(sender, new Message(TEXT_PRIVATE, textMsg, msg.getTARGET()));

                        break;
                    default:
                        System.out.println("Incorrect message");
                }
            }
        }
    }

    /**
     * Получение внешнего адреса на котором запущен сервер. Адрес получаем путём парсинга сайта myip.by
     * @return внешний IP
     */
    private static String getCurrentIP() {
        String result = null;
        URL url = null;
        try {
            url = new URL("http://myip.by/");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                char[] buff = new char[1024];
                StringBuilder allText = new StringBuilder();
                int count = 0;
                while ((count = reader.read(buff)) != -1) {
                    allText.append(buff, 0, count);
                }
                //парсим страничку где написан наш IP
                String stringForLoocking = String.valueOf("<div id=\"ip\" class=\"bodytext headline\">");
                int tagOpen = allText.indexOf(stringForLoocking);
                int tagClose = allText.indexOf("</div>", tagOpen + stringForLoocking.length() + 1);
                result = (allText.substring(tagOpen + stringForLoocking.length(), tagClose));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
//        system.out.println("Enter port:");
//        int port = ConsoleHelper.readInt();

        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("chat.Server is started...");
            System.out.println("Server address: " + getCurrentIP() + ":" + PORT);

            while (true) {
                Socket socket = ss.accept();
                new Handler(socket).start();
            }

        } catch (IOException e) {
            System.out.println("chat.Server error.");
        }
    }

    /**
     * Сервер отправляет сообщение всем пользователям или конкретному пользователю, если в сообщении указан адресат.
     * Если сообщение message приватного типа, то неоходимо указать и отправителя, что бы сообщение ушло по 2м адресам.
     *
     * @param sender отправитель
     * @param message Пересылаемое сообщение.
     */
    public static void sendMessageForAll(@Nullable String sender, Message message) {
        String addressee = message.getTARGET();
        try {
            if (addressee != null && sender != null) {

                if (connectionMap.containsKey(addressee)) {

                    connectionMap.get(addressee).send(new Message(TEXT_PRIVATE,
                            "Private from " +message.getDATA()));

                    connectionMap.get(sender).send(new Message(TEXT,
                            message.getDATA().replace(sender + ":", sender + " -> " + addressee + ":")));
                }
                else {
                    connectionMap.get(sender).send(new Message(TEXT,
                            message.getDATA().replace(sender + ":", "(err) " + sender + " -> " + addressee + ":")));
                }

            } else {
                for (Map.Entry<String, Connection> user : connectionMap.entrySet()) {
                    addressee = user.getKey();
                    user.getValue().send(message);
                }
            }
        } catch (IOException e) {
            System.out.println("chat.Message for " + addressee + " is not send");
        }
    }
}
