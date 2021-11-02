package com.agp;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Worker {

    private String fileIn;
    private String fileOut;
    private PdfReader pdfReader;
    private String folder;
    private HashMap<String, List<Integer>> listIDs = new HashMap<String, List<Integer>>();

    public Worker (String fileIn, String fileOut){
        this.fileIn=fileIn;
        this.fileOut=fileOut;
    }

    public Worker (String fileIn){
        this.fileIn=fileIn;
    }

    public void analizePDF(){
        // get IDs from every pdf page and save to ListIDs
        int totalPages=0;
        String pageText;
        System.out.println("Analizando Pdf: " + fileIn);
        try {
            pdfReader=new PdfReader(fileIn);
            totalPages=pdfReader.getNumberOfPages();

            for (int i=1;i<=totalPages;i++){
                pageText=PdfTextExtractor.getTextFromPage(pdfReader,i);
                String employeeID=getEmployeeIdFromPageText(pageText);
                String employerID=getEmployerIdFromPageText(pageText);

                // Adding employee and page to list
                addEmployeePageToList(employeeID, i);

            }

        }catch (IOException e){
            e.printStackTrace();
            errorOpening();
        }

        System.out.println("Análisis finalizado correctamente.\nTrabajadores: " + listIDs.size() + " - Total páginas: " + totalPages);
    }

    public void doSplit() throws Exception{
        // check listIds is not empty
        if (listIDs.size()<=0){
            System.out.println("Error: algo ha fallado y no se pueden extraer las páginas.");
            return;
        }
        // Folder to split pdf to
        System.out.println("Dividendo el archivo...");
        String folderOut = Long.toString(System.currentTimeMillis());
        checkFolder(folderOut);


        for (Map.Entry<String,List<Integer>> entry : listIDs.entrySet()){
            String employee = entry.getKey();
            ArrayList<Integer> pages = (ArrayList<Integer>) entry.getValue();
            fileOut = employee.toString()+".pdf";

            Document document=new Document(pdfReader.getPageSizeWithRotation(1));
            PdfCopy pdfWriter = new PdfCopy(document,new FileOutputStream(
                    folderOut+File.separator+fileOut));

            for (int page : pages){
                try {
                    document.open();
                    PdfImportedPage pdfImportedPage= pdfWriter.getImportedPage(pdfReader,page);
                    pdfWriter.addPage(pdfImportedPage);

                } catch (DocumentException e) {
                    e.printStackTrace();
                }

            }

            document.close();
            pdfWriter.close();

        }
    }

    private void checkFolder(String folder){
        File folderOut = new File(folder);
        if (!folderOut.exists()){
            try {
                folderOut.mkdir();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Error: No se ha podido crear la carpeta de salida.");
                return;
            }
        }

    }

    private void addEmployeePageToList(String employeeID, int page) {
        ArrayList<Integer> listPages = new ArrayList<Integer>();
        // key does not exist
        if (listIDs.get(employeeID)==null){
            listPages.add(page);
            listIDs.put(employeeID,listPages);
        }else{ // key already exists, add page to list linked to key
            listIDs.get(employeeID).add(page);
        }
    }

    private String getEmployeeIdFromPageText(String pageText) {
        String id=null;
        Pattern pattern;
        Matcher matcher;
        // search DNIs
        pattern=Pattern.compile("\\d{8}[A-Z]");
        matcher=pattern.matcher(pageText);
        if (matcher.find()){
            id=matcher.group();
        }
        // search NIE
        pattern=Pattern.compile("\"[X,Y,Z]\\d{7}[A-Z]\"gm"  );
        matcher=pattern.matcher(pageText);
        if (matcher.find()){
            id=matcher.group();
        }
        return id;
    }

    private String getEmployerIdFromPageText(String pageText) {
        String id=null;
        Pattern pattern=Pattern.compile("[A-B]\\d{8}");
        Matcher matcher=pattern.matcher(pageText);
        if (matcher.find()){
            id=matcher.group();
        }
        return id;
    }

    public int getTotalPages(){

        int pages=0;
        try {
            pdfReader = new PdfReader(fileIn);
            pages=pdfReader.getNumberOfPages();
        } catch (IOException e) {
            e.printStackTrace();
            errorOpening();
        }
        return pages;
    }

    private void errorOpening() {
        System.out.println("Error: No se puede abrir el archivo "+fileIn);
    }
}
