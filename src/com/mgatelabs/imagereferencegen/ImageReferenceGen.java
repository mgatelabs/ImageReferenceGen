package com.mgatelabs.imagereferencegen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to take a folder and create a .h file that contains string references
 *
 * @author Michael Fuller
 */
public class ImageReferenceGen {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final File target;
        final String outputPrefix;
        final boolean addExtension;
        if (args.length >= 2) {
            target = new File(args[0]);
            outputPrefix = args[1];
            if (args.length == 3) {
                addExtension = "true".equalsIgnoreCase(args[2]);
            } else {
                addExtension = true;
            }
        } else {
            System.out.println("Arguments:");
            System.out.println("1. [Target Folder] (Required) - Folder to Scan & Write to");
            System.out.println("2. [Filename Prefix] (Required) - Name to prefix files, for example \"Front\" will generate FrontImageConstants.h");
            System.out.println("3. [Add Extension] (Optional, values:true/false, default:true)");


            System.out.println("Missing target path");
            return;
        }

        ImageReferenceGen gen = new ImageReferenceGen(target, outputPrefix, addExtension);



    }

    public ImageReferenceGen(File target, String outputPrefix, boolean addExtension) {
        List<File> files = listFiles(target);

        if (files.size() == 0) {
            return;
        }

        Map<String, String> keyToValue = new TreeMap<>();

        for (File f: files) {

            String fileName = f.getName();
            if (!addExtension) {
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            }

            keyToValue.put("_" + f.getName().substring(0, f.getName().lastIndexOf(".")).toUpperCase().replaceAll("-", "_").replaceAll("\\.", "_"), fileName);
        }

        StringBuilder hBuilder = new StringBuilder();
        StringBuilder mBuilder = new StringBuilder();

        // Header

        hBuilder.append("//This file is generated, don't modify by hand\n\n");
        mBuilder.append("//This file is generated, don't modify by hand\n\n");

        hBuilder.append("#import <Foundation/Foundation.h>\n\n");
        mBuilder.append("#import \"").append(outputPrefix).append("Constants.h\"\n\n");

        for (Map.Entry<String, String> entry: keyToValue.entrySet()) {
            hBuilder.append("extern NSString *const ").append(entry.getKey()).append(";\n");
            mBuilder.append("NSString *const ").append(entry.getKey()).append(" = @\"").append(entry.getValue()).append("\";\n");
        }

        File hFile = new File(target, outputPrefix + "Constants.h");
        File mFile = new File(target, outputPrefix + "Constants.m");

        writeFile(hFile, hBuilder.toString());
        writeFile(mFile, mBuilder.toString());
    }

    public void writeFile(File target, String out) {

        System.out.println("Writing To: " + target.getAbsolutePath());

        System.out.println(out);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(target);
            fos.write(out.getBytes("UTF-8"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImageReferenceGen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public List<File> listFiles(File target) {
        List<File> files = new ArrayList<>();

        for (File f: target.listFiles()) {
            if (f.isFile() && !f.isDirectory() && f.getName().endsWith("png") && f.getName().indexOf("@") == -1) {
                files.add(f);
            } else {
                System.out.println("Skipping: " + f.getName());
            }
        }
        return files;
    }

}
