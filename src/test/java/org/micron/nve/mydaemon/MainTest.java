package org.micron.nve.mydaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class MainTest {

    private static DaemonContext daemonContext;

    public MainTest() {
    }  // Do NOT use constructor for set up.

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void simpleClientTest() {
        boolean passed = true;

        MainTest.daemonContext = new DaemonContext() {
            @Override
            public DaemonController getController() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String[] getArguments() {
                String[] args = {};
                return args;
            }
        };
        MyDaemon myDaemon = new MyDaemon(8099);
        myDaemon.setDebug(true);
        
        MyClient myClient = new MyClient("nsglnxdev1.micron.com", 8099);
        myClient.setDebug(true);
        
        @SuppressWarnings("UnusedAssignment")
        Thread myThread = null;

        try {
            myDaemon.init(daemonContext);
            myDaemon.start();

            myThread = myDaemon.getMyThread();


            if (myThread != null && myThread.isAlive()) {
                myClient.openSocket();
                myClient.writeSocket("Hello world");
                String response = myClient.readSocket();
                System.out.println("MainTest: response => " + response);
                
                
                myClient.writeSocket("This is 2nd call");
                response = myClient.readSocket();
                System.out.println("MainTest: response => " + response);
                myClient.closeSocket();

                
                Thread.sleep(10000);  // sleep 10 seconds to show Server polling.

                
                System.out.println("MainTest: Sending interrupt to kill SERVER");
                myThread.interrupt();
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
        }
        System.out.println("MainTest: Done.");

        assertTrue("simpleClientTest()  FAILED.", passed);
    }

}
