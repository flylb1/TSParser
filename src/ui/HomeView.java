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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class HomeView extends MyViewPart { //
    static final String ID = "HomeView";
    private Browser browser;

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        body.setLayout(new FillLayout());

        browser = new Browser(body, SWT.FILL | SWT.BORDER);
        browser.setUrl("http://flylbblog.duapp.com/?page_id=85");

        parent.layout();
    }

    public void setFocus() {

    }

    public String getHelp() {
        return null;
    }
}
