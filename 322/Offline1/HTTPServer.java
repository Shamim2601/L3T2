import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;

public class HTTPServer {
    static final int PORT = 5160;

    public static void main(String[] args) throws IOException {
        
        try (ServerSocket serverConnect = new ServerSocket(PORT)) {
            System.out.println("\nServer started.\nListening for connections on port : " + PORT + " ...\n");

            String logDirectory = Paths.get(Paths.get("").toAbsolutePath().toString(), "log").toString();
            File logs = new File(logDirectory + "/logFile.log");
            FileWriter eraser = new FileWriter(logs);
            eraser.write("");

            while(true)
            {
                Socket s = serverConnect.accept();
                System.out.println("Client connected: "+s.toString());

                new ServerThread(s, logs);   
            }
        }
    }
}

class ServerThread implements Runnable{
    Socket s;
    Thread t;
    File logfile;

    String folderItem="<li><a href=\"{href}\"><b><i>{name}</i></b><a></li>";
    String fileItem="<li><a href=\"{href}\" target=\"_blank\">{name}</i></li>";

    public ServerThread(Socket clientSocket, File lfile){
        s = clientSocket;
        logfile = lfile;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            OutputStream ops = s.getOutputStream();
            //PrintWriter pr = new PrintWriter(s.getOutputStream());

            String input = in.readLine();
            System.out.println("Received request : " + input);

            if(input == null) {}
            else if(input.length() > 0) {
                if(input.startsWith("GET")){
                    //extract request and absolute directory path
                    String reqMsg = input.split(" ")[1].split("[?]")[0].replaceAll("%20"," ");
                    if(reqMsg.equals("/")){
                        reqMsg+= "root";
                    }
                    String dir = Paths.get(Paths.get("").toAbsolutePath().toString(),reqMsg).toString();
                    File reqFile = new File(dir);

                    FileWriter fileWriter = new FileWriter(logfile, true);
                    PrintWriter logWriter = new PrintWriter(fileWriter);
                    logWriter.println("HTTP request : "+ input);
                    logWriter.println("HTTP response: ");

                    if(!reqMsg.equals("/favicon.ico")){
                        if(reqFile.exists()){
                            if(reqFile.isDirectory()){
                                String content = parseContent("index.html");

                                String items = "";
                                for (File file:reqFile.listFiles()){
                                    String childRoute=(reqMsg+"/"+file.getName()).replaceAll("//","/");
                                    if(file.isDirectory())
                                        items+=folderItem.replace("{name}",file.getName()).replace("{href}",childRoute)+"\n";
                                    else
                                        items+=fileItem.replace("{name}",file.getName()).replace("{href}",childRoute)+"\n";

                                }

                                content = content.replace("{items}", items);
                                //generateResponse(ops,content,status);
                                String result = generateResponse(content, 200, "text/html", logWriter);
                                ops.write(result.getBytes());
                                ops.flush();
                            }
                            else{
                                if(reqFile.getName().endsWith(".txt")){
                                    //handle txt file
                                    String textContent = parseContent("PreviewText.html");
                                    textContent = textContent.replace("{src}", parseContent(dir));

                                    String result = generateResponse(textContent, 200, "text/html", logWriter);
                                    ops.write(result.getBytes());
                                    ops.flush();
                                }
                                else if(reqFile.getName().endsWith(".png")||reqFile.getName().endsWith(".jpg")){
                                    //handle image file
                                    String imageContent = parseContent("PreviewImage.html");
                                    
                                    byte[] imagebytes = readFileData(reqFile, (int)reqFile.length());
                                    String base64 = Base64.getEncoder().encodeToString(imagebytes);
                                    imageContent = imageContent.replace("{src}", "data:image/jpeg;base64, "+base64);

                                    String result = generateResponse(imageContent, 200, "image/jpeg", logWriter);
                                    ops.write(result.getBytes());
                                    ops.flush();
                                }
                                else{
                                    //send all other files as download
                                    sendFile(ops, reqFile, logWriter);
                                    ops.flush();
                                    ops.close();
                                }
                            }
                        }
                        else{
                            String notFound = "Error 404! Page Not Found";
                            String result = generateResponse(notFound, 404, "text/html", logWriter);
                            ops.write(result.getBytes());
                            ops.flush();
                        }
                    }

                    try {
                        logWriter.close();
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(input.startsWith("UPLOAD")){
                    //handle client upload request
                    String validity = in.readLine();
                    System.out.println(validity);
                    if(validity.split(" ")[0].equalsIgnoreCase("invalid")){
                        try {
                            in.close();
                            s.close();

                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }



                    String rootDirectory = Paths.get(Paths.get("").toAbsolutePath().toString(), "root").toString();

                    File theDir = new File(rootDirectory + "/uploaded");
                    if (!theDir.exists()){
                        theDir.mkdirs();
                    }

                    byte[] inpFileBytes = new byte[2048];

                    try {
                        FileOutputStream fos = new FileOutputStream(new File(rootDirectory+"/uploaded/"+ input.substring(7)));
                        InputStream is = s.getInputStream();

                        int c;
                        while((c=is.read(inpFileBytes)) > 0){
                            fos.write(inpFileBytes);
                        }

                        System.out.println("Upload complete...");
                        fos.close();
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("Invalid input received");
                }
            }

            try {
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(OutputStream ops, File reqFile, PrintWriter lr) throws IOException {
        ops.write("HTTP/1.1 200 OK\r\n".getBytes());
        ops.write("Accept-Ranges: bytes\r\n".getBytes());
        ops.write(("Content-Length: "+reqFile.length()+"\r\n").getBytes());
        ops.write("Content-Type: application/octet-stream\r\n".getBytes());
        ops.write("\r\n".getBytes());

        byte[] filebytes = readFileData(reqFile, (int)reqFile.length());

        for(byte b:filebytes){
            ops.write(b);
            ops.flush();
        }

        lr.println("HTTP/1.1 200 OK\r\n"+
        "Server: Java HTTP Server: 1.0\r\n"+
        "Date: " + new Date() + "\r\n"+
        "Content-Type: application/octet-stream\r\n"+
        "Content-Length: " + reqFile.length() + "\r\n\r\n");
    }

    private String generateResponse(String content, int status, String type, PrintWriter lr) {
        String result = "HTTP/1.1 "+status+" OK\r\n"+
                        "Server: Java HTTP Server: 1.0\r\n"+
                        "Date: " + new Date() + "\r\n"+
                        "Content-Type: "+type+"\r\n"+
                        "Content-Length: " + content.length() + "\r\n"+
                        "\r\n"+ content;

        lr.println("HTTP/1.1 "+status+" OK\r\n"+
        "Server: Java HTTP Server: 1.0\r\n"+
        "Date: " + new Date() + "\r\n"+
        "Content-Type: "+type+"\r\n"+
        "Content-Length: " + content.length() + "\r\n\r\n");

        return result;
    }

    private String parseContent(String fileName) throws IOException{
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while(( line = br.readLine()) != null ) {
            sb.append( line );
            sb.append( '\n' );
        }
        br.close();
        return sb.toString();
    }

    public static byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

}
