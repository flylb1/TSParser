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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import core.NodeValue;
import core.NumberUtil;
import core.TSUtil;
import core.TextUtil;

@SuppressWarnings("unchecked")
public class UIUtil {//
    private static Logger log = Logger.getLogger(UIUtil.class);

    public static Image tableImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/tables.gif").createImage();

    public static Image sectionImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/section.png").createImage();

    public static ImageDescriptor filterImageDescriptor = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/setting.gif");

    public static ImageDescriptor reparseImageDescriptor = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/reparse.gif");

    public static ImageDescriptor infoImageDescriptor = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/infomation.gif");

    public static ImageDescriptor exportImageDescriptor = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/export.gif"); //

    public static Image sectionRootImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/sectionRoot.gif").createImage(); //

    public static Image loopImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/loop.gif").createImage();

    public static Image binImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/bin.gif").createImage();

    public static Image nodeImage = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/node.gif").createImage();

    public static Image up = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/up.gif").createImage();

    public static Image down = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/down.gif").createImage();

    public static Image left = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/left.gif").createImage();

    public static Image right = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/right.gif").createImage();

    public static Image leftDay = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/left1.gif").createImage();

    public static Image rightDay = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/right1.gif").createImage();

    public static Image great = //
    AbstractUIPlugin.imageDescriptorFromPlugin(TSPActivator.PLUGIN_ID, "/icons/i.png").createImage();

    public static void renderNode(Object _values, TreeItem _sectionTree) {
        renderNode(_values, _sectionTree, false);
    }

    public static void renderNode(Object _values, TreeItem _sectionTree, boolean expend) {
        if (_values == null) {
            return;
        }
        List<NodeValue> values = (List<NodeValue>) _values;
        for (NodeValue node : values) {

            String content = null;
            TreeItem item = new TreeItem(_sectionTree, 0);
            if (node.getValue() == null) {
                item.setImage(binImage);
                content = node.getName() + " = null";
            } else if (node.getValue().getClass() == ArrayList.class) {
                item.setImage(loopImage);
                content = node.getName();
                List<NodeValue> _list = (List<NodeValue>) node.getValue();
                if (_list != null && _list.size() > 0) {
                    for (NodeValue _firstNode : _list) {
                        if (_firstNode.getValue().getClass() != ArrayList.class) {
                            content += " " + _firstNode.getName() + " = " + _firstNode.getValue()//
                                    + " [0x" + NumberUtil.Object2Hex(_firstNode.getValue()) + "]";
                            break;
                        }
                    }
                }
                renderNode((List<NodeValue>) node.getValue(), item, expend);

            } else if (node.getValue().getClass() == byte[].class) {
                item.setImage(binImage);
                content = bytesNodeRender(node, values);
            } else if (node.getName().indexOf("ISO_639") != -1 || node.getName().indexOf("country_code") != -1) {
                item.setImage(nodeImage);
                byte[] languageBytes = new byte[3];
                int languageNumber = ((Long) node.getValue()).intValue();
                languageBytes[0] = (byte) ((languageNumber >> 16) & 0x000000ff);
                languageBytes[1] = (byte) ((languageNumber >> 8) & 0x000000ff);
                languageBytes[2] = (byte) ((languageNumber >> 0) & 0x000000ff);
                content = node.getName() + " = " /* + node.getValue() *///
                        + "[" + new String(languageBytes) + "]";
            } else {
                item.setImage(nodeImage);
                content = node.getName() + " = " + node.getValue()//
                        + " [0x" + NumberUtil.Object2Hex(node.getValue()) + "]";
            }
            item.setText(content);
        }
        _sectionTree.setExpanded(expend);
    }

    private static String bytesNodeRender(NodeValue node, List<NodeValue> values) {
        String content = null;
        String nodeName = node.getName();
        if (nodeName.equalsIgnoreCase("start_time") //
                || nodeName.equalsIgnoreCase("UTC_time")//
                || nodeName.equalsIgnoreCase("time_of_change")//
        )//
        {
            byte[] dt = (byte[]) node.getValue();
            Calendar cal = TSUtil.bufferToCal(dt);
            content = nodeName + " = " + TSUtil.calendarToString(cal, "yyyy/MM/dd hh:mm:ss");
        } else if (nodeName.equalsIgnoreCase("Unknow")) {
            if (node.getValue() != null) {
                content = nodeName + " = bytes ";
            }
        } else if (nodeName.equalsIgnoreCase("char") || nodeName.equalsIgnoreCase("text_char")) {
            // if (node.getValue() != null) {
            // content = nodeName + " = bytes " + (byte[]) node.getValue();
            // }
            // log.info(nodeName + "\t" + node.getValue());
            String str = "";
            try {
                str = TextUtil.getTextFromByte((byte[]) node.getValue(), values);
            } catch (Exception e) {
                log.info("Decode text error");
            }
            content = node.getName() + " = " + str;// new String((byte[]) node.getValue());
            // log.info(content);

        } else {
            String str = "";
            try {
                str = TextUtil.getTextFromByte((byte[]) node.getValue(), values);
            } catch (Exception e) {
                log.info("Decode text error");
            }
            content = node.getName() + " = " + str;// new String((byte[]) node.getValue());
        }
        return content;
    }

    public static Composite createFormComposite(Composite parent, int top, int left, int right, int bottom) {
        return createFormComposite(parent, top, left, right, bottom, SWT.NONE);
    }

    public static Composite createFormComposite(Composite parent, int top, int left, int right, int bottom, int style) {
        final Composite result = new Composite(parent, style);
        FormData fdLabel = new FormData();
        fdLabel.top = new FormAttachment(top, 0);
        fdLabel.left = new FormAttachment(left, 0);
        fdLabel.right = new FormAttachment(right, 0);
        fdLabel.bottom = new FormAttachment(bottom, 0);
        result.setLayoutData(fdLabel);
        return result;
    }

}
