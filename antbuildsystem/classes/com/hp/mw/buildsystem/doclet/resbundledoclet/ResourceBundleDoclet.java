/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceBundleDoclet.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mw.buildsystem.doclet.resbundledoclet;

import com.sun.javadoc.*;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.util.HashSet;

/**
 * This class is a doclet for the JavaDoc tool which ships with the Java Development Kit.
 * It produces resource bundle property files from comments placed in Java source.  The
 * comments have the following format:
 *
 *      @message <key> <text>
 *      e.g. @message foo This is a message: {0}
 *
 * It takes the following parameters (some of which are mandatory):
 *
 *      -resourcebundle <filename>  This pecifies the name of the resource bundle to create, only use
 *                                  this if the Doclet is to produce a single resource bundle.
 *      -basedir <directory>        This specifies the base directory to generate the resource bundle
 *                                  property files within (MANDATORY).
 *      -perclass                   This indicates that the doclet should produce resource bundles
 *                                  per class.   If this is not specified then a single resource bundle
 *                                  properties file is produced for all of the source specified.
 *      -ignorerepetition           This indicates that the doclet should ignore key repetition and not
 *                                  flag an error.
 *      -language <language code>   This indicates which language is to be used.
 *      -locale <locale code>       This indicates which locale is to be used.
 *      -properties                 This indicates that the property filename should be postfixed with
 *                                  the .properties postfix.
 */
public class ResourceBundleDoclet extends Doclet
{
    private static final String RESOURCE_BUNDLE_PARAMETER = "-resourcebundle";
    private static final String PER_CLASS_GENERATION_PARAMETER = "-perclass";
    private static final String DESTINATION_DIRECTORY_PARAMETER = "-basedir";
    private static final String REPETITION_PARAMETER = "-ignorerepetition";
    private static final String LANGUAGE_PARAMETER = "-language";
    private static final String LOCALE_PARAMETER = "-locale";
    private static final String PROPERTIES_PARAMETER = "-properties";
    private static final String APPEND_KEY_TO_TEXT_PARAMETER = "-appendkey";

    private static final int RESOURCE_BUNDLE_ELEMENTS = 2;
    private static final int PER_CLASS_GENERATION_ELEMENTS = 1;
    private static final int DESTINATION_DIRECTORY_ELEMENTS = 2;
    private static final int REPETITION_ELEMENTS = 1;
    private static final int LANGUAGE_ELEMENTS = 2;
    private static final int LOCALE_ELEMENTS = 2;
    private static final int PROPERTIES_ELEMENTS = 2;
    private static final int APPEND_KEY_TO_TEXT_ELEMENTS = 2;

    private static final int INVALID_PARAMETER = -1;

    private static final String MESSAGE_TAG = "@message";
    private static final String RESOURCE_TAG = "@resource";
    private static final String DEFAULT_LOCALE = null;
    private static final String DEFAULT_LANGUAGE = null;

    private static String ResourceBundleParameter = null;
    private static String DestinationDirectory = ".";
    private static String Language = DEFAULT_LANGUAGE;
    private static String Locale = DEFAULT_LOCALE;
    private static boolean PropertiesPostfix = false;
    private static boolean PerClassGeneration = false;
    private static boolean AppendKey = false;
    private static HashSet PropertyKeys = new HashSet();
    private static boolean IgnoreRepetition = false;

    private static RootDoc ThisRoot = null;

    private static final String HEADER = "#--------------------------------------------\n"+
                                         "# Copyright 2002\n" +
                                         "# Hewlett Packard\n" +
                                         "# Arjuna Labs\n"+
                                         "#--------------------------------------------\n";
    /**
     * Returns the number of elements to a given parameter
     *
     * @param option The parameter passed in
     * @return The number of elements required for this parameter
     */
    public static int optionLength(String option)
    {
        /**
         * The -resourcebundle parameter has the following format:
         *
         *    -resourcebundle <filename.properties>
         */
        if (option.equalsIgnoreCase(RESOURCE_BUNDLE_PARAMETER))
        {
            return(RESOURCE_BUNDLE_ELEMENTS);
        }
        else
        {
            /**
             * The -perclass parameter has the following format:
             *
             *   -perclass
             */
            if (option.equalsIgnoreCase(PER_CLASS_GENERATION_PARAMETER))
            {
                return(PER_CLASS_GENERATION_ELEMENTS);
            }
            else
            {
                /**
                 * The -basedir parameter has the following format:
                 *
                 *   -basedir <base directory>
                 */
                if (option.equalsIgnoreCase(DESTINATION_DIRECTORY_PARAMETER))
                {
                    return(DESTINATION_DIRECTORY_ELEMENTS);
                }
                else
                {
                    /**
                     *  The -language parameter has the following format:
                     *
                     *   -language <two letter language code>
                     */
                    if (option.equalsIgnoreCase(LANGUAGE_PARAMETER))
                    {
                        return(LANGUAGE_ELEMENTS);
                    }
                    else
                    {
                        /**
                         * The -locale parameter has the following format:
                         *
                         *   -locale <locale code>
                         */
                        if (option.equalsIgnoreCase(LOCALE_PARAMETER))
                        {
                            return(LOCALE_ELEMENTS);
                        }
                        else
                        {
                            /**
                             * The -ignorerepetition parameter has the following format:
                             *
                             *   -ignorerepetition
                             */
                            if (option.equalsIgnoreCase(REPETITION_PARAMETER))
                            {
                                return(REPETITION_ELEMENTS);
                            }
                            else
                            {
                                /**
                                 * The -properties parameter has the following format:
                                 *
                                 *   -properties
                                 */
                                if (option.equalsIgnoreCase(PROPERTIES_PARAMETER))
                                {
                                    return(PROPERTIES_ELEMENTS);
                                }
                                else
                                {
                                    /**
                                     * The -appendkey parameter has the following format:
                                     *
                                     *   -appendkey
                                     */
                                    if (option.equalsIgnoreCase(APPEND_KEY_TO_TEXT_PARAMETER))
                                    {
                                        return(APPEND_KEY_TO_TEXT_ELEMENTS);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return(INVALID_PARAMETER);
    }

    private static void parseOptions(String[][] options)
    {
        for (int optionCount=0;optionCount<options.length;optionCount++)
        {
            String[] subOptions = options[optionCount];

            if (subOptions[0].equalsIgnoreCase(RESOURCE_BUNDLE_PARAMETER))
            {
                ResourceBundleParameter = subOptions[1];
            }
            else
            if (subOptions[0].equalsIgnoreCase(PER_CLASS_GENERATION_PARAMETER))
            {
                PerClassGeneration = true;
            }
            else
            if (subOptions[0].equalsIgnoreCase(REPETITION_PARAMETER))
            {
                IgnoreRepetition = true;
            }
            else
            if (subOptions[0].equalsIgnoreCase(DESTINATION_DIRECTORY_PARAMETER))
            {
                DestinationDirectory = subOptions[1];
            }
            else
            if (subOptions[0].equalsIgnoreCase(LANGUAGE_PARAMETER))
            {
                Language = subOptions[1];
            }
            else
            if (subOptions[0].equalsIgnoreCase(LOCALE_PARAMETER))
            {
                Locale = subOptions[1];
            }
            else
            if (subOptions[0].equalsIgnoreCase(PROPERTIES_PARAMETER))
            {
                PropertiesPostfix = true;
            }
            else
            if (subOptions[0].equalsIgnoreCase(APPEND_KEY_TO_TEXT_PARAMETER))
            {
                AppendKey = true;
            }
        }

        ThisRoot.printNotice("Per Class: "+PerClassGeneration);
        ThisRoot.printNotice("Append Key to National Text: "+AppendKey);
        ThisRoot.printNotice("Destination Directory: "+DestinationDirectory);
        if (PerClassGeneration)
        {
            ThisRoot.printNotice("Language: "+Language);
            ThisRoot.printNotice("Locale: "+Locale);
            ThisRoot.printNotice("Properties Postfix: "+PropertiesPostfix);
        }
        else
        {
            ThisRoot.printNotice("Resource Bundle: "+ResourceBundleParameter);
        }
        ThisRoot.printNotice("Ignore Repetition: "+IgnoreRepetition);
    }

    /**
     * Validate the parameters passed in
     */
    public static boolean validOptions(String[][]       options,
                                       DocErrorReporter reporter)
    {
        boolean returnValue = true;
        boolean resourceBundleParameter = false;
        boolean baseDirSpecified = false;
        boolean perClass = false;

        for (int optionCount=0;optionCount<options.length;optionCount++)
        {
            String[] subOptions = options[optionCount];

            if (subOptions[0].equalsIgnoreCase(RESOURCE_BUNDLE_PARAMETER))
            {
                if (resourceBundleParameter)
                {
                    reporter.printError("Resource bundle specified twice");
                    returnValue = false;
                }

                resourceBundleParameter = true;
            }
            else
            if (subOptions[0].equalsIgnoreCase(PER_CLASS_GENERATION_PARAMETER))
            {
                perClass = true;
            }
            else
            if (subOptions[0].equalsIgnoreCase(DESTINATION_DIRECTORY_PARAMETER))
            {
                baseDirSpecified = true;
            }
        }

        if ((!perClass) && (!resourceBundleParameter))
        {
            reporter.printError("Not using per class resource bundle generation and no resource bundle file specified (use "+RESOURCE_BUNDLE_PARAMETER+")");
            returnValue = false;
        }
        if (!baseDirSpecified)
        {
            reporter.printNotice("Please specify a base director (using "+DESTINATION_DIRECTORY_PARAMETER+" <directory>)");
            returnValue = false;
        }

        return(returnValue);
    }

    /**
     * Process the document
     *
     * @param root The RootDoc of the document set
     */
    public static boolean start(RootDoc root)
    {
        ThisRoot = root;

        ThisRoot.printNotice("ResourceBundle Generator Doclet");
        ThisRoot.printNotice("HP Arjuna Labs");
        /**
         * Parse and interpret the options
         */
        parseOptions(root.options());

        /**
         * Retrieve all packages specified
         */
        PackageDoc[] packages = root.specifiedPackages();
        StringBuffer results = new StringBuffer();

        for (int count=0;count<packages.length;count++)
        {
            parseClasses(packages[count].allClasses(),results);
        }

        parseClasses(root.specifiedClasses(),results);

        if (!PerClassGeneration)
        {
            writeResourceBundleFile(results, new File(DestinationDirectory, ResourceBundleParameter));
        }

        return(true);
    }

    /**
     * This method parses an array of classes and produces the required key/text pairs
     * in the string buffer.
     */
    protected static StringBuffer parseClasses(ClassDoc[] classes, StringBuffer results)
    {
        for (int count=0;count<classes.length;count++)
        {
            ClassDoc cl = classes[count];

            /**
             * If we are performing per class resource bundle generation
             * then clear the stored property keys as they will not affect
             * this class
             */
            if (PerClassGeneration)
            {
                PropertyKeys.clear();
            }

            /**
             * Parse the class level doc
             */
            if (!parseDoc(cl, results))
            {
                ThisRoot.printError("Error while parsing class '"+cl.qualifiedTypeName()+"'");
            }
            else
            {
                /**
                 * Parse the constructor level docs
                 */
                if (!parseDocArray(cl.constructors(),results))
                {
                    ThisRoot.printError("Error while parsing constructors of class '"+cl.qualifiedTypeName()+"'");
                }
                else
                {
                    /**
                     * Parse the method level docs
                     */
                    if (!parseDocArray(cl.methods(),results))
                    {
                        ThisRoot.printError("Error while parsing methods of class '"+cl.qualifiedTypeName()+"'");
                    }
                    else
                    {
                        /**
                         * Parse the field level docs
                         */
                        if (!parseDocArray(cl.fields(),results))
                        {
                            ThisRoot.printError("Error while parsing fields of class '"+cl.qualifiedTypeName()+"'");
                        }
                    }
                }
            }

            if (PerClassGeneration)
            {
                writeResourceBundleFile(results, generatePerClassResourceFile(classes[count]));
                results = new StringBuffer();
            }
        }

        return(results);
    }

    /**
     * Given a class doc object this method generates the resource bundle
     * filename.
     */
    protected static File generatePerClassResourceFile(ClassDoc c)
    {
        String filename = DestinationDirectory + File.separatorChar + c.qualifiedTypeName().replace('.',File.separatorChar)+"_msg";

        if (Language != null)
        {
            filename += "_" + Language;
            if (Locale != null)
            {
                filename += "_"+Locale;
            }
        }

        if (PropertiesPostfix)
        {
            filename += ".properties";
        }

        return(new File(filename));
    }

    /**
     * This method writes the given key/text property pairs into a file
     */
    protected static boolean writeResourceBundleFile(StringBuffer properties, File filename)
    {
        boolean result = true;

        if ( properties.length() > 0 )
        {
            try
            {
                ThisRoot.printNotice("Generating '"+filename+"'");
                /**
                 * Create the directories if necessary
                 */
                filename.getParentFile().mkdirs();

                BufferedWriter out = new BufferedWriter(new FileWriter(filename));
                out.write(HEADER + '\n');
                out.write(properties.toString() + "\n");
                out.close();
            }
            catch (java.io.IOException e)
            {
                ThisRoot.printError("Error while generating resource bundle - "+e);
                result = false;
            }
        }

        return(result);
    }

    /**
     * This method iterates through the array of docs and parses
     * them into the string buffer.  This method can be used for
     * parsing any Doc type array.
     */
    protected static boolean parseDocArray(Doc[] docs, StringBuffer buffer)
    {
        boolean result = true;

        for (int count=0;(count<docs.length)&&result;count++)
        {
            result &= parseDoc(docs[count], buffer);
        }

        return(result);
    }
    /**
     * For a given classdoc this method generates a string buffer containing
     * all the key to text mappings contained within this class.
     */
    protected static boolean parseDoc(Doc doc, StringBuffer buffer)
    {
        boolean returnValue = false;

        /**
         * Generate key/text pairs for the class
         */
        Tag[] tags = doc.tags(MESSAGE_TAG);
        returnValue = generatePropertyString(tags, buffer, false);

        /**
         * Generate key/text pairs for the class
         */
        Tag[] resourceTags = doc.tags(RESOURCE_TAG);
        return(returnValue && generatePropertyString(resourceTags, buffer, true));
    }

    /**
     * Given an array of Tag classes this method produces the key/text pairs
     * that are specified in the tags and places them in a string buffer
     */
    private static boolean generatePropertyString(Tag[] classTags, StringBuffer results, boolean isResource)
    {
        String key = null;
        boolean errorFound = false;

        for (int tagCount=0;tagCount<classTags.length;tagCount++)
        {
            /**
             * If the key has been used previously and we don't allow repetition then flag an error
             */
            key = getKey(classTags[tagCount]);
            if ( (!IgnoreRepetition) && (PropertyKeys.contains(key)) )
            {
                ThisRoot.printError("The key '"+key+"' has been defined multiple times");
            }
            else
            {
                String keyTextPair = null;

                if ( isResource )
                {
                    keyTextPair = generateKeyResourceTextPair(classTags[tagCount]);
                }
                else
                {
                    keyTextPair = generateKeyTextPair(classTags[tagCount]);
                }

                if (keyTextPair != null)
                {
                    results.append(keyTextPair);
                    results.append('\n');
                }
                else
                {
                    ThisRoot.printError("Non valid tag - "+classTags[tagCount].toString());
                    errorFound = true;
                }
            }

            PropertyKeys.add(key);
        }

        return(!errorFound);
    }

    /**
     * Generates a key=text pair from a message tag
     *
     * @param tag The tag to produce the pair from
     * @return The textual key-text pair
     */
    public static String generateKeyTextPair(Tag tag)
    {
        String returnValue = null;

        try
        {
            String txt = handleNewlines(tag.text());
            String key = txt.substring(0,txt.indexOf(' '));
            String value = txt.substring(txt.indexOf(' ') + 1);

            returnValue = key + "=" + (AppendKey?("["+key+"] "):"") + value;
        }
        catch (Exception e)
        {
            // Ignore this exception just return null
        }

        return(returnValue);
    }

    /**
     * Generates a key=text pair from a resource tag
     *
     * @param tag The tag to produce the pair from
     * @return The textual key-text pair
     */
    public static String generateKeyResourceTextPair(Tag tag)
    {
        String returnValue = null;

        try
        {
            String txt = handleNewlines(tag.text());
            String key = txt.substring(0,txt.indexOf(' '));
            String value = txt.substring(txt.indexOf(' ') + 1);

            returnValue = key + "=" + value;
        }
        catch (Exception e)
        {
            // Ignore this exception just return null
        }

        return(returnValue);
    }

    /**
     * Generate a key from a tag
     *
     * @param tag The tag to produce the key from.
     * @return The key value
     */
    public static String getKey(Tag tag)
    {
        String returnValue = null;

        try
        {
            returnValue = tag.text().substring(0,tag.text().indexOf(' '));
        }
        catch (Exception e)
        {
            // Ignore this exception just return null
        }

        return(returnValue);
    }
    
    /**
     * Replace occurances of the newline character with a space and a backslash.
     * @param tag The current tag value.
     * @return The modified tag value.
     */
    private static String handleNewlines(final String tag)
    {
        final String trimmed = tag.trim() ;
        int newlineIndex = trimmed.indexOf('\n') ;
        if (newlineIndex != -1)
        {
            final StringBuffer newTagBuffer = new StringBuffer() ;
            int fromIndex = 0 ;
            do
            {
                final int toIndex ;
                if ((newlineIndex > 1) && (trimmed.charAt(newlineIndex-1) == '\r'))
                {
                    toIndex = newlineIndex-1 ;
                }
                else
                {
                    toIndex = newlineIndex ;
                }
                
                newTagBuffer.append(trimmed.substring(fromIndex, toIndex)) ;
                newTagBuffer.append(" \\") ;
                
                fromIndex = toIndex ;
                     
                newlineIndex = trimmed.indexOf('\n', newlineIndex+1) ;
            }
            while (newlineIndex != -1) ;
            
            newTagBuffer.append(trimmed.substring(fromIndex)) ;
            return newTagBuffer.toString() ;
        }
        return trimmed ;
    }
}
