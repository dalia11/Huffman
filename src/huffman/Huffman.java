package huffman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Huffman {
    //comparator: node value as8ar priority akbar w tetla3 l awel
    static PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    static TreeMap<Character, String> codes = new TreeMap<>();//keep every caracter with its code
    static String text = "";
    static String encodedBinary = "";
    static String encodedString = "";
    static String decoded = "";
    static TreeMap <Character,Integer>frequency=new TreeMap<>();//keep every character with its freq
    static BufferedWriter compressOutput = null; //write in file
    static BufferedWriter decompressOutput = null;
    static int compressedLength = 0; //bytes after
    static int uncompressedLength = 0;//bytes before

    public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
         int decision = 0;
        Scanner console = new Scanner(System.in);
        while (decision != 4) {
            System.out.println("\n---- Menu ----\n"
                    + "-- [1] to compress\n"
                    + "-- [2] to decompress\n"
                    + "-- [3] to compress folder\n"
                    + "-- [4] exit\n");
            decision = Integer.parseInt(console.nextLine());
            long startTime = System.currentTimeMillis();
            long endTime = 0;
            if(decision==1||decision==2||decision==3){
            if (decision == 3)
            {
              Scanner s3 = new Scanner(System.in);
              System.out.println("Enter The Full Folder Path:");
              String x = s3.nextLine();
                System.out.println("Enter output file path \n");
                String outputFile = console.nextLine();
              File[] files = new File(x).listFiles();
               compressOutput = new BufferedWriter(new FileWriter(new File(outputFile), false));
              for(int i = 0;i<files.length;i++){
                  compressOutput.append("\n file Number "+(i+1)+"\n");
                  System.out.println("File Number "+(i+1)+"\n"); 
                  Scanner scanner = new Scanner(new FileReader(files[i]));
                    handleEncodingNewText(scanner,decision);
                    endTime = System.currentTimeMillis();
                    System.out.println("compression ratio=" + (float) ((float) compressedLength / uncompressedLength));
                     compressOutput.append("\nEND of file\n");
            }
            
            compressOutput.close();}
            if (decision == 1) {
               System.out.println("enter file path \n");
                    String filePath = console.nextLine();
                    Scanner scanner = new Scanner(new File(filePath));
                    System.out.println("enter output file path \n");
                    String outputFile = console.nextLine();           
                    compressOutput = new BufferedWriter(new FileWriter(new File(outputFile), false));
                    text = new String(Files.readAllBytes(Paths.get(filePath)));  //read file in bytes transform in string          
                    handleEncodingNewText(scanner,decision);
                    endTime = System.currentTimeMillis();
                    System.out.println("compression ratio=" + (float) ((float) compressedLength / uncompressedLength));
                    compressOutput.close();
                    
                } else if (decision == 2) {
                      System.out.println("enter file path \n");
                   String filePath = console.nextLine();
                   Scanner scanner = new Scanner(new File(filePath));
                    handleDecodingNewText(scanner);
                    endTime = System.currentTimeMillis();
                    System.out.println("compression ratio=" + (float) ((float) compressedLength / uncompressedLength));
                  
                }
            
            System.out.println("Time=" + (endTime - startTime));

        }else if(decision!=4){
                System.out.println("Try Again");
                continue;
        }
        }
    }

    private static boolean handleEncodingNewText(Scanner scanner,int decision) {
        if(decision==3){
        text = "";
        while (scanner.hasNextLine()) {
            text += scanner.nextLine();
            text += "\n";
        }}
frequency.clear(); //reset
nodes.clear();
        codes.clear();
        decoded = "";
        encodedBinary = "";
        encodedString = "";
        calculateCharIntervals();
        buildTree(nodes);
        generateCodes(nodes.peek(), "");
        printCodes();
        encodeText();
        return false;
    }

       private static void encodeText() {
        encodedBinary = ""; //for bits
        encodedString = ""; //for encode for new character
        compressedLength = 0;
        uncompressedLength = text.length();
        int extraZeros = 0;
        for (int i = 0; i < text.length(); i++) {
            encodedBinary += codes.get(text.charAt(i));
        }
        for (int i = 0; i < encodedBinary.length(); i += 8) {
            if ((encodedBinary.length() - i) > 8) {
                //convert binary to int then to character
                encodedString += ((char) Integer.parseInt(encodedBinary.substring(i, i + 8), 2));
            } else {
                String remain = encodedBinary.substring(i);
                extraZeros = 8 - remain.length();
                for (int j = 0; j < extraZeros; j++) {
                    remain += '0';
                }
                encodedString += (char) Integer.parseInt(remain, 2);
            }
        }
        try {
            compressOutput.append("extra Zeros=" + extraZeros + "\n");
            compressOutput.append("end of header\n");
            compressOutput.append(encodedString);
            compressedLength = encodedString.length();
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void handleDecodingNewText(Scanner scanner) throws IOException {
        Scanner console = new Scanner(System.in);
        do {
          System.out.println("enter output file path \n");
String outputFile = console.nextLine();
            decompressOutput = new BufferedWriter(new FileWriter(new File(outputFile), false));
                
frequency.clear();
nodes.clear();
            codes.clear();
            decoded = "";
            encodedBinary = "";
            encodedString = "";
            //flags
            int headerEnd = 0;
            int endIntevals = 0;
            int startIntevals = 0;
            int extraZeros = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //read new file
                if (line.equals("END of file")) {
                    break;
                }
                if (line.equals("-- intervals --")) {
                    startIntevals = 1;
                    continue;
                }
                if (line.equals("--- Printing Codes ---")) {
                    endIntevals = 1;
                    continue;
                }
                if (line.contains("extra Zeros")) {
                    extraZeros = Character.getNumericValue(line.charAt(line.length() - 1));
                    continue;
                }
//ascii freq part
                if (startIntevals == 1 && endIntevals == 0) {
                    char c;
                    int length = line.length();
                    String[] words = line.split(":");
                    c = (char) Integer.parseInt(words[0].trim());
                frequency.put(c, Integer.parseInt(words[1].trim()));

                }
              
                if (line.equals("end of header")) {
                    headerEnd = 1;
                    continue;
                }
                if (headerEnd == 1) {
                    encodedString += line;
                }
            }
            int encodedStringLength = encodedString.length();
            for (int i = 0; i < encodedStringLength; i++) {
                //last charac to remove extra zeros
                if (i == encodedStringLength - 1) {
                    char c = encodedString.charAt(i);
                    String cbinary = String.format("%08d", Integer.parseInt(Integer.toBinaryString(c)));
                    int remain = 8 - extraZeros;
                    for (int j = 0; j < remain; j++) {
                        //bits without extra
                        encodedBinary += cbinary.charAt(j);
                    }
                } else {
                    //not last character
                    char c = encodedString.charAt(i);
                    String cbinary = String.format("%08d", Integer.parseInt(Integer.toBinaryString(c)));
                    encodedBinary += cbinary;

                }
            }
                           for ( Character c: frequency.keySet() ) {
          
                    nodes.add(new Node(frequency.get(c),c));
           
         }
              buildTree(nodes);
            decodeText(encodedBinary);
            compressedLength = encodedString.length();
            decompressOutput.close();
        } while (scanner.hasNextLine());
    }

      private static void decodeText(String S) {
          //point to first one in queue without dequeue
          //traverse tree
        Node c = nodes.peek();
        int length = S.length();
        for (int i = 0; i < length; i++) {
            if (S.charAt(i) == '1') {
                c = c.right;
            } else {
                c = c.left;
            }
            if (c.left == null && c.right == null) {
                decoded += c.character;
                c = nodes.peek();
            }
        }
      
        try {

            decompressOutput.append(decoded);
            uncompressedLength = decoded.length();
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static void buildTree(PriorityQueue<Node> vector) {
        while (vector.size() > 1) {
            //poll= dequeue
            vector.add(new Node(vector.poll(), vector.poll()));
        }
    }

    private static void printCodes() {
        try {
            compressOutput.append("--- Printing Codes ---\n");
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
        //loop on tree map of codes, k= character, v=value
        codes.forEach((k, v) -> {
            try {
                compressOutput.append(k + " : " + v + "\n");
            } catch (IOException ex) {
                Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        try {
            compressOutput.append("end of codes\n");
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void calculateCharIntervals() {
          try {
            compressOutput.append("-- intervals --\n");
        } catch (IOException ex) {
            Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
        }
          //freq of every character
        for (int i = 0; i < text.length(); i++) {
            if (!frequency.containsKey(text.charAt(i))) {
                frequency.put(text.charAt(i), 1);
            } else {
                frequency.put(text.charAt(i), frequency.get(text.charAt(i)) + 1);
            }
        }
//loop on map of freq.
                for ( Character c: frequency.keySet() ) {
                try {
                    nodes.add(new Node(frequency.get(c),c));
                    String s = (int)c + " : " + frequency.get(c) + "\n";
                    compressOutput.append(s);
                } catch (IOException ex) {
                    Logger.getLogger(Huffman.class.getName()).log(Level.SEVERE, null, ex);
                }
         }
    }

    private static void generateCodes(Node node, String s) {
        //tree not empty
        if (node != null) {
            if (node.right != null) {
                generateCodes(node.right, s + "1");
            }

            if (node.left != null) {
                generateCodes(node.left, s + "0");
            }
//leaf node then put code in tree
            if (node.left == null && node.right == null) {
                codes.put(node.character, s);
            }
        }
    }

}
