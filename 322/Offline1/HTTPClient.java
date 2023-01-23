import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Scanner;

public class HTTPClient {
    public static void main(String[] args) throws UnknownHostException, IOException {    
        System.out.println("Enter upload command:");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String command = input.split(" ")[0].toUpperCase();

        while(!input.equalsIgnoreCase("exit")  && command.equals("UPLOAD")){
            new ClientThread(input.split(" ")[1]);
            input = scanner.nextLine();
            command = input.split(" ")[0].toUpperCase();
        }
        
        scanner.close();
    }
}

class ClientThread implements Runnable{
    Socket socket;
    String fileName;
    Thread t;

    public ClientThread(String fname){
        this.fileName = fname;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try {
            socket = new Socket("127.0.0.1", 5160);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("filename: "+ fileName);

        try {
            String filePath = Paths.get(Paths.get("").toAbsolutePath().toString(), fileName).toString();
            File inpFile = new File(filePath);
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            pr.write("UPLOAD "+ fileName +"\r\n");
            pr.flush();

            boolean var = fileName.endsWith(".txt") || fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".mp4");

            if(!inpFile.exists()){
                pr.write("Invalid request! File does not exist... \r\n");
                pr.flush();
                System.out.println("Invalid request! File does not exist...");
                return;
            }
            else if(!var){
                pr.write("Invalid request! File type not allowed... \r\n");
                pr.flush();
                System.out.println("Invalid request! File type not allowed...");
                return;
            }
            else{
                pr.write("Valid file...\r\n");
                pr.flush();
                System.out.println("Valid file...");
            }

            byte[] fileBytes = new byte[2048];
            OutputStream ops = socket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inpFile));
            
            int c;
            while((c = bis.read(fileBytes)) > 0){
                ops.write(fileBytes, 0, c);
                ops.flush();
            }

            pr.close();
            ops.close();
            bis.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
