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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;

public class NBFSchemaParser {
    private static final Logger log = LogManager.getLogger(NBFSchemaParser.class);
    private String bufferName;
    private Map<String, String> flds;

    public NBFSchemaParser() {
        flds = new HashMap<String, String>();
    }

    private void findElementType(XSComplexType xtype) {
        XSContentType xsContentType = xtype.getContentType();
        XSParticle particle = xsContentType.asParticle();

        if (particle != null) {
            XSTerm term = particle.getTerm();
            if (term.isModelGroup()) {
                XSModelGroup xsModelGroup = term.asModelGroup();
                XSParticle[] particles = xsModelGroup.getChildren();
                for (XSParticle p : particles) {
                    XSTerm pterm = p.getTerm();
                    if (pterm.isElementDecl()) {
                        XSElementDecl element = pterm.asElementDecl();
                        String name = element.getName();
                        log.debug(name);
                        XSType type = element.getType();

                        while (type != null) {
                            String typeName = type.getName();
                            if (typeName != null
                                    && (typeName.equals("long") || typeName.equals("string") || typeName.equals("integer")
                                            || typeName.equals("float") || typeName.endsWith("_type"))) {
                                log.debug(typeName);
                                flds.put(name, typeName);
                                break;
                            }
                            type = type.getBaseType();
                        }
                    }
                }
            }
        }
    }

    public boolean parse(String fname) {
        boolean rc = false;

        try {
            flds.clear();
            XSOMParser parser = new XSOMParser();
            parser.parse(fname);
            XSSchemaSet xsSchema = parser.getResult();
            XSSchema schema = xsSchema.getSchema(1);
            File file = new File(fname);

            XSElementDecl element = schema.getElementDecl(file.getName().replace(".xsd", ""));

            if (element != null) {
                log.debug("element is " + element.getName());
                bufferName = element.getName();
                XSType xtype = element.getType();
                if (xtype.isComplexType()) {
                    findElementType(xtype.asComplexType());
                    rc = true;
                }
            }
        } catch (Exception e) {
            log.error("parse " + fname + " failed with " + e.getMessage(), e);
        }

        return rc;
    }

    public String getBufferName() {
        return bufferName;
    }

    public Map<String, String> getFileds() {
        return flds;
    }
}
