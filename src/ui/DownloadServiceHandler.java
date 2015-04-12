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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

class DownloadServiceHandler implements ServiceHandler {

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fileName = RWT.getRequest().getParameter("filename"); //$NON-NLS-1$

        // Send the file in the response
        response.setContentType("application/pdf"); //$NON-NLS-1$
        String contentDisposition = "attachment; filename=\"" + fileName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        response.setHeader("Content-Disposition", contentDisposition); //$NON-NLS-1$

        Bundle bundle = Platform.getBundle(TSPActivator.PLUGIN_ID);
        if (BundleUtility.isReady(bundle)) {
            URL fullPathString = BundleUtility.find(bundle, "/icons/" + fileName); //$NON-NLS-1$
            InputStream in = fullPathString.openStream();
            byte[] buffer = new byte[1024 * 100];
            while (in.read(buffer) != -1) {
                response.getOutputStream().write(buffer);
            }
            in.close();
            buffer = null;
        }

    }
}