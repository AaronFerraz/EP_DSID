package Protocol;

import Peer.PeerInfo;
import logger.Logger;
import logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    public static void handleSendMessage(String message, PeerInfo neighbor) {
        try (Socket socket = new Socket(neighbor.getIp(), neighbor.getPort());
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println(message);
            neighbor.setStatus("ONLINE");
        } catch (IOException e) {
            neighbor.setStatus("OFFLINE");
            log.log("Falha ao enviar mensagem para %s", neighbor);
            log.log(" === ERROR!!! === %n%s", e.getMessage());
        }
    }

    public static void handleReceiveMessage(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String messageReceived;
            while ((messageReceived = reader.readLine()) != null) {
                log.log("Mensagem recebida: %s", messageReceived);

                String[] msgSplit = messageReceived.split(" ", 4);

                String source = msgSplit[0];
                String clock = msgSplit[1];
                String type = msgSplit[2];

                String args = msgSplit.length == 4 ? msgSplit[3] : "";

                switch (type) {
                    case "HELLO":
                        MessageHelper.helloMessage(source, args);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
