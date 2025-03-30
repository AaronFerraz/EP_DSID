import Peer.Peer;

import java.util.Scanner;

public class Eachare {
    public static void main(String[] args) {
        String ip,portString;
        int port;

        String address = "127.0.0.1:54644";
        String nomeArquivo = "files/peer1.txt";
        String dirCompartilhado = "files/peer1/";

        if (args.length == 3) {
            address = args[0];
            nomeArquivo = args[1];
            dirCompartilhado = args[2];
        }

        if (address.split(":").length != 2) {
            throw new RuntimeException("Endereço deve seguir o padrão ip:porta");
        }
        ip = address.split(":")[0];
        portString = address.split(":")[1];

        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            throw new RuntimeException("A porta deve ser um número inteiro maior que zero.");
        } catch (Exception e) {
            throw new RuntimeException("Parabens");
        }

        Peer peer = new Peer(ip, port, nomeArquivo, dirCompartilhado);

        new Thread(peer).start();

        Scanner sc = new Scanner(System.in);
        int opcao;

        do{
            System.out.println("Escolha um comando: ");
            System.out.println(
                        "\t\t[1] Listar peers"
                    + "\n\t\t[2] Obter peers"
                    + "\n\t\t[3] Listar arquivos locais"
                    + "\n\t\t[4] Buscar arquivos"
                    + "\n\t\t[5] Exibir estatisticas"
                    + "\n\t\t[6] Alterar tamanho de chunk"
                    + "\n\t\t[9] Sair");
            System.out.print("> ");
            opcao = sc.nextInt();

            switch (opcao){
                case 1:
                    int listarPeersResult = peer.listarPeers();
                    if (listarPeersResult > 0) {
                        peer.handleSendMessage(listarPeersResult, "HELLO");
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 9:
                    opcao = 0;
                    break;
                default:
                    System.out.println("Não existe essa opção");
            }
        }while(opcao != 0);
    }
}
