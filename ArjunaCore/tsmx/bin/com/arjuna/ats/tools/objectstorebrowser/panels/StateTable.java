/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package com.arjuna.ats.tools.objectstorebrowser.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

/**
 * JTable for displaying name value pairs.
 */
public class StateTable extends JTable
{
    boolean shadeHeaders = false;

    public StateTable(DefaultTableModel defaultTableModel)
    {
        super(defaultTableModel);
    }

    /**
     * Add a header and tooltips for the table.
     */
    public Component prepareRenderer(TableCellRenderer renderer,
                                     int rowIndex, int vColIndex)
    {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);

        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            jc.setToolTipText((String) getValueAt(rowIndex, vColIndex));

            if (shadeHeaders && rowIndex == 0)
                jc.setBackground(Color.LIGHT_GRAY);
            else
                jc.setBackground(Color.WHITE);
        }
        
        return c;
    }

    /**
     * Update the column widths to accomodate the data in the cells
     * (this will either shrink or expand the column depending
     * upon the with of the text in the cells)
     */
    public void updateColumnSizes()
    {
        TableModel model = getModel();
        TableColumnModel colModel = getColumnModel();
        FontMetrics metrics = getGraphics().getFontMetrics();
        int[] maxWidths = new int[model.getColumnCount()];

        // find the maximum width needed for each column
        for (int i = 0; i < model.getColumnCount(); i++)
        {
            for (int j = 0; j < model.getRowCount(); j++)
            {
                int width = metrics.stringWidth((String) model.getValueAt(j, i));

                if (maxWidths[i] < width)
                    maxWidths[i] = width;
            }
        }

        for (int i = 0; i < maxWidths.length; i++)
            colModel.getColumn(i).setPreferredWidth(maxWidths[i]);
    }

    /**
     * Set a flag to decide whether the first table row should be shaded.
     *
     * @param shadeHeaders if true the first row will be shaded
     */
    void shadeHeaders(boolean shadeHeaders)
    {
        this.shadeHeaders = shadeHeaders;
    }
}
