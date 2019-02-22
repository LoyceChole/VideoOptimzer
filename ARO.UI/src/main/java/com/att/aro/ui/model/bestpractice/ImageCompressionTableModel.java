/*
 *  Copyright 2017 AT&T
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
package com.att.aro.ui.model.bestpractice;

import static java.text.MessageFormat.format;

import java.text.DecimalFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.att.aro.core.bestpractice.pojo.ImageCompressionEntry;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the data table model for text Image Size result table. This
 * class implements the aro.commonui.DataTableModel class using ImageSizeEntry
 * objects.
 */
public class ImageCompressionTableModel extends DataTableModel<ImageCompressionEntry> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LogManager.getLogger(ImageCompressionTableModel.class.getName());

	private static final int COL1_MIN = 70;
	private static final int COL1_MAX = 100;
	private static final int COL1_PREF = 70;

	private static final int COL2_MIN = 300;
	private static final int COL2_PREF = 300;

	private static final int COL3_MIN = 80;
	private static final int COL3_MAX = 120;
	private static final int COL3_PREF = 80;

	private static final int COL4_MIN = 80;
	private static final int COL4_MAX = 120;
	private static final int COL4_PREF = 80;
	
	private static final int COL5_MIN = 80;
	private static final int COL5_MAX = 120;
	private static final int COL5_PREF = 80;

	private static final int COL_1 = 0;
	private static final int COL_2 = 1;
	private static final int COL_3 = 2;
	private static final int COL_4 = 3;
	private static final int COL_5 = 4;
	private static final String[] COLUMNS = { ResourceBundleHelper.getMessageString("imageCompression.table.col1"),
											  ResourceBundleHelper.getMessageString("imageCompression.table.col2"),
											  ResourceBundleHelper.getMessageString("imageCompression.table.col3"),
											  ResourceBundleHelper.getMessageString("imageCompression.table.col4"),
											  ResourceBundleHelper.getMessageString("imageCompression.table.col5")};

	/**
	 * Initializes a new instance of the ImageSizeTableModel.
	 */
	public ImageCompressionTableModel() {
		super(ImageCompressionTableModel.COLUMNS);
	}

	/**
	 * Returns a TableColumnModel that is based on the default table column
	 * model for the DataTableModel class. The TableColumnModel returned by this
	 * method has the same number of columns in the same order and structure as
	 * the table column model in the DataTableModel. When a DataTable object is
	 * created, this method is used to create the TableColumnModel if one is not
	 * specified. This method may be overridden in order to provide
	 * customizations to the default column model, such as providing a default
	 * column width and/or adding column renderers and editors.
	 * 
	 * @return TableColumnModel object
	 */
	@Override
	public TableColumnModel createDefaultTableColumnModel() {
		TableColumnModel cols = super.createDefaultTableColumnModel();
		TableColumn col;

		col = cols.getColumn(ImageCompressionTableModel.COL_1);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
		col.setMinWidth(ImageCompressionTableModel.COL1_MIN);
		col.setPreferredWidth(ImageCompressionTableModel.COL1_PREF);
		col.setMaxWidth(ImageCompressionTableModel.COL1_MAX);

		col = cols.getColumn(ImageCompressionTableModel.COL_2);
		col.setMinWidth(ImageCompressionTableModel.COL2_MIN);
		col.setPreferredWidth(ImageCompressionTableModel.COL2_PREF);
		//col.setMaxWidth(ImageCompressionTableModel.COL2_MAX);

		col = cols.getColumn(ImageCompressionTableModel.COL_3);
		col.setMinWidth(ImageCompressionTableModel.COL3_MIN);
		col.setPreferredWidth(ImageCompressionTableModel.COL3_PREF);
		col.setMaxWidth(ImageCompressionTableModel.COL3_MAX);

		col = cols.getColumn(ImageCompressionTableModel.COL_4);
		col.setMinWidth(ImageCompressionTableModel.COL4_MIN);
		col.setPreferredWidth(ImageCompressionTableModel.COL4_PREF);
		col.setMaxWidth(ImageCompressionTableModel.COL4_MAX);
		
		col = cols.getColumn(ImageCompressionTableModel.COL_5);
		col.setMinWidth(ImageCompressionTableModel.COL5_MIN);
		col.setPreferredWidth(ImageCompressionTableModel.COL5_PREF);
		col.setMaxWidth(ImageCompressionTableModel.COL5_MAX);

		return cols;
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		ImageCompressionTableModel.LOGGER.debug(format("getColumnClass, idx: {0}", columnIndex));
		switch (columnIndex) {
		case COL_1:
			return Double.class;
		case COL_2:
			return String.class;
		case COL_3:
			return Double.class;
		case COL_4:
			return Double.class;
		case COL_5:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	/**
	 * Defines how the data object managed by this table model is mapped to its
	 * columns when displayed in a row of the table.
	 * 
	 * @param item
	 *            An object containing the column information.
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return The table column value calculated for the object.
	 */
	@Override
	protected Object getColumnValue(ImageCompressionEntry item, int columnIndex) {
		ImageCompressionTableModel.LOGGER.trace(format("getColumnValue, idx:{0}", columnIndex));
		switch (columnIndex) {
		case COL_1:
			return item.getTimeStamp();
		case COL_2:
			return item.getHttpObjectName();
		case COL_3:
			return item.getOrgImageSize();
		case COL_4:
			return item.getSizeE();
		case COL_5:
		return item.getSizeS();
		default:
			return null;
		}
	}

}
