package org.micron.nve.mydaemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

/*
 * SVN information
 * $Revision: 3687 $
 * $Author: ftrujillo $
 * $Date: 2015-09-02 11:55:43 -0600 (Wed, 02 Sep 2015) $
 * $HeadURL: http://svn/NSG/comp_ssd/software/trunk/NetBeansProjects/Examples/simple-java-app/src/main/resources/archetype-resources/src/main/java/Main.java $
 *
 */
public class Main {

    private static DaemonContext daemonContext;

    public Main() {
    }

    @SuppressWarnings("FieldMayBeFinal")
    private static Options options = new Options();

    @SuppressWarnings("SleepWhileInLoop")
    public static void main(String[] args) {

        try {
            Main.loadUpOptions(); //  This method will load up private static Options options or throw MainException on duplicate options.

            // Next 3 lines are 100%  Apache CLI
            CommandLineParser parser = new DefaultParser();
            boolean allowNonOptions = false;  // set to false to throw exception on non arg parsed.  
            CommandLine cmd = parser.parse(options, args, allowNonOptions);

            // This is MY post processing to get 'cmd' into something with defaults and honoring the type()'s by type conversion.
            Map<String, Object> cliMap = Main.generateCliMap(cmd);

            if ((Integer) cliMap.get("debug") >= 3) {
                Main.displayCliMap(cliMap);
            }

            // If you threw the --help or -h option.
            if ((Boolean) cliMap.get("help")) {
                displayUsageAndExit(-1);
            }

            System.out.println("Hello world.");

            Main.daemonContext = new DaemonContext() {
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

            MyDaemon myDaemon = new MyDaemon();

            try {
                myDaemon.init(daemonContext);
                myDaemon.start();

                Thread.sleep(4000);
                Thread myThread = myDaemon.getMyThread();


                if (myThread != null && myThread.isAlive()) {
                    System.out.println("Sending interrupt");
                    myThread.interrupt();
                    Thread.sleep(2000);
                }

                while (myThread != null && myThread.isAlive()) {
                    System.out.println("Running ");
                    Thread.sleep(2000);
                    myThread = myDaemon.getMyThread();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Done.");
            System.exit(0);

        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            Main.displayUsageAndExit(10);
        } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
            Main.displayUsageAndExit(11);
        } catch (MainException ex) {
            System.out.println(ex.getMessage());
            System.exit(12);
        }
    }

    /**
     * This is where you will define your options.
     *
     * Please read =>
     * https://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/Option.Builder.html
     *
     * @throws MainException if options fail Main.verifyAndAddOptions()
     */
    public static void loadUpOptions() throws MainException {
        List<Option> optionList = new ArrayList<>();

        optionList.add(Option.builder()
                .longOpt("debug")
                .required(false)
                .hasArg(true)
                .argName("LEVEL")
                .desc("Debug  1,2,3,...")
                .type(Integer.class)
                .build());

        optionList.add(Option.builder("h")
                .longOpt("help")
                .required(false)
                .hasArg(false)
                .desc("This help message")
                .type(Boolean.class)
                .build());

        Main.verifyAndAddOptions(optionList);
    }

    /**
     * Display formatted cliMap when --debug > 3 or --help
     *
     * @param cliMap Map&lt;String, Object&gt;
     *
     */
    public static void displayCliMap(Map<String, Object> cliMap) {
        Iterator<String> itr = cliMap.keySet().iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            String val = cliMap.get(key).toString();
            System.out.println(String.format("%20s: %s", key, val));
        }
        System.out.println("");
    }

    /**
     * This method is 99% Apache Cli, but I added a System.exit(status) for
     * Main.main() to return exit status.
     *
     * @param status 0,1,2,3,4,5,...
     */
    public static void displayUsageAndExit(int status) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println("");
        String header = "\nThis app will do this ...\n\n";
        String footer = "\nPlease report any issues to *******@micron.com\n\nSTATUS: " + status + "\n";
        int width = 132;
        formatter.printHelp(width, "myProgramName", header, options, footer, true);
        System.exit(status);
    }

    /**
     * Generate a Map of Objects based on CLI CommandLine parsed args based on
     * original Options. *** Also uses Main.options are original option list.
     *
     * @param cmd CLI CommandLine object
     * @return Map&lt;String, Objects&gt; with each value converted into it's
     * type() as defined in Option.
     * @throws NumberFormatException
     */
    public static Map<String, Object> generateCliMap(CommandLine cmd) throws NumberFormatException {
        Map<String, Object> cliMap = new TreeMap<>(); // Result Map
        Collection<Option> collection = Main.options.getOptions();  // Original Options
        Iterator<Option> itr = collection.iterator();

        while (itr.hasNext()) {
            Option l_option = itr.next();
            String key = (l_option.hasLongOpt()) ? l_option.getLongOpt() : l_option.getOpt();
            String type = l_option.getType().toString();
            String val = cmd.getOptionValue(key);

            switch (type) {
                case "class java.lang.Boolean":
                    val = (cmd.hasOption(key)) ? "true" : "false";
                    cliMap.put(key, Boolean.parseBoolean(val));
                    break;
                case "class java.lang.Integer":
                    if (cmd.hasOption(key) == false) {
                        val = "0";
                    }
                    try {
                        cliMap.put(key, Integer.parseInt(val));
                    } catch (NumberFormatException ex) {
                        String msg = "\nERROR: CLI parse error for KEY[" + key + "].  The VALUE should have been an Integer.  The VALUE was => " + val + "\n";
                        throw new NumberFormatException(msg);
                    }
                    break;
                case "class java.lang.Double":
                    if (cmd.hasOption(key) == false) {
                        val = "0.0";
                    }
                    try {
                        cliMap.put(key, Double.parseDouble(val));
                    } catch (NumberFormatException ex) {
                        String msg = "\nERROR: CLI parse error for KEY[" + key + "].  The VALUE should have been a Double.  The VALUE was => " + val + "\n";
                        throw new NumberFormatException(msg);
                    }
                    break;
                default:
                    if (cmd.hasOption(key) == false) {
                        val = "";
                    }
                    cliMap.put(key, val);
                    break;
            }
        }

        return cliMap;
    }

    /*
        This is needed due to having duplicate short or long name for option is not handled properly in Apache CLI.
     */
    public static void verifyAndAddOptions(List<Option> optionList) throws MainException {
        Iterator<Option> itr = optionList.iterator();
        Set<String> shortArgs = new TreeSet<>();
        Set<String> longArgs = new TreeSet<>();

        while (itr.hasNext()) {
            Option option = itr.next();
            String key = option.getOpt();
            String lkey = option.getLongOpt();

            if (key != null) {
                if (shortArgs.contains(key)) {
                    String msg = "ERROR:  Duplicate SHORT arg => " + key;
                    throw new MainException(msg);
                } else {
                    shortArgs.add(key);
                }
            }

            if (lkey != null) {
                if (longArgs.contains(lkey)) {
                    String msg = "ERROR:  Duplicate LONG arg => " + lkey;
                    throw new MainException(msg);
                } else {
                    longArgs.add(lkey);
                }
            }

            // If you get this far, then no duplicates.  Just add option.
            // This entire method should be part of Apache CLI in addOption method.
            Main.options.addOption(option);
        }
    }

}
