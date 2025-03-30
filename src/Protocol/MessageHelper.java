package Protocol;

public class MessageHelper {

    public static String createMessage(String ip, int port, String message, String... arguments) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append(ip).append(":").append(port)
                .append(" ").append(1)
                .append(" ").append(message);
        for (String arg : arguments) {
            msgBuilder.append(" ").append(arg);
        }

        return msgBuilder.toString();

    }

}
