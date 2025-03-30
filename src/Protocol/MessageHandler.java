package Protocol;

import Peer.Peer;
import Peer.PeerInfo;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    public static String handleSendMessage(String message, PeerInfo neighbor) {
        try (Socket socket = new Socket(neighbor.getIp(), neighbor.getPort())) {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeBytes(String.format("%s%n", message));
            BufferedReader serverBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            neighbor.setStatus("ONLINE");
            return serverBufferedReader.readLine();
        } catch (Exception e) {
            neighbor.setStatus("OFFLINE");
            log.log("Falha ao enviar mensagem para %s", neighbor);
            log.log(" === ERROR!!! === %n%s", e.getMessage());
            throw new RuntimeException();
        }
    }

    public static void handleReceiveMessage(Peer peer, Socket clientSocket) {
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
                        peer.addNeighbor(source);
                        handleAnswerMessage(clientSocket,"");
                        break;
                    case "GET_PEERS":
                        peer.listarPeersConhecidos(clientSocket, source);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handleAnswerMessage(Socket clientSocket, String answer) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeBytes(String.format("%s%n", answer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
