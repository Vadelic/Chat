package chat;

/**
 * Created by Вадим on 19.02.2016.
 */
public enum MessageType {
    NAME_REQUEST,       // – запрос имени
    USER_NAME,          // – имя пользователя
    NAME_ACCEPTED,      // – имя принято
    TEXT,               // – текстовое сообщение
    TEXT_PRIVATE,       // - приватное текстовое сообщение
    USER_ADDED,         // – пользователь добавлен
    USER_REMOVED        // – пользователь удален
}
