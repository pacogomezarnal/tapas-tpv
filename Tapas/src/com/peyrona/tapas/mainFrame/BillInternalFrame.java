/*
 * Copyright (C) 2010 Francisco José Morero Peyrona. All Rights Reserved.
 *
 * This file is part of Tapas project: http://code.google.com/p/tapas-tpv/
 *
 * GNU Classpath is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the free
 * Software Foundation; either version 3, or (at your option) any later version.
 *
 * Tapas is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Tapas; see the file COPYING.  If not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.peyrona.tapas.mainFrame;

import com.peyrona.tapas.Utils;
import com.peyrona.tapas.account.BillAndMenuPanel;
import com.peyrona.tapas.persistence.Bill;
import com.peyrona.tapas.persistence.DataProvider;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 * Cada una de las JInternalFrame en las que se se almacena la cuenta de cada cliente.
 *
 * @author Francisco Morero Peyrona
 */
final class BillInternalFrame extends JInternalFrame
{
    private static int nCount = 0;

    private LabelAmount lblAmount;
    private Bill        bill;

    //------------------------------------------------------------------------//

    BillInternalFrame()
    {
        this( null );
    }

    BillInternalFrame( Bill bill )
    {
        this.bill = (bill == null ? new Bill() : bill);
        lblAmount = new LabelAmount();
        lblAmount.setAmount( this.bill.getTotal() );

        removeWindowMenu();
        setResizable( false );
        setIconifiable( false );
        setMaximizable( false );
        setClosable( false );
        setFrameIcon( null );
        setCustomer( this.bill.getCustomer() );
        setLocation( (nCount*12)+4, (nCount*12)+4 );

        nCount = nCount > 30 ? 0 : nCount+1;

        JPanel panel = new JPanel( new BorderLayout( 0,7 ) );
               panel.setBorder( new EmptyBorder( 9,9,9,9 ) );
               panel.add( new ButtonUpdate(), BorderLayout.CENTER );
               panel.add( lblAmount         , BorderLayout.SOUTH );

        getContentPane().add( panel );
        pack();
    }

    //----------------------------------------------------------------------------//

    @Override
    public void setSelected( boolean bSelected )
    {
        try
        {
            super.setSelected( bSelected );
        }
        catch( PropertyVetoException exc )
        {
            /* Nada que hacer */
        }
    }

    //------------------------------------------------------------------------//

    private void setCustomer( String sCustomer )
    {
        // Cada vez que se abre una cuenta nueva, el sistema le da un número nuevo
        // (ordinal consecutivo), esto es un Nombre Automático, pero el usuario
        // puede darle a la cuenta un nombre más descriptivo: el del cliente p.ej.
        boolean isAutomaticName = true;

        // Comprueba si es el automático o no
        for( char c : sCustomer.toCharArray() )
        {
            if( ! Character.isDigit( c ) )
            {
                isAutomaticName = false;
                break;
            }
        }

        // Actrúa en consecuencia
        sCustomer = (isAutomaticName ? "Cuenta - "+ sCustomer : sCustomer);

        setTitle( sCustomer );
    }

    private void onCloseBill()
    {
        if( bill.getLines().size() > 0 )
        {
            DataProvider.getInstance().insertBill( bill );
            printTicket( bill );
        }

        dispose();
    }

    private void printTicket( Bill bill )
    {
        // TODO: implementarlo
    }

    private void removeWindowMenu()
    {
        BasicInternalFrameUI bifui = (BasicInternalFrameUI) getUI();
        Container north = (Container) bifui.getNorthPane();
                  north.remove(0);
                  north.validate();
                  north.repaint();
    }

    //------------------------------------------------------------------------//
    // INNER CLASS: El botón de la InternalFrame
    //------------------------------------------------------------------------//
    private final class ButtonUpdate extends JButton implements ActionListener
    {
        ButtonUpdate()
        {
            setMargin( new Insets( 9,0,9,0 ) );
            setFont( getFont().deriveFont( Font.BOLD, 16f ) );
            setText( "Modificar" );
            addActionListener( ButtonUpdate.this );
        }

        @Override
        public void actionPerformed( ActionEvent ae )
        {
            BillAndMenuPanel panel = new BillAndMenuPanel( BillInternalFrame.this.bill );
                                 panel.showInDialog();

            bill = panel.getBill();
            setCustomer( bill.getCustomer() ); // Por si ha cambiado (es más simple que comprobar si ha cambiado)
            lblAmount.setAmount( bill.getTotal() );

            if( bill.isClosed() )
            {
                onCloseBill();
            }
        }
    }

    //------------------------------------------------------------------------//
    // INNER CLASS: La etiqueta con la cantidad en la InternalFrame
    //------------------------------------------------------------------------//
    private final class LabelAmount extends JLabel
    {
        LabelAmount()
        {
            setFont( getFont().deriveFont( Font.BOLD, 16f ) );
            setHorizontalAlignment( JLabel.CENTER );
        }

        void setAmount( BigDecimal nAmount )
        {
            setText( Utils.formatAsCurrency( nAmount ) );
        }
    }
}