package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class MyServer extends Thread {

    private ServerSocket serverSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private int port = 0;
    private Socket clientSocket = null;

    public MyServer(int port) {
        this.port = port;
    }

    @Override
    public void interrupt() {  // Sockets do not honor interrupt unless we do this.
        try {
            if (this.serverSocket != null) {
                this.closeSocket();
            }
        } catch (IOException ignored) {
            // finally will pass interrupt up the chain.
        } finally {
            super.interrupt();
        }
    }

    public void openSocket() throws IOException {
        if (this.serverSocket == null) {
            System.out.println("one");
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setSoTimeout(10000);
            System.out.println("two");
        }
    }

    public void writeSocket(String message) throws IOException {
        if (this.out != null) {
            this.out.println(message);
            this.out.println("<END>");
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    public String readSocket() throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();
        String line;
        
        this.clientSocket = this.serverSocket.accept();
        System.out.println("three");
        this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
        System.out.println("four");
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("five");

        if (this.in != null) {
            while ((line = this.in.readLine()) == null) {
                // waiting for non-null response.
                Thread.sleep(2000);
            }

            // line should contain some response now.
            sb.append(line);

            if (line.indexOf("<END>") == -1) {
                // if there is more data, then grab it.
                while ((line = this.in.readLine()) != null) {
                    sb.append(line);
                    if (line.indexOf("<END>") != -1) {
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

    public void closeSocket() throws IOException {
        if (this.in != null) {
            this.in.close();
            this.in = null;
        }

        if (this.out != null) {
            this.out.close();
            this.out = null;
        }

        if (this.clientSocket != null) {
            this.clientSocket.close();
            this.clientSocket = null;
        }

        if (this.serverSocket != null) {
            this.serverSocket.close();
            this.serverSocket = null;
        }
    }

}
