/* JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General public  License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General public  License for more details.
 * You should have received a copy of the GNU Lesser General public  License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.nbf;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;

public class NBFToJavaBean {

    private static final String JBossLicence = "/*\n" + "\tJBoss, Home of Professional Open Source Copyright 2008, Red Hat\n"
            + "\tMiddleware LLC, and others contributors as indicated by the @authors\n"
            + "\ttag. All rights reserved. See the copyright.txt in the distribution\n"
            + "\tfor a full listing of individual contributors. This copyrighted\n"
            + "\tmaterial is made available to anyone wishing to use, modify, copy, or\n"
            + "\tredistribute it subject to the terms and conditions of the GNU Lesser\n"
            + "\tGeneral Public License, v. 2.1. This program is distributed in the\n"
            + "\thope that it will be useful, but WITHOUT A WARRANTY; without even the\n"
            + "\timplied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR\n"
            + "\tPURPOSE. See the GNU Lesser General Public License for more details.\n"
            + "\tYou should have received a copy of the GNU Lesser General Public\n"
            + "\tLicense, v.2.1 along with this distribution; if not, write to the Free\n"
            + "\tSoftware Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA\n" + "\t02110-1301, USA.\n" + "*/\n";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage:java org.jboss.narayana.blacktie.jatmibroker.nbf.NBFToJavaBean <xsd>");
            System.exit(1);
        }

        convert(args[0], false);
    }

    public static void convert(String fname, boolean rewrite) throws Exception {
        NBFSchemaParser parser = new NBFSchemaParser();

        if (parser.parse(fname)) {
            String bufferName;
            Map<String, String> flds;
            bufferName = parser.getBufferName();
            flds = parser.getFileds();

            String className = bufferName.substring(0, 1).toUpperCase() + bufferName.substring(1, bufferName.length())
                    + "Buffer";
            File toFile = new File(className + ".java");
            if (rewrite == false && toFile.exists()) {
                System.out.println(toFile + " exists, Do you want to cover it?[y/N]");
                int c = System.in.read();
                if (c != 'y' && c != 'Y') {
                    System.exit(0);
                }
            }

            FileOutputStream fos = new FileOutputStream(toFile.getName());
            fos.write(JBossLicence.getBytes());
            String header = "import org.jboss.narayana.blacktie.jatmibroker.xatmi.BT_NBF;\n"
                    + "import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;\n"
                    + "import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;\n\n";
            fos.write(header.getBytes());
            String content = "public class " + className + " {\n";
            content += "\tprivate Connection connection;\n";
            content += "\tprivate BT_NBF buffer;\n\n";
            content += "\tpublic " + className + "() throws Exception {\n";
            content += "\t\tConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();\n";
            content += "\t\tconnection = connectionFactory.getConnection();\n";
            content += "\t\tbuffer = (BT_NBF) connection.tpalloc(\"BT_NBF\", \"" + bufferName + "\", 0);\n";
            content += "\t}\n\n";

            flds = parser.getFileds();
            for (Entry<String, String> entry : flds.entrySet()) {
                String type = entry.getValue();
                String name = entry.getKey();

                if (type.equals("string")) {
                    type = "String";
                } else if (type.equals("long")) {
                    type = "Long";
                } else if (type.equals("int")) {
                    type = "Integer";
                } else if (type.equals("short")) {
                    type = "Short";
                } else if (type.endsWith("_type")) {
                    type = "BT_NBF";
                }
                content += "\tpublic " + type + " get" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length())
                        + "(int index) throws Exception {\n";
                content += "\t\treturn (" + type + ")buffer.btgetattribute(\"" + name + "\", index);\n";
                content += "\t}\n\n";

                content += "\tpublic void set" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length()) + "("
                        + type + " value, int index) throws Exception {\n";
                content += "\t\tbuffer.btsetattribute(\"" + name + "\", index, value);\n";
                content += "\t}\n\n";

                content += "\tpublic void add" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length()) + "("
                        + type + " value) throws Exception {\n";
                content += "\t\tbuffer.btaddattribute(\"" + name + "\", value);\n";
                content += "\t}\n\n";

                content += "\tpublic void remove" + name.substring(0, 1).toUpperCase() + name.substring(1, name.length())
                        + "(int index) throws Exception {\n";
                content += "\t\tbuffer.btdelattribute(\"" + name + "\", index);\n";
                content += "\t}\n\n";
            }
            content += "}\n";
            fos.write(content.getBytes());
            fos.close();
        }
    }

}
