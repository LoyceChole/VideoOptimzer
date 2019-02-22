/*
 * Copyright 2015 AT&T
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
package com.att.aro.ui.commonui;

import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.bestpractice.pojo.CombineCsJssResult;
import com.att.aro.core.bestpractice.pojo.ConnectionClosingResult;
import com.att.aro.core.bestpractice.pojo.DisplayNoneInCSSEntry;
import com.att.aro.core.bestpractice.pojo.EmptyUrlResult;
import com.att.aro.core.bestpractice.pojo.FlashResult;
import com.att.aro.core.bestpractice.pojo.ForwardSecrecyEntry;
import com.att.aro.core.bestpractice.pojo.Http10UsageResult;
import com.att.aro.core.bestpractice.pojo.HttpEntry;
import com.att.aro.core.bestpractice.pojo.ImageCompressionEntry;
import com.att.aro.core.bestpractice.pojo.ImageMdataEntry;
import com.att.aro.core.bestpractice.pojo.MultipleConnectionsEntry;
import com.att.aro.core.bestpractice.pojo.PeriodicTransferResult;
import com.att.aro.core.bestpractice.pojo.ScriptsResult;
import com.att.aro.core.bestpractice.pojo.SpriteImageEntry;
import com.att.aro.core.bestpractice.pojo.TransmissionPrivateDataEntry;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionEntry;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.bestpractice.pojo.UnsecureSSLVersionEntry;
import com.att.aro.core.bestpractice.pojo.UsingCacheResult;
import com.att.aro.core.bestpractice.pojo.WeakCipherEntry;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.VideoStall;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.diagnostictab.DiagnosticsTab;
import com.att.aro.ui.view.overviewtab.OverviewTab;

/**
 *
 */
public class ARODiagnosticsOverviewRouteImpl implements IARODiagnosticsOverviewRoute {
	private static final Logger LOG = LogManager.getLogger(ARODiagnosticsOverviewRouteImpl.class);
	private final JTabbedPane jtabbedPane;
	private static final int OVERVIEW_INDEX = 1;
	private static final int DIAGNOSTIC_INDEX = 2;
	private static final int WATERFALL_INDEX = 5;

	public ARODiagnosticsOverviewRouteImpl(JTabbedPane jtabbedPane) {
		this.jtabbedPane = jtabbedPane;
	}

	private boolean isTabRoute(String headerColumnMarker, DataTableModel<? extends AbstractTableModel> tableModel) {
		boolean tabRoute = false;
		for (int index = 0; index < tableModel.getColumnCount(); ++index) {
			if (headerColumnMarker.equals(tableModel.getColumnName(index))) {
				tabRoute = true;
				break;
			}
		}
		return tabRoute;
	}

	private boolean isDiagnosticsTabRoute(DataTableModel<? extends AbstractTableModel> tableModel) {
		boolean diaTabRoute = isTabRoute("Time", tableModel);
		if (!diaTabRoute) {
			diaTabRoute = isTabRoute("StartTime", tableModel);
			if (!diaTabRoute) {
				diaTabRoute = isTabRoute("Destination IP", tableModel);
				if (!diaTabRoute) {
					diaTabRoute = isTabRoute(ResourceBundleHelper.getMessageString("videoStall.table.col3"),
							tableModel);
				}
			}
		}
		return diaTabRoute;
	}

	private boolean isOverviewTabRoute(DataTableModel<? extends AbstractTableModel> tableModel) {
		return isTabRoute("File Size", tableModel);
	}

	@Override
	public int getRoutedJTabbedPaneIndex(DataTableModel<? extends DataTableModel<?>> tableModel) {
		int routedJTabbedPandIndex = -1;
		if (isDiagnosticsTabRoute(tableModel)) {
			routedJTabbedPandIndex = DIAGNOSTIC_INDEX;
		} else if (isOverviewTabRoute(tableModel)) {
			routedJTabbedPandIndex = OVERVIEW_INDEX;
		}
		return routedJTabbedPandIndex;
	}

	@Override
	public int route(DataTableModel<? extends DataTableModel<?>> tableModel, Object routeInfo) {
		int routedIndex = routeInfo != null ? getRoutedJTabbedPaneIndex(tableModel) : -1;
		if (routedIndex == DIAGNOSTIC_INDEX) {
			updateDiagnosticsTab(routeInfo);
		} else if (routedIndex == OVERVIEW_INDEX) {
			updateOverviewTab(routeInfo);
		}
		return routedIndex;
	}

	@Override
	public void routeHyperlink(BestPracticeType bpType) {
		if (bpType.equals(BestPracticeType.DUPLICATE_CONTENT)) {
			jtabbedPane.setSelectedIndex(OVERVIEW_INDEX);
		} else {
			jtabbedPane.setSelectedIndex(DIAGNOSTIC_INDEX);
			DiagnosticsTab diagnosticsTab = (DiagnosticsTab) jtabbedPane.getSelectedComponent();
			switch (bpType) {
			case FILE_COMPRESSION: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof FileCompressionResult) {
				// diagnosticsTab.setHighlightedTCP(((FileCompressionResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			// case DUPLICATE_CONTENT: {
			// for (AbstractBestPracticeResult result :
			// diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
			// if (result instanceof DuplicateContentResult) {
			// diagnosticsTab.setHighlightedTCP(((DuplicateContentResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
			// break;
			// }
			// }
			// }
			// break;
			case USING_CACHE: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof UsingCacheResult) {
						diagnosticsTab.setHighlightedTCP(
								((UsingCacheResult) result).getNoCacheHeaderFirstPacket().getTimeStamp());
						break;
					}
				}
			}
				break;
			case CACHE_CONTROL: {// cant not find cache control start time in
									// CacheControlResult
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof CacheControlResult) {
				// diagnosticsTab.setHighlightedTCP(((CacheControlResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case COMBINE_CS_JSS: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof CombineCsJssResult) {
						diagnosticsTab.setHighlightedTCP(
								((CombineCsJssResult) result).getConsecutiveCssJsFirstPacket().getTimeStamp());
						break;
					}
				}
			}
				break;
			case IMAGE_SIZE: {
			}
				break;
			case IMAGE_MDATA: {
			}
				break;
			case IMAGE_CMPRS: {
			}
				break;
			case IMAGE_FORMAT: {
			}
				break;
			case IMAGE_COMPARE: {
			}
				break;
			case MINIFICATION: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof CombineCsJssResult) {
				// diagnosticsTab.setHighlightedTCP(((MinificationResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case SPRITEIMAGE: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof SpriteImageResult) {
				// diagnosticsTab.setHighlightedTCP(((SpriteImageResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case UNNECESSARY_CONNECTIONS: {// to test
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof UnnecessaryConnectionResult) {
						diagnosticsTab
								.setHighlightedTCP(((UnnecessaryConnectionResult) result).getTightlyCoupledBurstTime());
						break;
					}
				}
			}
				break;
			case SCRIPTS_URL: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof ScriptsResult) {
						diagnosticsTab.setHighlightedTCP(((ScriptsResult) result).getFirstFailedHtml());
						break;
					}
				}
			}
				break;
			case SCREEN_ROTATION: {
			}
				break;
			case SIMUL_CONN: {
			}
				break;
			case MULTI_SIMULCONN: {
			}
				break;
			case PERIODIC_TRANSFER: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof PeriodicTransferResult) {
						diagnosticsTab
								.setHighlightedTCP(((PeriodicTransferResult) result).getMinimumPeriodicRepeatTime());
						break;
					}
				}
			}
				break;
			case HTTP_4XX_5XX: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof Http4xx5xxResult) {
				// diagnosticsTab.setHighlightedTCP(((Http4xx5xxResult)result).getHttpResCodelist()ConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case HTTP_3XX_CODE: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof Http3xxCodeResult) {
				// diagnosticsTab.setHighlightedTCP(((Http3xxCodeResult)result).getFirstResMap()ConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case HTTP_1_0_USAGE: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof Http10UsageResult) {
						diagnosticsTab.setHighlightedTCP(((Http10UsageResult) result).getHttp10Session());
						break;
					}
				}
			}
				break;
			case FLASH: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof FlashResult) {
						diagnosticsTab.setHighlightedTCP(((FlashResult) result).getFirstFlash());
						break;
					}
				}
			}
				break;
			case FILE_ORDER: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof FileOrderResult) {
				// diagnosticsTab.setHighlightedTCP(((FileOrderResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case EMPTY_URL: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof EmptyUrlResult) {
						diagnosticsTab.setHighlightedTCP(((EmptyUrlResult) result).getFirstFailedHtml());
						break;
					}
				}
			}
				break;
			case DISPLAY_NONE_IN_CSS: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof DisplayNoneInCSSResult) {
				// diagnosticsTab.setHighlightedTCP(((DisplayNoneInCSSResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case CONNECTION_OPENING: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof ConnectionOpeningResult) {
				// diagnosticsTab.setHighlightedTCP(((ConnectionOpeningResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case CONNECTION_CLOSING: {// done
				for (AbstractBestPracticeResult result : diagnosticsTab.getAnalyzerResult().getBestPracticeResults()) {
					if (result instanceof ConnectionClosingResult) {
						diagnosticsTab.setHighlightedTCP(((ConnectionClosingResult) result).getLargestEnergyTime());
						break;
					}
				}
			}
				break;
			case ASYNC_CHECK: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof AsyncCheckInScriptResult) {
				// diagnosticsTab.setHighlightedTCP(((AsyncCheckInScriptResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case ACCESSING_PERIPHERALS: {
				// for (AbstractBestPracticeResult result :
				// diagnosticsTab.getAnalyzerResult().getBestPracticeResults())
				// {
				// if (result instanceof AccessingPeripheralResult) {
				// diagnosticsTab.setHighlightedTCP(((AccessingPeripheralResult)result).getConsecutiveCssJsFirstPacket().getTimeStamp());
				// break;
				// }
				// }
			}
				break;
			case DUPLICATE_CONTENT:
				break;
			default:
				break;
			}// switch
		}
	}

	@Override
	public void updateDiagnosticsTab(Object routeInfo) {
		int oldPanelIndex = jtabbedPane.getSelectedIndex();
		jtabbedPane.setSelectedIndex(DIAGNOSTIC_INDEX);
		DiagnosticsTab diagnosticsTab = (DiagnosticsTab) jtabbedPane.getSelectedComponent();
		if (routeInfo == null) {
			jtabbedPane.setSelectedIndex(oldPanelIndex);
			LOG.error("Diagnostics Tab needs a type for updating");
			return;
		}
		LOG.debug("Type used to route to Diagnostics Tab: " + routeInfo.getClass().getSimpleName());
		if (routeInfo instanceof CacheEntry) {
			diagnosticsTab.setHighlightedTCP(((CacheEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof Session) {
			diagnosticsTab.setHighlightedTCP(((Session) routeInfo));
		} else if (routeInfo instanceof HttpRequestResponseInfo) {
			diagnosticsTab.setHighlightedTCP((HttpRequestResponseInfo) routeInfo);
		} else if (routeInfo instanceof HttpEntry) {
			diagnosticsTab.setHighlightedTCP(((HttpEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof DisplayNoneInCSSEntry) {
			diagnosticsTab.setHighlightedTCP(((DisplayNoneInCSSEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof ImageMdataEntry) {
			diagnosticsTab.setHighlightedTCP(((ImageMdataEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof ImageCompressionEntry) {
			diagnosticsTab.setHighlightedTCP(((ImageCompressionEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof MultipleConnectionsEntry) {
			if (((MultipleConnectionsEntry) routeInfo).isMultiple()) {
				jtabbedPane.setSelectedIndex(WATERFALL_INDEX);
			} else {
				if (((MultipleConnectionsEntry) routeInfo).getHttpReqRespInfo().getSession() != null) {
					diagnosticsTab.setHighlightedSessionTCP(((MultipleConnectionsEntry) routeInfo).getHttpReqRespInfo());
				} else {
					diagnosticsTab.setHighlightedTCP(((MultipleConnectionsEntry) routeInfo).getHttpReqRespInfo());
				}
			}

		} else if (routeInfo instanceof SpriteImageEntry) {
			diagnosticsTab.setHighlightedTCP(((SpriteImageEntry) routeInfo).getHttpRequestResponse());
		} else if (routeInfo instanceof UnnecessaryConnectionEntry) {
			UnnecessaryConnectionEntry unConnectionEntry = (UnnecessaryConnectionEntry) routeInfo;
			diagnosticsTab.setHighlightedTCP(unConnectionEntry.getLowTime());
		} else if (routeInfo instanceof TransmissionPrivateDataEntry || routeInfo instanceof UnsecureSSLVersionEntry
				|| routeInfo instanceof WeakCipherEntry || routeInfo instanceof ForwardSecrecyEntry) {
			diagnosticsTab.setHighlightedTCP(routeInfo);
		} else if (routeInfo instanceof VideoStall) {
			double timestamp = ((VideoStall) routeInfo).getSegmentTryingToPlay().getStartTS();
			diagnosticsTab.getGraphPanel().setGraphView(timestamp, true);
			diagnosticsTab.getVideoPlayer().setMediaTime(timestamp);
		} else {
			jtabbedPane.setSelectedIndex(oldPanelIndex);
			LOG.error("Diagnostics Tab cannot handle a type of " + routeInfo.getClass().getSimpleName()
					+ " for updating");
		}
	}

	@Override
	public void launchSliderDialogFromDiagnosticTab() {
		jtabbedPane.setSelectedIndex(DIAGNOSTIC_INDEX);
		DiagnosticsTab diagnosticTab = (DiagnosticsTab) jtabbedPane.getSelectedComponent();
		diagnosticTab.launchSliderDialog();
	}

	public void updateOverviewTab(Object routeInfo) {
		int oldPanelIndex = jtabbedPane.getSelectedIndex();
		jtabbedPane.setSelectedIndex(OVERVIEW_INDEX);
		OverviewTab overviewTab = (OverviewTab) jtabbedPane.getSelectedComponent();
		if (routeInfo == null) {
			jtabbedPane.setSelectedIndex(oldPanelIndex);
			LOG.error("Overview Tab needs a type for updating");
			return;
		}
		LOG.debug("Type used to route to Overview Tab: " + routeInfo.getClass().getSimpleName());
		if (routeInfo instanceof CacheEntry) {
			overviewTab.setHighlightedDuplicate((CacheEntry) routeInfo);
		} else {
			jtabbedPane.setSelectedIndex(oldPanelIndex);
			LOG.error("Overview Tab cannot handle a type of " + routeInfo.getClass().getSimpleName() + " for updating");
		}
	}

	@Override
	public JTabbedPane getJtabbedPane() {
		return jtabbedPane;
	}
}
