/*
 *  Copyright 2018 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.att.aro.core.util.PcapConfirmationImpl;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class PcapConfirmationDialog extends ConfirmationDialog {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle resourceBundle = ResourceBundleHelper.getDefaultBundle();

	private JLabel pcapMsgLabel;
	public PcapConfirmationImpl pcapImpl = ContextAware.getAROConfigContext().getBean("pcapConfirmationImpl",
			PcapConfirmationImpl.class);

	@Override
	public void createDialog() {
		setUndecorated(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(resourceBundle.getString("pcap.dialog.title"));
		setResizable(false);
		setBounds(400, 300, 400, 150);
		setPreferredSize(new Dimension(530, 220));
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(panel);

		GridBagConstraints constraint = new GridBagConstraints();
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridx = 1;
		constraint.gridy = 0;
		constraint.insets = new Insets(0, 10, 0, 0);
		constraint.weightx = 1;

		pcapMsgLabel = new JLabel(getLabelMsg());
		pcapMsgLabel.setFont(new Font("pcapLabel", Font.PLAIN, 12));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridBagLayout());
		labelPanel.add(pcapMsgLabel, constraint);
		panel.add(labelPanel);

		JPanel btnPanel = new JPanel(new BorderLayout());
		btnPanel.setBorder(BorderFactory.createEmptyBorder(1, 200, 1, 200));
		okBtn = new JButton("OK");
		okBtn.setFont(new Font("okBtn", Font.BOLD, 15));

		//okBtn.setPreferredSize(new Dimension(15, 10));
		btnPanel.add(okBtn);
		okBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		panel.add(btnPanel);

		pack();
		panel.setSize(panel.getPreferredSize());
		panel.validate();
	}

	@Override
	public String getLabelMsg() {
		return resourceBundle.getString("pcap.dialog.message");
	}

}
