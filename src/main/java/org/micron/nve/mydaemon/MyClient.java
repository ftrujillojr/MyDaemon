package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public final class MyClient extends Thread {

    private Socket clientSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private String host = null;
    private int port = 0;

    public MyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void interrupt() {  // Sockets do not honor interrupt unless we do this.
        try {
            if (this.clientSocket != null) {
                this.closeSocket();
            }
        } catch (IOException ignored) {
            // finally will pass interrupt up the chain.
        } finally {
            super.interrupt();
        }
    }

    public void openSocket() throws IOException {
        if (this.clientSocket == null) {
            this.clientSocket = new Socket(this.host, this.port);
            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

    }

}
