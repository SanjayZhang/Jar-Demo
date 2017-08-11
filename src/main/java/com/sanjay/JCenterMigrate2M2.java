package com.sanjay;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanjay.zsj09@gmail.com on 2017/3/4.
 */
public class JCenterMigrate2M2 {

    private static void copyFolder(File src, File dest, boolean removeRoot) throws IOException {
        if (!dest.exists() && !dest.mkdirs()) {
            return;
        }

        if (src.isDirectory()) {
            File[] fs = src.listFiles();
            if (fs == null) {
                return;
            }

            for (File sf : fs) {
                //40 regard as
                if (src.getName().length() >= 39 || removeRoot) {
                    copyFolder(sf, new File(dest.getPath() + File.separator), false);
                } else {
                    copyFolder(sf, new File(dest.getPath() + File.separator + src.getName()), false);
                }
            }
        } else {
            InputStream ins = null;
            try {
                ins = new FileInputStream(src);
                FileOutputStream outs = null;
                try {
                    outs = new FileOutputStream(new File(dest.getPath() + File.separator + src.getName()));
                    byte[] bytes = new byte[1024 * 512];
                    int length;
                    while ((length = ins.read(bytes)) != -1) {
                        outs.write(bytes, 0, length);
                    }
                    outs.flush();
                } finally {
                    if (outs != null) {
                        outs.close();
                    }
                }
            } finally {
                if (ins != null) {
                    ins.close();
                }
            }
        }
    }

    private static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    private static boolean isNumeric(String str) {
        Pattern p = Pattern.compile("^-?[0-9]+$");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private static String getM2Field(File file) {
        if (file == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String[] strings = file.getName().split("\\.");
        //filter like this:com.squareup.retrofit2
        for (String string : strings) {
            if (isNumeric(string)) {
                sb.setLength(0);
                sb.append(File.separator);
                sb.append(file.getName());
                return sb.toString();
            }

            sb.append(File.separator);
            sb.append(string);
        }
        return sb.toString();
    }

    public static void main(String... args) {
        if (args.length < 1) {
            System.err.println("none args!");
            System.exit(-1);
        }

        String inputDirectory = args[0];
        if (isEmpty(inputDirectory)) {
            System.err.println("inputDirectory empty!");
            System.exit(-2);
        }

        File fIn = new File(inputDirectory);
        String outputDirectory;
        if (args.length < 2) {
            outputDirectory = fIn.getParent();
        } else {
            outputDirectory = args[1];
        }

        if (isEmpty(outputDirectory)) {
            System.err.println("outputDirectory empty!");
            System.exit(-3);
        }

        StringBuilder sb = new StringBuilder()
                .append(outputDirectory).append(File.separator)
                .append("m2")
                .append(getM2Field(fIn));

        outputDirectory = sb.toString();

        File[] files = fIn.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                sb.setLength(0);
                sb.append(outputDirectory)
                        .append(getM2Field(file));

                File out = new File(sb.toString());
                try {
                    copyFolder(file, out, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("finish! output:" + outputDirectory);
    }

}
