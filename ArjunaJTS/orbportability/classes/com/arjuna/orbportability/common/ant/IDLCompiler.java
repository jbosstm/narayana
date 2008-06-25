/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: IDLCompiler.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.orbportability.common.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;

import java.util.*;

import java.io.InputStream;
import java.io.FileOutputStream;

import java.io.File;

import org.w3c.dom.*;

import javax.xml.parsers.*;

/**
 * Ant task to compile IDL across multiple ORB's.
 *
 * This task uses a definitions file (idl-compiler-definitions.xml) which defines how to call each of the supported IDL compilers.
 * For each compiler  you specify  a  number  of parameters  and whether  they are  required  for  this  IDL compiler.   You  then
 * call this ant task and pass it a comma separated list of parameters which it then maps to the IDL compiler specific parameters.
 *
 * @author Richard A. Begg
 */
public class IDLCompiler extends Task
{
    private final static String	NAME_ELEMENT = "name";
    private final static String VALUE_ELEMENT = "value";

    protected String _parameters = null;
    protected String _orb = null;
    protected Hashtable _idlCompilerDefinitions = null;
    protected String _filename = null;
    protected String _destinationDirectory = null;
    protected String _packageName = null;
    protected String _buildList = null;
    protected boolean _debugOn = false;
    protected boolean _verbose = false;

    protected FileSet _fileSet = null;

    public IDLCompiler()
    {
        parseIDLCompilerDefinitionsFile();
    }

    public final void setDebug(String debug)
    {
        _debugOn = new Boolean(debug).booleanValue();
    }

    public final void setVerbose(String verbose)
    {
        _verbose = new Boolean(verbose).booleanValue();
    }

    public final void setDestdir(String destDir)
    {
        _destinationDirectory = destDir;
    }

    public void setBuildlist(String buildList)
    {
        _buildList = buildList;
    }

    public final void setPackage(String packageName)
    {
        _packageName = packageName;
    }

    public final void setParameters(String parameters)
    {
        _parameters = parameters;
    }

    public final void setFilename(String filename)
    {
        _filename = filename;
    }

    public final void setOrb(String orb)
    {
        _orb = orb;
    }

    protected boolean childrenContainsSubParam(Node n)
    {
        NodeList children = n.getChildNodes();
        boolean found = false;

        for (int count=0;(count<children.getLength())&&(!found);count++)
        {
            found |= (children.item(count).getNodeName().equalsIgnoreCase("sub-param"));
        }

        return(found);
    }

    /**
     * Parse the IDL compiler definitions file.
     */
    protected void parseIDLCompilerDefinitionsFile() throws BuildException
    {
        _idlCompilerDefinitions = new Hashtable();
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(false);

            DocumentBuilder db = dbf.newDocumentBuilder();

            /**
             * Get input stream for the configuration XML file
             */
            InputStream configIn = getClass().getResourceAsStream("/idl-compiler-definitions.xml");

            /**
             * If the configuration file isn't in the classpath the input stream will be null
             */
            if ( configIn == null )
            {
                throw new BuildException("Cannot find idl-compiler-definitions.xml in your classpath");
            }

            Document doc = db.parse(configIn);

            Element root = doc.getDocumentElement();
            NodeList supportedOrbs = root.getChildNodes();

            for (int count = 0; count < supportedOrbs.getLength(); count++)
            {
                Node orbConfig = supportedOrbs.item(count);

                if (orbConfig.getNodeName().equalsIgnoreCase("orb"))
                {
                    IDLCompilerDetails idlDetails = new IDLCompilerDetails();
                    idlDetails.setORBName(orbConfig.getAttributes().getNamedItem("name").getNodeValue());

                    NodeList orbDef = orbConfig.getChildNodes();

                    for (int orbDefCount = 0; orbDefCount < orbDef.getLength(); orbDefCount++)
                    {
                        Node orbDefNode = orbDef.item(orbDefCount);
                        String nodeName = orbDefNode.getNodeName();

                        if (nodeName.equalsIgnoreCase("idl-executable"))
                        {
                            idlDetails.setIDLExecutable(orbDefNode.getFirstChild().getNodeValue());
                        }
                        else
                            if (nodeName.equalsIgnoreCase("in-parameters"))
                            {
                                NodeList inParameters = orbDefNode.getChildNodes();

                                for (int inCount = 0; inCount < inParameters.getLength(); inCount++)
                                {
                                    Node inParamNode = inParameters.item(inCount);

                                    if (inParamNode.getNodeName().equals("param"))
                                    {
                                        NamedNodeMap paramAttributes = inParamNode.getAttributes();

                                        String name = paramAttributes.getNamedItem("name").getNodeValue();
                                        boolean required = Boolean.valueOf(paramAttributes.getNamedItem("required").getNodeValue()).booleanValue();
                                        String delimiter = null;
					String replace = null;
					boolean isClasspath = false;
					Node isClasspathNode = paramAttributes.getNamedItem("classpath");
                                        Node delimiterNode = paramAttributes.getNamedItem("delimiter");
					Node replaceNode = paramAttributes.getNamedItem("replace");

                                        if ( delimiterNode != null )
                                        {
                                            delimiter = delimiterNode.getNodeValue();
                                        }

					if ( replaceNode != null )
					{
					    replace = replaceNode.getNodeValue();
					}

                                        if ( isClasspathNode != null )
					{
					    isClasspath = new Boolean(isClasspathNode.getNodeValue()).booleanValue();
					}

                                        idlDetails.addInParameter(name, required, delimiter, isClasspath, replace);
                                    }
                                }
                            }
                            else
                                if (nodeName.equalsIgnoreCase("out-parameters"))
                                {
                                    NodeList outParameters = orbDefNode.getChildNodes();

                                    for (int inCount = 0; inCount < outParameters.getLength(); inCount++)
                                    {
                                        Node outParamNode = outParameters.item(inCount);

                                        if (outParamNode.getNodeName().equals("param"))
                                        {
					    Node forEachNode = outParamNode.getAttributes().getNamedItem("foreach");
                                            if (childrenContainsSubParam(outParamNode))
                                            {
                                                NodeList children = outParamNode.getChildNodes();

                                                ArrayList multiParam = new ArrayList();
                                                for (int subParamCount=0;subParamCount<children.getLength();subParamCount++)
                                                {
                                                    Node child = children.item(subParamCount);

                                                    if (child.getNodeName().equalsIgnoreCase("sub-param"))
                                                    {
                                                        multiParam.add(child.getFirstChild().getNodeValue());
                                                    }
                                                }

						if ( forEachNode != null )
						{
						    idlDetails.addOutParameter(multiParam,forEachNode.getNodeValue());
						}
						else
						{
						    idlDetails.addOutParameter(multiParam);
						}
                                            }
                                            else
                                            {
                                                if ( forEachNode != null )
                                                {
                                                    idlDetails.addOutParameter(outParamNode.getFirstChild().getNodeValue(),forEachNode.getNodeValue());
                                                }
                                                else
                                                {
                                                    idlDetails.addOutParameter(outParamNode.getFirstChild().getNodeValue());
                                                }
                                            }
                                        }
                                    }
                                }
                    }

                    /**
                     * Add the IDL compiler definition to the map
                     */
                    _idlCompilerDefinitions.put(idlDetails.getORBName(), idlDetails);
                }
            }
        }
        catch (Exception e)
        {
            throw new BuildException("An unexpected exception occurred while processing the idl compiler definitions file (" + e + ")");
        }
    }

    private String[] createExecArray(String data)
    {
        int count = 0;
        StringTokenizer st = new StringTokenizer(data);
        String[] results = new String[st.countTokens()];

        while (st.hasMoreTokens())
        {
            results[count++] = st.nextToken();
        }

        return (results);
    }

    protected String stripDirectory(String filename)
    {
        String result = new File(filename).getParent();

        return ((result == null)?"":result);
    }

    public void execute() throws BuildException
    {
        if (_orb == null)
        {
            throw new BuildException("You have not specified the name of the ORB to build against.");
        }

        IDLCompilerDetails idlCompiler = (IDLCompilerDetails) _idlCompilerDefinitions.get(_orb);

        if (idlCompiler == null)
        {
            throw new BuildException("The IDL compiler for '" + _orb + "' is not defined within the configuration file");
        }

        if (_buildList != null)
        {
            log("Parsing...");
            BuildListParser blp = new BuildListParser(_buildList);

            if ((_buildList != null) && (!blp.isValid()) && (_buildList.length() > 0))
            {
                throw new BuildException("The IDL build list is invalid!");
            }

            while (blp.getNextElement())
            {
                String source = blp.getSource();
                String packageName = blp.getPackage();
                String mappings = blp.getMappings();

                if (!processIDL(idlCompiler, source, packageName, _destinationDirectory, mappings))
                {
                    throw new BuildException("Failed to compile '" + source + "' file");
                }
            }
        }
        else
        {
            if (_filename == null)
            {
                throw new BuildException("No source filename has been given");
            }

            if (_packageName == null)
            {
                throw new BuildException("No package name has been given");
            }

            log("Processing '" + _filename + "'");

            if (!processIDL(idlCompiler, _filename, _packageName, _destinationDirectory))
            {
                throw new BuildException("Failed to compile '" + _filename + "' file");
            }
        }
    }

    public boolean processIDL(IDLCompilerDetails idlCompiler, String filename, String packageName, String destDir)
    {
        return processIDL(idlCompiler, filename, packageName, "");
    }

    public boolean processIDL(IDLCompilerDetails idlCompiler, String filename, String packageName, String destDir, String mappings)
    {
        boolean returnValue = false;

        log("Processing for " + idlCompiler.getORBName() + " '" + filename + "'");

        if (_verbose)
        {
            log("Package: " + packageName);
            log("Parameters: " + _parameters);
            log("Mappings: " + mappings);
            log("Destination Directory: " + destDir);
        }

        try
        {
            String[] idlParameters = idlCompiler.parse(filename, destDir, packageName, _parameters, mappings);
            String[] idlExec = createExecArray(idlCompiler.getCompilerExecutable());
            String[] execParameters = new String[idlExec.length + idlParameters.length];

            System.arraycopy(idlExec, 0, execParameters, 0, idlExec.length);
            System.arraycopy(idlParameters, 0, execParameters, idlExec.length, idlParameters.length);

            Runtime rt = Runtime.getRuntime();

            if (_debugOn)
            {
                for (int count = 0; count < execParameters.length; count++)
                    log("Param[" + count + "] = '" + execParameters[count] + "'");
            }

            Process idlProc = rt.exec(execParameters);

            if (idlProc == null)
            {
                throw new BuildException("Failed to run IDL compiler");
            }

            new InputStreamFileWriter(idlProc.getInputStream(), "idl-compiler.out");
            new InputStreamFileWriter(idlProc.getErrorStream(), "idl-compiler.err");

            returnValue = (idlProc.waitFor() == 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new BuildException("The following exception occurred while executing the IDL compiler: " + e);
        }

        return (returnValue);
    }

    public class IDLCompilerDetails
    {
        protected String _orbName;
        protected String _compilerExe;
        protected Hashtable _inParameters;
        protected ArrayList _outParameters;
        protected Hashtable _parameterValueMap = new Hashtable();
        protected Hashtable _forEachMapping = new Hashtable();

        public IDLCompilerDetails()
        {
            _inParameters = new Hashtable();
            _outParameters = new ArrayList();
        }

        public void setIDLExecutable(String idlExe)
        {
            _compilerExe = idlExe;
        }

        public void setORBName(String name)
        {
            _orbName = name;
        }

        public String getORBName()
        {
            return (_orbName);
        }

        public String getCompilerExecutable() throws Exception
        {
            return (replaceVariables(_compilerExe, _parameterValueMap, new HashSet()));
        }

        public void addInParameter(String param, boolean required, String delimiter, boolean isClasspath, String replace)
        {
            _inParameters.put(param, new InParameterDetails(param, required, delimiter, isClasspath, replace));
        }

        public InParameterDetails getInParameter(String param)
        {
            return ((InParameterDetails) _inParameters.get(param));
        }

        public void addOutParameter(String param)
        {
            _outParameters.add(param);
        }

        public void addOutParameter(String param, String forEach)
        {
            _outParameters.add(param);
            setForEach(param, forEach);
        }

        public void setForEach(String param, String forEach)
        {
            _forEachMapping.put(param, forEach);
        }

        public void addOutParameter(ArrayList params)
        {
            _outParameters.add(params);
        }

        public void addOutParameter(ArrayList params, String forEach)
        {
            _outParameters.add(params);
            _forEachMapping.put(params, forEach);
        }

        protected InParameterDetails setAndFlagParameter(String param, String value, Hashtable map)
        {
            if (value != null)
            {
                InParameterDetails inParamDef = getInParameter(param);

                if (inParamDef == null)
                {
                    throw new BuildException("The parameter '" + param + "' used in the parameter attribute is not defined for this ORB");
                }

                if (inParamDef.hasDelimiter())
                {
                    StringTokenizer st = new StringTokenizer(value,inParamDef.getDelimiter());
                    ArrayList values = new ArrayList();

                    while (st.hasMoreTokens())
                    {
                        values.add(st.nextToken());
                    }

                    map.put(param, values);
                }
                else
                {
                    map.put(param, value);
                }

                return (inParamDef);
            }
            else
            {
                map.put(param, value);
            }


            return (null);
        }

        /**
         * Passed the parameters line from the ANT build file, e.g.
         *
         *   parameter='value',parameter2='value2',...
         */
        public String[] parse(String filename, String destDir, String packageName, String parameters, String mappings) throws BuildException
        {
            HashSet usedParameters = new HashSet();
            ArrayList outputParameters = new ArrayList();
            String parameterPair;
            String parameter;
            String value;

            /**
             * Ensure filename is valid and contains data
             */
            if ( (filename != null) && (filename.length() > 0) )
            {
                usedParameters.add(setAndFlagParameter("filename", filename, _parameterValueMap));
            }
            else
            {
                /**
                 * If the filename wasn't valid or didn't contain data then throw a buildexception
                 */
                throw new BuildException("The filename specified was invalid");
            }

            /**
             * Ensure destDir is valid and contains data
             */
            if ( (destDir != null) && (destDir.length() > 0) )
            {
                usedParameters.add(setAndFlagParameter("destdir", destDir, _parameterValueMap));
            }

            /**
             * Ensure packageName is valid and contains data
             */
            if ( (packageName != null) && (packageName.length() > 0) )
            {
                usedParameters.add(setAndFlagParameter("package", packageName, _parameterValueMap));
            }

            /**
             * Ensure mappings is valid and contains data
             */
            if ( (mappings != null) && (mappings.length() > 0) )
            {
                usedParameters.add(setAndFlagParameter("mapping", mappings, _parameterValueMap));
            }

            if (parameters != null)
            {
                /**
                 * Parse passed in parameters
                 */
                while (parameters.indexOf(',') != -1)
                {
                    parameterPair = parameters.substring(0, parameters.indexOf(','));
                    parameter = parameterPair.substring(0, parameterPair.indexOf('='));
                    value = parameterPair.substring(parameterPair.indexOf('='));
                    value = value.substring(value.indexOf('\'') + 1);
                    value = value.substring(0, value.indexOf('\''));

                    InParameterDetails inParamDef = getInParameter(parameter);

                    if (inParamDef == null)
                    {
                        throw new BuildException("The parameter '" + parameter + "' used in the parameter attribute is not defined for this ORB");
                    }

                    if (usedParameters.contains(inParamDef))
                    {
                        throw new BuildException("The parameter '" + parameter + "' has been defined twice");
                    }

                    /**
                     * Add parameter to usedParameters array
                     */
                    usedParameters.add(inParamDef);

                    ArrayList values = new ArrayList();
                    _parameterValueMap.put(parameter, values);

                    if (inParamDef.hasDelimiter())
                    {
                        StringTokenizer st = new StringTokenizer(value,inParamDef.getDelimiter());

                        while (st.hasMoreTokens())
                        {
                            values.add(st.nextToken());
                        }
                    }
                    else
                    {
                        values.add(value);
                    }

                    parameters = parameters.substring(parameters.indexOf(',') + 1);
                }

                parameter = parameters.substring(0, parameters.indexOf('='));
                value = parameters.substring(parameters.indexOf('='));
                value = value.substring(value.indexOf('\'') + 1);
                value = value.substring(0, value.indexOf('\''));

                InParameterDetails inParamDef = getInParameter(parameter);

                if (inParamDef == null)
                {
                    throw new BuildException("The parameter '" + parameter + "' used in the parameter attribute is not defined for this ORB");
                }

                if (usedParameters.contains(inParamDef))
                {
                    throw new BuildException("The parameter '" + parameter + "' has been defined twice");
                }

                usedParameters.add(inParamDef);

                ArrayList values = new ArrayList();
                _parameterValueMap.put(parameter, values);

                if ( ( inParamDef.hasDelimiter() ) && ( !inParamDef.isClasspath() ) )
                {
                    StringTokenizer st = new StringTokenizer(value,inParamDef.getDelimiter());

                    while (st.hasMoreTokens())
                    {
                        values.add(st.nextToken());
                    }
                }
                else
                {
		    if ( inParamDef.isClasspath() )
		    {
			if ( !inParamDef.hasDelimiter() )
			{
				throw new BuildException("Param defined as classpath but delimiter defined");
			}

			value = createClasspath( value, inParamDef.getDelimiter() );
		    }

		    if ( inParamDef.getReplaceString() != null )
		    {
			String replaceString = inParamDef.getReplaceString();
			String find = replaceString.substring( 0, replaceString.indexOf(',') );
			String replaceWith = replaceString.substring( replaceString.indexOf(',') + 1 );

			while ( value.indexOf(find) != -1 )
			{
			    value = value.substring(0, value.indexOf(find)) + replaceWith + value.substring( value.indexOf(find) + find.length() );
			}
		    }

                    values.add(value);
                }
            }

            for (Enumeration e = _inParameters.elements(); e.hasMoreElements();)
            {
                InParameterDetails ipd = (InParameterDetails) e.nextElement();

                if (ipd.isRequired() && !usedParameters.contains(ipd))
                {
                    throw new BuildException("The parameter '" + ipd.getParameter() + "' is defined as being required but has not been specified");
                }
            }

            for (int count = 0; count < _outParameters.size(); count++)
            {
                if ( _outParameters.get(count) instanceof ArrayList )
                {
                    ArrayList subParams = (ArrayList) _outParameters.get(count);

                    ArrayList multiParam = new ArrayList();
                    try
                    {
                        generateOutputParameters(subParams, usedParameters, multiParam);

                        outputParameters.addAll(multiParam);
                    }
		    catch (BuildException e)
		    {
			throw e;
		    }
                    catch (Exception e)
                    {
			// If one of the parameters in the multi-param block is not defined
                        // then ignore this block
                    }
                }
                else
                {
                    try
                    {
                        generateOutputParameters(_outParameters.get(count), usedParameters, outputParameters);
                    }
		    catch (BuildException e)
		    {
			throw e;
		    }
                    catch (Exception e)
                    {
                        // If one of the parameters in the multi-param block is not defined
                        // then ignore this block
                    }
                }
            }

            String[] returnArray = new String[outputParameters.size()];
            System.arraycopy(outputParameters.toArray(), 0, returnArray, 0, outputParameters.size());

            return (returnArray);
        }

		private String createClasspath( String value, String delimiter )
		{
			String returnValue = "";
			StringTokenizer st = new StringTokenizer(value, delimiter);

			while ( st.hasMoreTokens() )
			{
				returnValue += (String)st.nextToken();

				if ( st.hasMoreTokens() )
				{
					returnValue += File.pathSeparatorChar;
				}
			}

			return returnValue;
		}

        private void generateOutputParameters(Object forEachKey, HashSet usedParameters, ArrayList outputParameters) throws Exception
        {
            String forEach = (String)_forEachMapping.get( forEachKey );

            if (forEach == null)
            {
		if ( forEachKey instanceof ArrayList )
		{
		    ArrayList al = (ArrayList)forEachKey;
		    for (int count=0;count<al.size();count++)
		    {
		    	outputParameters.add(replaceVariables((String) al.get(count), _parameterValueMap, usedParameters));
		    }
		}
		else
		{
                    outputParameters.add(replaceVariables((String) forEachKey, _parameterValueMap, usedParameters));
		}
            }
            else
            {
                Object obj = _parameterValueMap.get( forEach );

                if (obj instanceof ArrayList)
                {
                    ArrayList paramList = (ArrayList)obj;
                    /**
                     * Ensure this parameter has actually been specified
                     */
                    if ( paramList != null )
                    {
                        Hashtable newParamValueMap = new Hashtable(_parameterValueMap);

                        for (int paramCount=0;paramCount<paramList.size();paramCount++)
                        {
                            newParamValueMap.put( forEach, paramList.get(paramCount) );
			    if ( !( forEachKey instanceof ArrayList ) )
			    {
                            	outputParameters.add(replaceVariables((String) forEachKey, newParamValueMap, usedParameters));
			    }
			    else
			    {
				ArrayList forEachArrayList = (ArrayList)forEachKey;

				for (int count=0;count<forEachArrayList.size();count++)
				{
					outputParameters.add(replaceVariables((String) forEachArrayList.get(count), newParamValueMap, usedParameters));
				}
			    }
                        }
                    }
                }
                else
                {
                    Hashtable newParamValueMap = new Hashtable(_parameterValueMap);

                    newParamValueMap.put( forEach, obj );
                    outputParameters.add(replaceVariables((String) forEachKey, newParamValueMap, usedParameters));
                }
            }
        }

        public String replaceVariables(String data, Hashtable variableValueMap, HashSet usedParameters) throws Exception
        {

            while (data.indexOf("${") != -1)
            {
                String variableName = data.substring(data.indexOf("${") + 2);
		String variableElement = null;

		if ( ( variableName.indexOf('-') != -1 ) && ( variableName.indexOf('-') < variableName.indexOf('}') ) )
		{
		    variableElement = variableName.substring( variableName.indexOf('-') + 1, variableName.indexOf('}'));
		    variableName = variableName.substring(0, variableName.indexOf('-'));
		}
		else
		{
                    variableName = variableName.substring(0, variableName.indexOf('}'));
		}
                String value = null;
                Object obj = variableValueMap.get(variableName);

                if (obj instanceof ArrayList)
                {
                    value = (String)((ArrayList)obj).get(0);
                }
                else
                {
                    value = (String)obj;
                }

		if ( ( value != null ) && ( variableElement != null ) )
		{
		    if ( variableElement.equalsIgnoreCase(NAME_ELEMENT) )
		    {
			value = value.substring(0, value.indexOf('='));
		    }
		    else
		    {
			if ( variableElement.equalsIgnoreCase(VALUE_ELEMENT) )
			{
			    value = value.substring(value.indexOf('=')+1);
			}
			else
			{
			    throw new BuildException("Unknow variable element '"+variableElement+"'");
			}
		    }
		}

                if (value == null)
                {
                    InParameterDetails ipd = getInParameter(variableName);

                    if ((ipd != null) && (!usedParameters.contains(ipd)))
                    {
                        throw new Exception("Unspecified optional parameter used - ignore this parameter line");
                    }

                    value = (variableName);
                }

                data = data.substring(0, data.indexOf("${")) + value + data.substring(data.indexOf('}') + 1);
            }

            return (data);
        }

        private class InParameterDetails
        {
            public String 	_parameter;
            public boolean 	_required;
            public String 	_delimiter;
	    public boolean 	_isClasspath;
	    public String	_replaceString;

            public InParameterDetails(String param, boolean required, String delimiter, boolean isClasspath, String replaceString)
            {
                _parameter = param;
                _required = required;
                _delimiter = delimiter;
		_isClasspath = isClasspath;
		_replaceString = replaceString;
            }

            public String getParameter()
            {
                return (_parameter);
            }

	    public boolean isClasspath()
	    {
		return _isClasspath;
	    }

            public boolean isRequired()
            {
                return (_required);
            }

            public String getDelimiter()
            {
                return (_delimiter);
            }

	    public String getReplaceString()
	    {
		return _replaceString;
	    }

            public boolean hasDelimiter()
            {
                return (_delimiter != null);
            }
        }
    }

    public class InputStreamFileWriter extends Thread
    {
        InputStream _inStream = null;
        FileOutputStream _outStream = null;

        public InputStreamFileWriter(InputStream inStr, String filename) throws java.io.FileNotFoundException, java.io.IOException
        {
            /*
             * Create the directories required to be able to create this file
             */
            File inFile = new File(filename);
            String directory = filename.substring(0, filename.indexOf(inFile.getName()));
            File inDir = new File(directory);
            inDir.mkdirs();

            _inStream = inStr;
            _outStream = new FileOutputStream(inFile);

            start();
        }

        public void run()
        {
            try
            {
                // Create 32k buffer
                byte[] buffer = new byte[32768];
                int bytesRead;

                while ((bytesRead = _inStream.read(buffer)) != -1)
                {
                    if (bytesRead != 0)
                    {
                        _outStream.write(buffer, 0, bytesRead);
                        _outStream.flush();
                    }
                }

                _outStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        public void close() throws java.io.IOException
        {
            _outStream.close();
        }
    }

    public class BuildListParser
    {
        protected String _buildList;
        protected String _currentSrc;
        protected String _currentPackage;
        protected String _currentMapping;
        protected boolean _valid = false;

        public BuildListParser(String buildListString)
        {
            _buildList = buildListString;
            _valid = _buildList.indexOf('<') < _buildList.indexOf('>');
        }

        public boolean isValid()
        {
            return (_valid);
        }

        private boolean getNextElement()
        {
            boolean returnValue = false;

            if ((_valid) && (_buildList.indexOf('<') != -1))
            {
                String element = _buildList.substring(_buildList.indexOf('<') + 1);

                element = element.substring(0, element.indexOf('>'));
                _currentSrc = element.substring(element.indexOf('\'') + 1);
                _currentSrc = _currentSrc.substring(0, _currentSrc.indexOf('\''));

                element = element.substring(element.indexOf(',') + 1);
                _currentPackage = element.substring(element.indexOf('\'') + 1);
                _currentPackage = _currentPackage.substring(0, _currentPackage.indexOf('\''));

                element = element.substring(element.indexOf(',') + 1);
                _currentMapping = element.substring(element.indexOf('\'') + 1);
                _currentMapping = _currentMapping.substring(0, _currentMapping.indexOf('\''));

                _buildList = _buildList.substring(1).substring(_buildList.indexOf('>'));

                returnValue = true;
            }

            return (returnValue);
        }

        public String getSource()
        {
            return (_currentSrc);
        }

        public String getPackage()
        {
            return (_currentPackage);
        }

        public String getMappings()
        {
            return (_currentMapping);
        }
    }
}
