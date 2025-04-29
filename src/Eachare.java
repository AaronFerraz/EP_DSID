import Peer.Peer;
import Peer.PeerInfo;
import logger.Logger;
import logger.LoggerFactory;

import java.util.Scanner;

public class Eachare {

    private static final Logger log = LoggerFactory.getLogger(Eachare.class);

    public static void main(String[] args) {
        Peer peer = Peer.createAndStartPeer(args);

        Scanner sc = new Scanner(System.in);
        int opcao;

        do{
            log.log("Escolha um comando: ", true);
            log.log(
                        "\t\t[1] Listar peers"
                    + "\n\t\t[2] Obter peers"
                    + "\n\t\t[3] Listar arquivos locais"
                    + "\n\t\t[4] Buscar arquivos"
                    + "\n\t\t[5] Exibir estatisticas"
                    + "\n\t\t[6] Alterar tamanho de chunk"
                    + "\n\t\t[9] Sair", true);
            log.log("> ", false);
            opcao = sc.nextInt();

            switch (opcao){
                case 1:
                    PeerInfo listarPeersResult = peer.listarPeers();
                    peer.hello(listarPeersResult);
                    break;
                case 2:
                    peer.getPeers();
                    break;
                case 3:
                    peer.showDirectoryShared();
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 9:
                    peer.bye();
                    System.exit(0);
                    break;
                default:
                    log.log("Não existe essa opção", true);
                    break;
            }
        }while(opcao != 0);

        sc.close();
    }


}
