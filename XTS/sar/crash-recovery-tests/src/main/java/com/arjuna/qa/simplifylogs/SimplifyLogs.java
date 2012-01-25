package com.arjuna.qa.simplifylogs;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author paul.robinson@redhat.com, 2012-01-17
 */
public class SimplifyLogs
{
    public static void main(String... args) throws Exception
    {
        if (args.length != 2)
        {
            System.err.println("Usage: SimplifyLogs <log dir> <output dir>");
            System.exit(1);
        }

        File logDir = createAndTestDir(args[0], false);
        File outputDir = createAndTestDir(args[1], true);

        for (File file : logDir.listFiles())
        {
            if (file.isDirectory())
            {
                continue;
            }

            System.out.println("Processing: '" + file.getName() + "'");
            List<String> log = loadLog(file);
            
            if (log.size() == 0)
            {
                System.err.println("Empty file: " + file.getName());
                continue;
            }
            
            log = stripJVMExitFromLastLine(log);
            log = simplifyUUIDs(log);
            log = removeDuplicatesKeepingLastOccurrence(log);

            List<String> beforeCrash = new ArrayList<String>();
            List<String> afterCrash = new ArrayList<String>();
            splitIntoBeforeAndAfterCrash(log, beforeCrash, afterCrash);

            outputProcessedFile(beforeCrash, afterCrash, outputDir, file.getName(), file);
        }
    }
    
    static List<String> stripJVMExitFromLastLine(List<String> log)
    {
        int lastIndex = log.size()-1;
        if (log.get(lastIndex).equals("JVM exit"))
        {
            log.remove(lastIndex);
        }
        return log;
    }

    static void printLog(List<String> log, String title, PrintStream out)
    {
        out.println("=== " + title + "===");
        for (String line : log)
        {
            out.println(line);
        }
    }

    static void outputProcessedFile(List<String> beforeCrash, List<String> afterCrash, File outputLocation, String outFileName, File inputFile) throws Exception
    {
        File outFile = new File(outputLocation.getPath() + File.separator + outFileName);
        FileOutputStream fstream = new FileOutputStream(outFile);
        DataOutputStream out = new DataOutputStream(fstream);
        PrintStream printStream = new PrintStream(out);
        
        printStream.println("Original File: " + inputFile.getAbsoluteFile());
        printStream.println();
        printStream.println();
        printLog(beforeCrash, "Before crash", printStream);
        printStream.println();
        printStream.println();
        printLog(afterCrash, "After crash", printStream);
    }

    static File createAndTestDir(String path, boolean create)
    {
        File result = new File(path);

        if (create)
        {
            result.mkdir();
        }
        
        if (!result.exists())
        {
            System.err.println("Directory does not exist: " + path);
            System.exit(1);
        }

        if (!result.isDirectory())
        {
            System.err.println("Path represents a file, not a directory: " + path);
            System.exit(1);
        }

        return result;
    }

    static void splitIntoBeforeAndAfterCrash(List<String> log, List<String> beforeCrash, List<String> afterCrash)
    {
        boolean before = true;
        for (String line : log)
        {
            if (before)
            {
                if (line.equals("JVM exit"))
                {
                    before = false;
                    continue;
                }
                beforeCrash.add(line);
            }
            else
            {
                afterCrash.add(line);
            }
        }
    }

    static List<String> removeDuplicatesKeepingLastOccurrence(List<String> list)
    {
        List<String> result = new ArrayList<String>();
        List<String> seenLines = new ArrayList<String>();

        ListIterator i = list.listIterator();
        while (i.hasNext())
        {
            i.next();
        }

        while (i.hasPrevious())
        {
            String line = (String) i.previous();
            if (!seenLines.contains(line))
            {
                seenLines.add(line);
                result.add(line);
            }
        }

        return reverse(result);
    }

    static List<String> loadLog(File fileName) throws IOException
    {
        List<String> log = new ArrayList<String>();

        FileInputStream fstream = new FileInputStream(fileName);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        //Read File Line By Line
        while ((line = br.readLine()) != null)
        {
            log.add(line);
        }
        in.close();
        return log;
    }

    static List<String> reverse(List<String> list)
    {
        List<String> result = new ArrayList<String>();
        ListIterator i = list.listIterator();
        while (i.hasNext())
        {
            i.next();
        }

        while (i.hasPrevious())
        {
            String line = (String) i.previous();
            result.add(line);
        }

        return result;
    }

    static List<String> simplifyUUIDs(List<String> log)
    {
        Pattern pattern = Pattern.compile("(-)*[a-fA-F0-9]+:(-)*[a-fA-F0-9]+:(-)*[a-fA-F0-9]+:(-)*[a-fA-F0-9]{8}:(-)*[a-fA-F0-9]+");

        Map<String, Integer> simplifiedUUIDS = new HashMap<String, Integer>();

        List<String> result = new ArrayList<String>();
        for (String line : log)
        {
            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                String uuid = matcher.group();

                Integer simplifiedID = simplifiedUUIDS.get(uuid);
                if (simplifiedID == null)
                {
                    simplifiedID = simplifiedUUIDS.size();
                    simplifiedUUIDS.put(uuid, simplifiedID);
                }

                result.add(line.replace(uuid, String.valueOf(simplifiedID)));
            }
            else
            {
                result.add(line);
            }
        }
        return result;
    }

}
