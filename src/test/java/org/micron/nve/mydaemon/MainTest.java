package org.micron.nve.mydaemon;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * ARRANGE ACT ASSERT
 *
 * <pre>
 * http://www.javaworld.com/article/2076265/testing-debugging/junit-best-practices.html
 * http://www.kyleblaney.com/junit-best-practices/
 *
 * ==============================================================================
 * Don't assume the order in which tests within a test case run
 *
 * Put assertion parameters in the proper order
 *
 * The parameters to JUnit's assertions are: 1. expected 2. actual
 *
 * For example, use assertEquals(expected, actual) rather than
 * assertEquals(actual, expected).
 *
 * Ordering the parameters correctly ensures that JUnit's messages are accurate.
 *
 * ==============================================================================
 *
 * assertArrayEquals
 * assertEquals
 * assertNotEqual()
 * assertTrue
 * assertFalse
 * assertNotNull
 * assertNotSame
 * assertNull
 * assertSame
 * assertThat
 * fail()
 * fail(message)
 *
 * </pre>
 */
public class MainTest {

    private static DaemonContext daemonContext;
    private static MyDaemon myDaemon = null;
    private static Thread myThread = null;

    // CLIENT / SERVER
    private static int port = 8099;

    // CLIENT
    private static String host = "nsglnxdev1.micron.com";
    private static int readTimeoutMilliseconds = 10000;

    public MainTest() {
    }  // Do NOT use constructor for set up.

    @BeforeClass
    public static void setUpClass() {
        try {
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

            myDaemon = new MyDaemon(port); // This normally running stand alone.
            myDaemon.setDebug(false);

            myDaemon.init(daemonContext);
            myDaemon.start();

            myThread = myDaemon.getMyThread();

        } catch (Exception ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("MainTest: Sending interrupt to kill SERVER");
        myThread.interrupt();
    }

    @Test
    public void test1() {
        MyClient myClient = new MyClient(host, port, readTimeoutMilliseconds);
        myClient.setDebug(false);

        String[] count = {
            "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten"};

        for (int ii = 0; ii < count.length; ii++) {
            try {
                // ARRANGE
                String request1 = "Hello world [" + count[ii] + "]";
                String expect1 = "RESPONSE: " + request1 + "\n";

                // ACT
                myClient.writeSocket(request1);
                String response1 = myClient.readSocket();

                // ASSERT
                //System.out.println("test1() " + response1);
                assertEquals(expect1, response1);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void simpleClientTest() {
        MyClient myClient = new MyClient(host, port, readTimeoutMilliseconds);
        myClient.setDebug(false);

        try {
            // ARRANGE
            String request1 = "Hello world";
            String expect1 = "RESPONSE: " + request1 + "\n";

            // ACT
            myClient.writeSocket(request1);
            String response1 = myClient.readSocket();

            // ASSERT
            //System.out.println("simpleClientTest() " + response1);
            assertEquals(expect1, response1);

            /////////////////////////////////////////////////////
            /////////////////////////////////////////////////////
            /////////////////////////////////////////////////////
            /////////////////////////////////////////////////////
            // ARRANGE
            String request2 = "This is 2nd call to socket";
            String expect2 = "RESPONSE: " + request2 + "\n";

            // ACT
            myClient.writeSocket(request2);
            String response2 = myClient.readSocket();

            // ASSERT
            //System.out.println("simpleClientTest() " + response2);
            assertEquals(expect2, response2);

        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void test2() {
        MyClient myClient = new MyClient(host, port, readTimeoutMilliseconds);
        myClient.setDebug(false);

        String[] count = {
            "uno", "dos", "tres", "quattro", "cinco",
            "seis", "siete", "ocho", "nueve", "diez"};

        for (int ii = 0; ii < count.length; ii++) {
            try {
                // ARRANGE
                String request1 = "Hello world [" + count[ii] + "]";
                String expect1 = "RESPONSE: " + request1 + "\n";

                // ACT
                myClient.writeSocket(request1);
                String response1 = myClient.readSocket();

                // ASSERT
                //System.out.println("test2() " + response1);
                assertEquals(expect1, response1);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
