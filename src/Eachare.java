import Peer.Peer;

import java.util.Scanner;

public class Eachare {
    public static void main(String[] args) {
        String address = "31231";
        int porta = 3131;

        Scanner sc = new Scanner(System.in);
        int opcao = 0;

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

//            Peer peer = new Peer();

            switch (opcao){
                case 1:
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
