/*
 *  SourceCodeCounter
 *  Copyright (C) 2009 Nick Sydenham <nsydenham@yahoo.co.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nrs.scc;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

/**
 * About dialog box for the app
 *
 * @author Copyright 2009 Nick Sydenham &lt;nsydenham@yahoo.co.uk&gt;
 */
public class SourceCodeCounterAboutBox extends JDialog
{
    public SourceCodeCounterAboutBox(Frame parent)
	{
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(closeButton);

		// set cursor on URL label
		appHomepageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// load licence
		try {
			URL licence = getClass().getResource("/nrs/scc/resources/gpl-3.0.txt");
			if (licence != null)
			{
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(licence.openStream()));
				String read;
				while ((read = reader.readLine()) != null)
				{
					sb.append(read);
					sb.append("\n");
				}
				reader.close();
				licenceTA.setText(sb.toString());
				licenceTA.setCaretPosition(0);
			}
		} catch (IOException ioex) {
			licenceTA.setText("Cannot load GPL 3 licence");
		}
    }

    @Action public void closeAboutBox()
	{
        setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new JButton();
        JLabel appTitleLabel = new JLabel();
		JLabel versionLabel = new JLabel();
		JLabel appVersionLabel = new JLabel();
		JLabel copyrightLabel = new JLabel();
		JLabel appCopyrightLabel = new JLabel();
		JLabel homepageLabel = new JLabel();
        appHomepageLabel = new JLabel();
		JLabel appDescLabel = new JLabel();
		JLabel imageLabel = new JLabel();
        licenceScroll = new JScrollPane();
        licenceTA = new JTextArea();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		ResourceMap resourceMap = Application.getInstance(SourceCodeCounter.class).getContext().getResourceMap(SourceCodeCounterAboutBox.class);
        setTitle(resourceMap.getString("title")); // NOI18N
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setModal(true);
        setName("aboutBox"); // NOI18N
        setResizable(false);

        ActionMap actionMap = Application.getInstance(SourceCodeCounter.class).getContext().getActionMap(SourceCodeCounterAboutBox.class, this);
        closeButton.setAction(actionMap.get("closeAboutBox")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | Font.BOLD, appTitleLabel.getFont().getSize()+4));
        appTitleLabel.setText(resourceMap.getString("Application.title")); // NOI18N
        appTitleLabel.setName("appTitleLabel"); // NOI18N

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | Font.BOLD));
        versionLabel.setText(resourceMap.getString("versionLabel.text")); // NOI18N
        versionLabel.setName("versionLabel"); // NOI18N

        appVersionLabel.setText(resourceMap.getString("Application.version")); // NOI18N
        appVersionLabel.setName("appVersionLabel"); // NOI18N

        copyrightLabel.setFont(copyrightLabel.getFont().deriveFont(copyrightLabel.getFont().getStyle() | Font.BOLD));
        copyrightLabel.setText(resourceMap.getString("copyrightLabel.text")); // NOI18N
        copyrightLabel.setName("copyrightLabel"); // NOI18N

        appCopyrightLabel.setText(resourceMap.getString("Application.vendor")); // NOI18N
        appCopyrightLabel.setName("appCopyrightLabel"); // NOI18N

        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | Font.BOLD));
        homepageLabel.setText(resourceMap.getString("homepageLabel.text")); // NOI18N
        homepageLabel.setName("homepageLabel"); // NOI18N

        appHomepageLabel.setForeground(resourceMap.getColor("appHomepageLabel.foreground")); // NOI18N
        appHomepageLabel.setText(resourceMap.getString("Application.homepage")); // NOI18N
        appHomepageLabel.setName("appHomepageLabel"); // NOI18N
        appHomepageLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                showHomepage(evt);
            }
        });

        appDescLabel.setText(resourceMap.getString("appDescLabel.text")); // NOI18N
        appDescLabel.setName("appDescLabel"); // NOI18N

        imageLabel.setIcon(resourceMap.getIcon("imageLabel.icon")); // NOI18N
        imageLabel.setName("imageLabel"); // NOI18N

        licenceScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resourceMap.getString("licenceScroll.border.title"))); // NOI18N
        licenceScroll.setName("licenceScroll"); // NOI18N

        licenceTA.setColumns(20);
        licenceTA.setEditable(false);
        licenceTA.setRows(5);
        licenceTA.setName("licenceTA"); // NOI18N
        licenceScroll.setViewportView(licenceTA);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(licenceScroll, GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(imageLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(versionLabel)
                                    .addComponent(copyrightLabel)
                                    .addComponent(homepageLabel))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(appHomepageLabel)
                                    .addComponent(appCopyrightLabel)
                                    .addComponent(appVersionLabel)))
                            .addComponent(appTitleLabel)
                            .addComponent(appDescLabel, GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(603, Short.MAX_VALUE)
                        .addComponent(closeButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(appTitleLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(appDescLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(versionLabel)
                            .addComponent(appVersionLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(copyrightLabel)
                            .addComponent(appCopyrightLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                            .addComponent(homepageLabel)
                            .addComponent(appHomepageLabel)))
                    .addComponent(imageLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(licenceScroll, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void showHomepage(MouseEvent evt)//GEN-FIRST:event_showHomepage
	{//GEN-HEADEREND:event_showHomepage
		Application app = Application.getInstance(SourceCodeCounter.class);
		ResourceMap rMap = app.getContext().getResourceMap(SourceCodeCounterAboutBox.class);
		String url = rMap.getString("appHomepageLabel.text");
		if (url != null)
		{
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
}//GEN-LAST:event_showHomepage
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    JLabel appHomepageLabel;
    JButton closeButton;
    JScrollPane licenceScroll;
    JTextArea licenceTA;
    // End of variables declaration//GEN-END:variables
    
}
