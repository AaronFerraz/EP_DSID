package Peer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class NeighborManager {
    private String path;

    public NeighborManager(String filePath){
        this.path = filePath;
    }

    public static ArrayList<String> createListNeighbors(String filePath){
        ArrayList<String> listNeibors = new ArrayList<>();

        try{
            BufferedReader rd = new BufferedReader(new FileReader(filePath));
            String line;
            while((line = rd.readLine()) != null){
                listNeibors.add(line);
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
