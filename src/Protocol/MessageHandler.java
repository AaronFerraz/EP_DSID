package Protocol;

import Peer.Peer;
import Peer.PeerInfo;
import logger.Logger;
import logger.LoggerFactory;

import java.io.*;
import java.net.Socket;
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
            log.logDebug(String.format("Falha ao enviar mensagem para %s", neighbor));
            log.logDebug(String.format(" === ERROR!!! === %n%s", e.getMessage()));

            return "";
        }
    }

    public static void handleReceiveMessage(Peer peer, Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String messageReceived;
            while ((messageReceived = reader.readLine()) != null) {
                log.log("Mensagem recebida: \"%s\"", messageReceived);

                String[] msgSplit = messageReceived.split(" ", 4);

                log.logDebug(Arrays.toString(msgSplit));

                String source = msgSplit[0];
                int clock = Integer.parseInt(msgSplit[1]);
                String type = msgSplit[2];

                String args = msgSplit.length == 4 ? msgSplit[3] : "";
                peer.incrementClock(clock);

                switch (type) {
                    case "HELLO":
                        peer.addNeighborByAddress(source, clock);
                        handleAnswerMessage(clientSocket,"", "");
                        break;
                    case "GET_PEERS":
                        peer.addNeighborByAddress(source, clock);
                        peer.listarPeersConhecidos(clientSocket, source);
                        break;
                    case "LS":
                        peer.addNeighborByAddress(source, clock);
                        peer.ls_list(clientSocket, source);
                        break;
                    case "DL":
                        peer.addNeighborByAddress(source, clock);
                        peer.dlFile(clientSocket, source, args);
                        break;
                    case "BYE":
                        peer.changeStatusPeer(source, clock);
                        handleAnswerMessage(clientSocket,"", "");
                        break;
                }
            }
        } catch (IOException e) {
            log.logDebug(e.getMessage());
        }
    }

    public static void handleAnswerMessage(Socket clientSocket, String answer, String source) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            dataOutputStream.writeBytes(String.format("%s%n", answer));
            if (!answer.isBlank())
                log.log("Encaminhando mensagem \"%s\" para %s", answer, source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
