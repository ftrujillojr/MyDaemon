package org.micron.nve.mydaemon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class MyDaemon implements Daemon {

    private Thread myThread;
    private boolean stopped = false;
    private boolean lastOneWasATick = false;
    private ServerSocket serverSocket = null;
    private static int port = 8099;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        /*
         * Construct objects and initialize variables here.
         * You can access the command line arguments that would normally be passed to your main() 
         * method as follows:
         */
        String[] args = daemonContext.getArguments();

        myThread = new Thread() {
            private final long lastTick = 0;

            @Override
            public void interrupt() {
                try {
                    if (serverSocket != null) {
                        System.out.println("Closed socket");
                        serverSocket.close();
                    }
                } catch (IOException ignored) {
                    System.out.println("would've IOEX");
                    // finally will pass interrupt up the chain.
                } finally {
                    super.interrupt();
                }
            }

            @Override
            public synchronized void start() {
                System.out.println("start inside");
                MyDaemon.this.stopped = false;
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException ex) {
                    System.out.println("Could not read port " + port);
                    Logger.getLogger(MyDaemon.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(-1);
                }

                super.start();
            }

            @Override
            @SuppressWarnings("SleepWhileInLoop")
            public void run() {
                int count = 0; // TODO: takeout count

                while (!stopped) {
                    try {
                        Thread.sleep(1000);  // give the server time to start.
                        
                        System.out.println("Listen for client");
                        Socket client = serverSocket.accept();

                        System.out.println(!lastOneWasATick ? "tick" : "tock");
                        lastOneWasATick = !lastOneWasATick;

                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        System.out.println("Caught  Interrupted Exception.");
                        stopped = true;
                    } catch (IOException ex) {
                        System.out.println("Caught IOException");
                        stopped = true;
                    }

                    if (!stopped && count++ >= 10) { // TODO: takeout if statement
                        System.out.println("Count expired.");
                        stopped = true;
                    }
                }
            }
        };
    }

    public Thread getMyThread() {
        return this.myThread;
    }

    @Override
    public void start() throws Exception {
        System.out.println("start outside");
        myThread.start();
    }

    @Override
    public void stop() throws Exception {
        stopped = true;
        try {
            myThread.join(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public void destroy() {
        myThread = null;
    }
}
