/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.dashboard;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.gettingstarted.browser.BrowserFactory;
import org.springsource.ide.eclipse.commons.gettingstarted.browser.STSBrowserViewer;


//Note: some complications on Linux systems because of problems satisfying
// the requirements for SWT browser component to work.
//For Ubuntu 12.04 some usefull info here.
//Maybe this can be somehow fixed by us packaging a compatible xulrunner
//and STS.ini file?

// http://askubuntu.com/questions/125980/how-do-i-install-xulrunner-in-12-04

/**
 * A DashBoard page that displays the contents of a webpage.
 * 
 * @author Kris De Volder
 */
public class WebDashboardPage extends ADashboardPage /* implements IExecutableExtension*/ {

//	/**
//	 * Using this ID ensures we only open one 'slave' browser when opening links from within
//	 * a dashboard page.
//	 */
//	public static final String DASHBOARD_SLAVE_BROWSER_ID = WebDashboardPage.class.getName()+".SLAVE";

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String homeUrl;

	private String name;

	private Shell shell;

	private STSBrowserViewer browserViewer;

	/**
	 * Constructor for when this class is used as n {@link IExecutableExtension}. In that case
	 * setInitializationData method will be called with infos from plugin.xml to fill
	 * in the fields.
	 */
	public WebDashboardPage() {
	}
	
	/**
	 * Name may by null, in that case the name will be set later when the page is loaded
	 * in the browser (The title from the first page title event is used).
	 */
	public WebDashboardPage(String name, String homeUrl) {
		this.name = name;
		this.homeUrl = homeUrl;
	}

	
//	@Override
//	public void setInitializationData(IConfigurationElement cfig,
//			String propertyName, Object data) {
//		if (data!=null && data instanceof Map) {
//			@SuppressWarnings("unchecked")
//			Map<String, String> map = (Map<String, String>) data;
//			this.name = map.get("name");
//			this.homeUrl = map.get("url");
//		}
//		Assert.isNotNull(this.name, "A name must be provided as initialization data for WebDashboardPage");
//		Assert.isNotNull(this.homeUrl, "A url must be provided as initialization data for WebDashboardPage");
//	}
	
	@Override
	public void createControl(Composite parent) {
		this.shell = parent.getShell();
		parent.setLayout(new FillLayout());
		browserViewer = BrowserFactory.create(parent, hasToolbar());
		final Browser browser = browserViewer.getBrowser();
		if (homeUrl!=null) {
			browserViewer.setHomeUrl(homeUrl);
			browserViewer.setURL(homeUrl);
		} else {
			browser.setText("<h1>URL not set</h1>" +
					"<p>Url should be provided via the setInitializationData method</p>"
			);
		}
		if (getName()==null) {
			//Get name from the browser
			final TitleListener l = new TitleListener() {
				@Override
				public void changed(TitleEvent event) {
					setName(event.title);
					browser.removeTitleListener(this);
				}
			};
			browser.addTitleListener(l);
		}
		addBrowserHooks(browser);
	}

	/**
	 * Subclasses may override if they don't want the url and buttons toolbar.
	 * Defailt implementation returns true causing the toolbar to be added
	 * when the browser widget is created.
	 */
	protected boolean hasToolbar() {
		return true;
	}

	/**
	 * Subclasses may override this if they want to customize the browser (e.g. add listeners to
	 * handle certain urls specially.
	 */
	protected void addBrowserHooks(Browser browser) {
	}

	/**
	 * The url of the landing page this dashboard page will show when it is opened.
	 */
	public String getHomeUrl() {
		return homeUrl;
	}
	
	public String getPageId() {
		return getHomeUrl();
	}
	
	/**
	 * Change the url this dashboard page will show when it is first opened,
	 * or when the user clicks on the 'home' icon. 
	 */
	public void setHomeUrl(String url) {
		this.homeUrl = url;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		DashboardPageContainer container = getContainer();
		if (name!=null && container!=null) {
			CTabItem widget = container.getWidget();
			if (widget!=null && !widget.isDisposed()) {
				widget.setText(name);
			}
		}
	}
	
	@Override
	public boolean canClose() {
		return true;
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public void goHome() {
		browserViewer.goHome();
	}
	
	@Override
	public void dispose() {
		if (this.browserViewer!=null) {
			this.browserViewer.dispose();
			this.browserViewer = null;
		}
		super.dispose();
	}
}
 