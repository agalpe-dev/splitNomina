package com.agp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
	    if (args.length<1){
	        usage();
	        return;
        }

	    File file = new File(args[0].toString());
	    if (!file.exists()){
	        System.out.println("No se puede acceder al archivo. Indique la ruta correctamente.");
	        return;
        }

	    // Worker worker = new Worker("d:\\varios\\vertice\\proyecto\\febrero.pdf"); -> Only for testing
        Worker worker = new Worker(file.getAbsolutePath());
        // Analize pdf before split
        worker.analizePDF();

        // split pdf ones analisys is ok
        try{
            worker.doSplit();
            System.out.println("El proceso ha finalizado correctamente!");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Error en el proceso");
        }
    }

    private static void usage(){
        System.out.println("***** splitNomina v1.0 *****\nEs necesario indicar un archivo a tratar como primer argumento.\nEj: splitNomina febrero.pdf");
    }
}
