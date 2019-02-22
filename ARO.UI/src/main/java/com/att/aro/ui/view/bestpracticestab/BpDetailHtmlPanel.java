/*
 *  Copyright 2015 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License", BestPracticeType.);
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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JPanel;

import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.bestpractice.pojo.BestPracticeType.Category;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;

/**
 * 
 *
 *
 */
public class BpDetailHtmlPanel extends BpDetail {

	private static final long serialVersionUID = 1L;
	
	public BpDetailHtmlPanel(String title,
			IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		super(title, diagnosticsOverviewRoute);

		setBackground(new Color(238,238,238));
		int row = 0;

		addPanel(row++, new BpDetailItem("html.asyncload", BestPracticeType.ASYNC_CHECK, new BpHtmlAsyncLoadTablePanel()));
		addPanel(row++, new BpDetailItem("html.httpUsage", BestPracticeType.HTTP_1_0_USAGE));
		addPanel(row++, new BpDetailItem("html.fileorder", BestPracticeType.FILE_ORDER, new BpHtmlFileOrderTablePanel()));
		addPanel(row++, new BpDetailItem("empty.url", BestPracticeType.EMPTY_URL));
		addPanel(row++, new BpDetailItem("flash", BestPracticeType.FLASH));
		addPanel(row++, new BpDetailItem("html.displaynoneincss", BestPracticeType.DISPLAY_NONE_IN_CSS, new BpHtmlDisplayNoneInCSSTablePanel()));

		fullPanel.add(dataPanel, BorderLayout.CENTER);
		fullPanel.add(detailPanel, BorderLayout.SOUTH);
		add(fullPanel);
		
		List<BestPracticeType> list = BestPracticeType.getByCategory(Category.HTML);
		bpFileDownloadTypes.addAll(list);
	}

	@Override
	public JPanel layoutDataPanel() {
		return null;
	}
	
	@Override
	public void refresh(AROTraceData model) {
		overViewObservable.refreshModel(model);
		dateTraceAppDetailPanel.refresh(model);
		bpResults = model.getBestPracticeResults();
		updateHeader(model);
	}

}
