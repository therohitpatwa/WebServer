import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Server {
    public Consumer<Socket> getConsumer() {
        return (clientSocket) -> {
            try (
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream())
            ) {
                String requestLine = fromClient.readLine();
                if (requestLine != null) {
                    String[] requestParts = requestLine.split(" ");
                    if (requestParts.length >= 2 && requestParts[0].equals("GET")) {
                        String filePath = requestParts[1];
                        if (filePath.equals("/")) {
                            filePath = "/index.html";
                        }

                        java.io.File file = new java.io.File("www" + filePath);
                        if (file.exists() && !file.isDirectory()) {
                            toClient.println("HTTP/1.1 200 OK");
                            toClient.println("Content-Type: text/html");
                            toClient.println("Content-Length: " + file.length());
                            toClient.println(); 

                            try (BufferedReader fileReader = new BufferedReader(new java.io.FileReader(file))) {
                                String line;
                                while ((line = fileReader.readLine()) != null) {
                                    toClient.println(line);
                                }
                            }
                        } else {
                            toClient.println("HTTP/1.1 404 Not Found");
                            toClient.println();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
    }
    
    public static void main(String[] args) {
        int port = 8010;
        Server server = new Server();
        
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(70000);
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                
                // Create and start a new thread for each client
                Thread thread = new Thread(() -> server.getConsumer().accept(clientSocket));
                thread.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
