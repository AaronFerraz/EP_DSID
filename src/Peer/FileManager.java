package Peer;

import java.io.File;

public class FileManager {
    private String path;

    public FileManager(String path){
        this.path = path;
    }

    public void ListFilesShared(){
        File dir = new File(path);

        if(dir.isDirectory()){
            File[] files = dir.listFiles();

            if(files != null){
                for(File file : files){
                    String fullName = file.getName();

                    System.out.println(fullName);
                }
            }
        }
    }

    public static void main(String[] args) {
        FileManager file = new FileManager("F:\\Faculdade-USP\\Semestre 5\\DSID\\EP\\EP1\\Peer1\\Arquivos");
        file.ListFilesShared();
    }
}
