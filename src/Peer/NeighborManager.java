package Peer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class NeighborManager {
    private String path;

    public NeighborManager(String filePath){
        this.path = filePath;
    }

    public static HashMap<String, String> createListNeighbors(String filePath){
        HashMap<String, String> listNeibors = new HashMap();

        try{
            BufferedReader rd = new BufferedReader(new FileReader(filePath));
            String line;
            while((line = rd.readLine()) != null){
                String[] peer = line.split(":");
                listNeibors.put(peer[0], peer[1]);
            }
        } catch (IOException e){
            System.out.println("Erro ao ler o arquivo: " + e);
        }

        return listNeibors;
    }

    public void addNeighbor(String address, int port){

    }

    public void saveNeighborsToFile(){

    }

//    public static void main(String[] args) {
//        String caminho = "F:\\Faculdade-USP\\Semestre 5\\DSID\\EP\\EP1\\Peer1\\Vizinhos\\vizinhos.txt";
//        ArrayList<String> teste = createListNeighbors(caminho);
//        System.out.println(teste);
//
//    }

}
