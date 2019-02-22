/*
 *  Copyright 2015 AT&T
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
public class BpDetailConnectionsPanel extends BpDetail {

	private static final long serialVersionUID = 1L;
	
	public BpDetailConnectionsPanel(String title,
			IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		super(title, diagnosticsOverviewRoute);

		setBackground(new Color(238,238,238));
		int row = 0;

		addPanel(row++, new BpDetailItem("connections.connectionOpening", BestPracticeType.CONNECTION_OPENING));
		
		addPanel(row++, new BpDetailItem("connections.unnecssaryConn", BestPracticeType.UNNECESSARY_CONNECTIONS, new BpConnectionsUnnecessaryTablePanel()));
		
		addPanel(row++, new BpDetailItem("connections.simultaneous", BestPracticeType.SIMUL_CONN, new BPConnectionsSimultnsTablePanel()));
		
		addPanel(row++, new BpDetailItem("connections.multiSimultaneous", BestPracticeType.MULTI_SIMULCONN, new BPConnectionsSimultnsTablePanel()));
		
		addPanel(row++, new BpDetailItem("connections.periodic", BestPracticeType.PERIODIC_TRANSFER));
		
		addPanel(row++, new BpDetailItem("connections.screenRotation", BestPracticeType.SCREEN_ROTATION));
		
		addPanel(row++, new BpDetailItem("connections.connClosing", BestPracticeType.CONNECTION_CLOSING));
				
		addPanel(row++, new BpDetailItem("connections.http4xx5xx", BestPracticeType.HTTP_4XX_5XX, new BpConnectionsHttp4xx5xxTablePanel()));
		
		addPanel(row++, new BpDetailItem("connections.http3xx", BestPracticeType.HTTP_3XX_CODE, new BpConnectionsHttp3xxTablePanel()));

		
		
		addPanel(row++, new BpDetailItem("3rd.party.scripts", BestPracticeType.SCRIPTS_URL));
		

//		fullPanel.setBackground(Color.GREEN);   // bcn
//		dataPanel.setBackground(Color.RED);     // bcn
//		detailPanel.setBackground(Color.BLUE);  // bcn

		fullPanel.add(dataPanel, BorderLayout.CENTER);
		fullPanel.add(detailPanel, BorderLayout.SOUTH);
		add(fullPanel);
		
		List<BestPracticeType> list = BestPracticeType.getByCategory(Category.CONNECTIONS);
		bpFileDownloadTypes.addAll(list);
	}

	@Override
	public JPanel layoutDataPanel() {
		return null;
	}

	@Override
	public void refresh(AROTraceData model) {
		// refresh the common detail at the top of each section
		dateTraceAppDetailPanel.refresh(model);

		overViewObservable.refreshModel(model);
		
		bpResults = model.getBestPracticeResults();

		updateHeader(model);
	}

}
