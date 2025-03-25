package Protocol;

import java.io.PrintWriter;
import java.net.Socket;

public class MessageHandler {
    private PrintWriter out;

    public MessageHandler(PrintWriter out){
        this.out = out;
    }

    public void sendMessage(String type){
//      "<ORIGEM> <CLOCK> <TIPO>[ ARGUMENTO1 ARGUMENTO2...] \n"
        String msg = String.format("<> <> <%s>[ ARGUMENTO1 ARGUMENTO2...] \n", type);
        out.println(msg);
    }

    public void handleIncomingMessage(String rawMessage, MessageHelper messageHelper){
        String[] fields = rawMessage.split(" ", 4);

        if(fields.length < 3){
            System.out.println("Mensamem inválida: " + rawMessage);
        }

        String origem = fields[0];
        int clock = Integer.parseInt(fields[1]);
        String tipo = fields[2];
        String argumentos = "";

        messageHelper.processMessage(origem, clock, tipo, argumentos);

    }

}
