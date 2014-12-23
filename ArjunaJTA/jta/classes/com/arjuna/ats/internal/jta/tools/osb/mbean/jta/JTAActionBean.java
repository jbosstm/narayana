/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;

/**
 * JTA specific version of an ActionBean that knows when a participant record
 * corresponds to an XAResource
 *
 * @author Mike Musgrove
 */
/**
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTAActionBean extends ActionBean {
    public JTAActionBean(UidWrapper w) {
        super(w);
    }

    @Override
    protected LogRecordWrapper createParticipant(AbstractRecord rec, ParticipantStatus listType) {
        if (rec instanceof com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord)
            return new com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XAResourceRecordBean(this, rec, listType);
        else if (rec instanceof com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord)
            return new com.arjuna.ats.internal.jta.tools.osb.mbean.jta.CommitMarkableResourceRecordBean(this, rec, listType);
        else
            return super.createParticipant(rec, listType);
    }
}
