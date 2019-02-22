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
package com.att.aro.ui.view.videotab;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

class WordWrapRenderer extends JTextArea implements TableCellRenderer{	
	private static final long serialVersionUID = 1L;
		JEditorPane editor;
		int rowSelected=-1;
		
		public WordWrapRenderer(){
			setLineWrap(true);
			setWrapStyleWord(true);
		}
		
		public WordWrapRenderer(int rowSelected){
			this();
			this.rowSelected = rowSelected;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String html = value.toString();
			setText(html);
			if (row == rowSelected) {
				this.setForeground(Color.gray);
			} else {
				this.setForeground(Color.blue);
			}
			setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
			if (table.getRowHeight(row) != getPreferredSize().height) {
				table.setRowHeight(row, getPreferredSize().height);
			}
			return this;
		}

	}