/*******************************************************************************
 * Copyright 2015 Bin Liu (flylb1@gmail.com)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *******************************************************************************/
package ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

import core.RuntimeConfig;

public class AboutView extends MyViewPart { //
    private static Logger log = Logger.getLogger(AboutView.class);
    static final String ID = "AboutView";
    private Text userInfo;
    private Text text;

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        body.setLayout(new GridLayout());
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("License");
            userInfo = new Text(body, SWT.MULTI /* | SWT.WRAP */);
            userInfo.setText(sb.toString());
            userInfo.setEditable(false);
            userInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Composite textbody = new Composite(body, SWT.FILL | SWT.BORDER);
            textbody.setLayout(new FillLayout());
            textbody.setBackground(new Color(null, 255, 0, 0));
            textbody.setLayoutData(new GridData(GridData.FILL_BOTH));

            text = new Text(textbody, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            text.setEditable(false);
            text.setTouchEnabled(true);
            text.setFont(RuntimeConfig.fontRegistry.get("code"));
            Bundle bundle = Platform.getBundle(TSPActivator.PLUGIN_ID);
            if (BundleUtility.isReady(bundle)) {
                URL fullPathString = BundleUtility.find(bundle, "/icons/license.txt");
                InputStream in = fullPathString.openStream();
                byte[] buffer = new byte[1024 * 1];
                in.read(buffer);
                in.close();
                String licenseStr = new String(buffer);
                text.setText(licenseStr);
                buffer = null;
            }
        } catch (SWTException e) {
            log.info(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parent.layout();
    }

    public void setFocus() {

    }

    public String getHelp() {
        return null;
    }
}
