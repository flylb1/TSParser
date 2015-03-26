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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;

import core.BitStream;
import core.StreamHistory;
import core.TSSection;
import core.TSpacket;

public class TsExportHandler implements ServiceHandler {
    private static Logger log = Logger.getLogger(TsExportHandler.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fileName = RWT.getRequest().getParameter("filename");
        log.info("fileName:" + fileName);

        // Send the file in the response
        // HttpServletResponse response = RWT.getResponse();
        response.setContentType("application/bin");
        String contentDisposition = "attachment; filename=\"" + new File(fileName).getName() + ".exp\"";
        response.setHeader("Content-Disposition", contentDisposition);

        BitStream ts = StreamHistory.getBitStream(fileName);
        List<TSSection> sections = ts.getSectionManager().getSectionList();
        List<TSpacket> tsPackets = null;
        byte[] rawPacket = null;
        for (TSSection section : sections) {
            tsPackets = section.getPacketList();
            for (TSpacket tsPacket : tsPackets) {
                rawPacket = tsPacket.getRawData();
                response.getOutputStream().write(rawPacket);
            }
        }

    }
}