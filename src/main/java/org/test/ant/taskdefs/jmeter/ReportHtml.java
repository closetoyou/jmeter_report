package org.test.ant.taskdefs.jmeter;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportHtml {
    private static int allCase = 0;
    private static int passCase = 0;
    private static NumberFormat numberFormat = NumberFormat.getInstance();

    public static void main(String[] args) {
    	String jtlPath = "C:\\Users\\test\\Downloads\\3\\report\\jtl\\TestReport201812240757.jtl";
    	String outHtmlPath = "C:\\Users\\Downloads\\1\\report\\html\\TestReport.html";
    	String runUser = "test";
    	String reportUrl = "";
    	String reportName = "";
        try {
        	write(jtlPath, outHtmlPath, runUser, reportName, reportUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String write(String jtlPath, String outHtmlPath, String runUser, String reportName, String reportUrl) {
        System.out.println("开始读取JTL文件,路径:" + jtlPath);
        try {
			String a = read(jtlPath);
			a = a.replace("&#", "");
			WritetoFile(a,jtlPath);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Document document = null;
        DocumentBuilder documentBuilder = null;
        File file = new File(jtlPath);
        try {
            DocumentBuilderFactory factory = null;
            factory = DocumentBuilderFactory.newInstance();
            documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(file);
        } catch (Exception e) {
            System.out.println("转换JTL文件失败，请确认格式是否正确:" + e);
            e.printStackTrace();
        }
        System.out.println("读取JTL文件完毕，开始获取测试用例:");
        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        long startTime = 0; int start = 0;
        long endTime = 0;
        List<ReportCase> reportCaseList = new ArrayList<ReportCase>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.contains("Sample")) {
                ReportCase reportCase = new ReportCase();
                NamedNodeMap namedNodeMap = node.getAttributes();
                try {
                    Node lb = namedNodeMap.getNamedItem("lb");
                    String caseName = lb.getNodeValue();
                    reportCase.setCaseName(caseName);
                    Node runTimeNode = namedNodeMap.getNamedItem("t");
                    if (!isEmpty(runTimeNode)) {
                        reportCase.setRunTime(runTimeNode.getNodeValue() + "ms");
                    }
                    Node responseCodeNode = namedNodeMap.getNamedItem("rc");
                    if (!isEmpty(responseCodeNode)) {
                        reportCase.setResponseCode(responseCodeNode.getNodeValue());
                    }
                    Node responseMessageNode = namedNodeMap.getNamedItem("rm");
                    if (!isEmpty(responseMessageNode)) {
                        reportCase.setResponseMessage(responseMessageNode.getNodeValue());
                    }
                    Node startTimeNode = namedNodeMap.getNamedItem("ts");
                    if (!isEmpty(startTimeNode)) {
                        String startTimeStr = startTimeNode.getNodeValue();
                        if (start == 0) {
                            startTime = Long.parseLong(startTimeStr);
                            start++;
                        }
                        if (i >= nodeList.getLength() - 2) {
                            endTime = Long.parseLong(startTimeStr);
                        }
                    }
                    Node statusNode = namedNodeMap.getNamedItem("s");
                    if (!isEmpty(statusNode)) {
                        String s = statusNode.getNodeValue();
                        boolean status = Boolean.parseBoolean(s);
                        if (status) {
                        }
                        reportCase.setStatus(status);
                    }

                    Node assertionNode = getNodeByName(node, "assertionResult");
                    if (!isEmpty(assertionNode)) {
                        AssertionResult assertionResult = formatAssertion(assertionNode);
                        reportCase.setAssertionResult(assertionResult);
                    } else {
                        reportCase.setAssertionResult(null);
                    }
                    Node responseDataNode = getNodeByName(node, "responseData");
                    if (!isEmpty(responseDataNode)) {
                    	String a = responseDataNode.getTextContent();
                    	if (a.indexOf("<html>")!=-1) {
                    		FilterHtmlUtil f = new FilterHtmlUtil();
                    		a = f.Html2Text(a);
                    		reportCase.setResponseData(a);
                    	}
                    	else {
                        reportCase.setResponseData(responseDataNode.getTextContent());
                    	}
                    }

                    Node methodNode = getNodeByName(node, "method");
                    if (!isEmpty(methodNode)) {
                        reportCase.setMethodName(methodNode.getTextContent());
                    }
                    Node queryStringNode = getNodeByName(node, "queryString");
                    if (!isEmpty(queryStringNode)) {
                        reportCase.setQueryString(queryStringNode.getTextContent());
                    }
                    Node urlNode = getNodeByName(node, "java.net.URL");
                    if (!isEmpty(urlNode)) {
                        reportCase.setUrl(urlNode.getTextContent());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                reportCaseList.add(reportCase);
            }
        }
        System.out.println("用例转换完毕，开始写入报告，写入路径:" + outHtmlPath);
        double runTime = (double)(endTime - startTime) / 1000;
//        outHtmlPath = "C:\\Users\\test\\Downloads\\3\\report\\html\\TestReport.html"; //test
        writeReport(reportCaseList, runTime, outHtmlPath, runUser, reportName, reportUrl);
        String Content = "";
        return Content;
    }

    private static boolean isEmpty(Object o) {
        boolean f = false;
        if (o == null) {
            f = true;
        }
        return f;
    }

    private static AssertionResult formatAssertion(Node assertionNode) {
        AssertionResult assertionResult = new AssertionResult();
        Node nameNode = getNodeByName(assertionNode, "name");
        assertionResult.setName(nameNode.getTextContent());
        Node failureNode = getNodeByName(assertionNode, "failure");
        boolean failure = Boolean.parseBoolean(failureNode.getTextContent());
        assertionResult.setFailure(failure);
        Node errorNode = getNodeByName(assertionNode, "error");
        boolean error = Boolean.parseBoolean(errorNode.getTextContent());
        assertionResult.setError(error);
        Node failureMessageNode = getNodeByName(assertionNode, "failureMessage");
        if (!isEmpty(failureMessageNode)) {
            assertionResult.setFailureMessage(failureMessageNode.getTextContent());
        }
        return assertionResult;
    }

    public static Node getNodeByName(Node node, String name) {
        NodeList msgNodeList = node.getChildNodes();
        Node node1 = null;
        for (int k = 0; k < msgNodeList.getLength(); k++) {
            Node childNode = msgNodeList.item(k);
            String childName = childNode.getNodeName();
            if (childName.equalsIgnoreCase(name)) {
                node1 = childNode;
                break;
            }
        }
        return node1;
    }
    
    public static String getfilename(String htmlPath) {
    	File directory = new File(htmlPath);
    	String path = directory.getParent();
    	System.out.println("path:" + path);
    	path = path.replace("\\report\\html", "");
//    	path = path.replace("webapps", "workspace").replace("/html", "").replace("/loglist", "").replace("/api", "");
    	String fileName = "";
    	File file = new File(path);
    	System.out.println("用例名称目录:" + path);
    	File[] fileList = file.listFiles();
    	for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile() && fileList[i].toString().indexOf(".jmx")!=-1) {
                fileName = fileList[i].getName().replace(".jmx", "");
                System.out.println("文件：" + fileName);                
            }
    	}
		return fileName;	
    }

    public static void writeReport(List<ReportCase> reportCaseList, double runTime, String htmlPath, String runUser, String reportName, String reportUrl) {
    	String fileName = getfilename(htmlPath);
        String title = getTitle() + getBodyTitle(fileName);     
        Date d = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        fileName = fileName+"接口自动化测试报告"+sdf.format(d)+".html";
        System.out.println("fileName:" + fileName);
        String suiteBody = getSuiteList(reportCaseList) + getCaseStep(reportCaseList) + htmlEnd();
        String summary = getSummary(runTime, runUser);
        String mailHtml = title + summary;
        String html = title + summary + suiteBody;
        String path = "";
        htmlPath = htmlPath.replace("report.html", fileName);
        System.out.println("htmlPath:" + htmlPath);
        File directory = new File(htmlPath);
        try {
            path = directory.getParent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File htmlPathFile = new File(htmlPath);
            String pathParent = htmlPathFile.getParent();
            File parentFile = new File(pathParent);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String passRate = numberFormat.format((float) passCase / (float) allCase * 100);
        System.out.println("写入报告路径:" + htmlPath);
/*
        html = html.replaceAll(" bgcolor=\\\\\"white\\\\\"", "");
        FileUtil.writeFile("E:\\log11.txt", html);
        FileUtil.writeFile("F:\\apache-tomcat\\apache-tomcat-8.5.35\\webapps\\report\\temp\\"+fileName, html);
        WritetoFile1(fileName+"|"+passRate,"F:\\apache-tomcat\\apache-tomcat-8.5.35\\webapps\\report\\loglist.js");
 */
        FileUtil.writeFile(htmlPath, html);

        utf8bom u8 = new utf8bom();
        try {
			u8.Process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("报告地址:" + reportUrl + reportName);

        System.out.println("报告写入结束");
    }

    private static String getTitle() {
        return getHeadStart() + getStyle() + getJavaScript() + getHeadEnd();
    }

    private static String getBodyTitle(String filename) {
//    	Date d = new Date();
//    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String reportName = filename+"接口自动化测试报告";
        String date = DateUtil.getCurrentDate();
        String bodyTitle = "<body><h1>" + reportName + "</h1>" +
                "<table width=\"100%\">" +
                "    <tr>" +
                "        <td align=\"left\">Date report: " + date + "</td>" +             
                "    </tr>" +
                "</table>";
        return bodyTitle;
    }

    private static String getSummary(double runTimeLong, String runUser) {
        int failCase = allCase - passCase;
        String passRate = numberFormat.format((float) passCase / (float) allCase * 100) + "%";
        String runTime = runTimeLong + "秒";
        String ip = OS.getLocalIP();
        String summary = "" +
                "<hr size=\"1\">" +
                "<h2>概要</h2>" +
                "<table align=\"center\" class=\"details\" border=\"0\" cellpadding=\"5\" cellspacing=\"2\" width=\"95%\">" +
                "    <tr valign=\"top\">" +
                "        <th>用例总数</th>" +
                "        <th>通过数</th>" +
                "        <th>失败数</th>" +
                "        <th>通过率</th>" +
                "        <th>运行时长</th>" +
                "        <th>执行机器IP</th>" +
                "        <th>执行人</th>" +
                "    </tr>" +
                "    <tr valign=\"top\" class=\"\">" +
                "        <td align=\"center\">" + allCase + "</td>" +
                "        <td align=\"center\">" +
                "           <span style=\"color: green\">" +
                "             <b>" + passCase + "</b>" +
                "           </span>" +
                "        </td>" +
                "        <td align=\"center\">" +
                "              <span style=\"color: red;width:120px;height:20px;\">" +
                "             <b>" + failCase + "</b>" +
                "           </span></td>" +
                "        <td align=\"center\">" + passRate + "</td>" +
                "        <td align=\"center\">" + runTime + "</td>" +
                "        <td align=\"center\">" + ip + "</td>" +
                "        <td align=\"center\">" + runUser + "</td>" +
                "    </tr>" +
                "</table>" +
                "</br>";
        return summary;
    }

    private static int caseNum = 0;

    private static String getSuiteList(List<ReportCase> reportCaseList) {
        String s = "<hr size=\"1\" width=\"100%\" align=\"center\">" +
                "<div>" +
                "    <div id=\"div_left\" style=\"overflow:auto\">" +
                " <table id=\"suites\">";
        String suites = "";
        int i = 0;
        String suiteName = "用例列表";
        String tbodyId = "tests-" + i;
        String toggleId = "toggle-" + i;
        suites += "<thead>" +
                "            <tr>" +
                "                <th class=\"header suite\" onclick=\"toggleElement('" + tbodyId + "', 'table-row-group'); toggle('" + toggleId + "')\">" +
                "                    <span id=\"" + toggleId + "\" class=\"toggle\">&#x25bc;</span>" +
                "                    <span id=\"" + i + "\">" + suiteName + "</span>" +
                "                </th>" +
                "            </tr>" +
                "            </thead>";
        String suiteCase = getCaseList(tbodyId, reportCaseList);
        suites += suiteCase;

        String center = "<div id=\"div_center\">" +
                "    </div>";
        s = s + suites + "  </table></div>" + center;
        return s;
    }


    private static String getCaseList(String tbodyId, List<ReportCase> caseList) {
        String c = "<tbody id=\"" + tbodyId + "\" class=\"tests\">";
        String div = "<div id=\"allSpan\" style=\"display:none\">";
        for (int i = 0; i < caseList.size(); i++) {
            ReportCase reportCase = caseList.get(i);
            String caseName = reportCase.getCaseName();
            boolean caseStatus = reportCase.isStatus();
            c += "<tr><td class=\"test\">";
            allCase++;
            div += "<div>";
            if (caseStatus) {
                passCase++;
                c += " <span class=\"successIndicator\" title=\"全部通过\">&#x2714;</span>";
                div += " <span class=\"successIndicator\" title=\"全部通过\">&#x2714;</span>";
            } else {
                c += " <span class=\"failureIndicator\" title=\"部分失败\">&#x2718;</span>";
                div += " <span class=\"failureIndicator\" title=\"部分失败\">&#x2718;</span>";
            }
            c += "   <a id=\"Case" + caseNum + "\" href=\"#\" onclick=\"showDetail(this)\">" + caseName + "</a>" +
                    "   </td>" +
                    "   </tr>";
            div += "<a  href=\"#\" onclick=\"showDetail(this)\">" + caseName + "</a>";
            div += "</div>";
            caseNum++;
        }
        div += "</div>";
        c += div;
        c += "</tbody>";
        return c;
    }


    private static String htmlEnd() {
        return "</div></div></body></html>";
    }


    private static String getCaseStep(List<ReportCase> reportCaseList) {
        String div = "<div id=\"div_right\" style=\"overflow:auto\">";
        div += "<ol id=\"right-panel\">\n";
        caseNum = 0;
        String caseDiv = "";
        String firstCaseName = "";
        String step = "";
        for (int k = 0; k < reportCaseList.size(); k++) {
            ReportCase reportCase = reportCaseList.get(k);
            String caseName = reportCase.getCaseName();
            String display = "none";
            if (caseNum == 0) {
                display = "";
                firstCaseName = caseName;
            }
            step += "<div id =\"parentCase" + caseNum + "\" style=\"display: " + display + "\">";
            step += newD(reportCase);
            step += "</div>";
            caseNum++;
        }
        caseDiv = caseDiv + step;
        div += "<table>" +
                "<tr>" +
                "<td><h1 id =\"caseName\">当前用例:" + firstCaseName + "</h1></td>" +
                "<td>\n" +
                "    &nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</td>\n" +
                "<td>\n" +
                "    <h1>用例状态类型:</h1>\n" +
                "</td>\n" +
                "<td>\n" +
                "    <select onchange=\"showCaseType(this)\">\n" +
                "        <option value=\"all\" selected=\"selected\">全部用例</option>\n" +
                "        <option value=\"successIndicator\">通过用例</option>\n" +
                "        <option value=\"failureIndicator\">失败用例</option>\n" +
                "    </select>\n" +
                "</td>\n" +
                "<td>\n" +
                "    &nbsp;&nbsp;&nbsp;&nbsp;\n" +
                "</td>\n" +
                "<td>\n" +
                "    <input id=\"searchText\" type=\"text\">\n" +
                "    <button onclick=\"searchCase()\">搜索用例</button>\n" +
                "</td></tr>" +
                "</table>";
        div += caseDiv;
        div += "<input id=\"allCaseNum\" type=\"hidden\" value=\"" + caseNum + "\">" +
                " <input id=\"currentCaseId\" type=\"hidden\" value=\"parentCase0\">" +
                "</div></div>";
        return div;
    }

    private static String newD(ReportCase reportCase) {
        AssertionResult assertionResult = reportCase.getAssertionResult();
        String div = "<div class=\"group\">Sampler</div>\n" +
                "<div class=\"zebra\">\n" +
                "<table>\n" +
                "<tr><td class=\"data key\">Time</td><td class=\"data delimiter\">:</td><td class=\"data\">" + reportCase.getRunTime() + "</td></tr>\n" +
                "<tr><td class=\"data key\">Response Code</td><td class=\"data delimiter\">:</td><td class=\"data\">" + reportCase.getResponseCode() + "</td></tr>\n" +
                "<tr><td class=\"data key\">Response Message</td><td class=\"data delimiter\">:</td><td class=\"data\">" + reportCase.getResponseMessage() + "</td></tr>\n" +
                "</table>\n" +
                "</div>\n";
        if (null != assertionResult) {
            if (assertionResult.isFailure() || assertionResult.isError()) {
                div += "<div class=\"trail\"></div>\n" +
                        "<div class=\"group\">Assertion</div>\n" +
                        "<div class=\"zebra\">\n" +
                        "<table>\n" +
                        "<tbody class=\"failure\"><tr><td class=\"data assertion\" colspan=\"3\">" + assertionResult.getName() + "</td></tr>\n" +
                        "<tr><td class=\"data key\">Failure</td><td class=\"data delimiter\">:</td><td class=\"data\">" + assertionResult.isFailure() + "</td></tr>\n" +
                        "<tr><td class=\"data key\">Error</td><td class=\"data delimiter\">:</td><td class=\"data\">" + assertionResult.isError() + "</td></tr>\n" +
                        "<tr><td class=\"data key\">Failure Message</td><td class=\"data delimiter\">:</td>\n" +
                        "<td class=\"data\">" + assertionResult.getFailureMessage() + "</td></tr></tbody></table></div>\n";
                ;
            }
        }
        div += "<div class=\"trail\">\n" +
                "</div><div class=\"group\">Request</div>\n" +
                "<div class=\"zebra\">" +
                "<table>" +
                "<tr><td class=\"data key\">接口/Url</td><td class=\"data delimiter\">:</td><td class=\"data\"><pre class=\"data\">" + reportCase.getUrl() + "</pre></td></tr>" +
                "<tr><td class=\"data key\">Method</td><td class=\"data delimiter\">:</td><td class=\"data\"><pre class=\"data\">" + reportCase.getMethodName() + "</pre></td></tr>" +
                "<tr><td class=\"data key\">Query String</td><td class=\"data delimiter\">:</td><td class=\"data\"><pre class=\"data\">" + formatJson(reportCase.getQueryString()) + "</pre></td></tr>\n" +
                "</table>\n" +
                "</div>\n" +
                "<div class=\"trail\"></div>\n" +
                "<div class=\"group\">Response</div><div class=\"zebra\">\n" +
                "<table>\n" +
                "<tr><td class=\"data key\">Response Data</td><td class=\"data delimiter\">:</td><td class=\"data\">\n" +
                "<pre class=\"data\">" + formatJson(reportCase.getResponseData()) + "</pre></td></tr>\n" +
                "</table>\n" +
                "</div>\n";
        return div;
    }

    private static String getStyle() {
        String style = "<style type=\"text/css\">" +
                "        body {" +
                "            font:normal 68% verdana,arial,helvetica;" +
                "            color:#000000;" +
                "        }" +
                "        table tr td, table tr th {" +
                "            font-size: 68%;" +
                "        }" +
                "        table.details tr th{" +
                "            color: #ffffff;" +
                "            font-weight: bold;" +
                "            text-align:center;" +
                "            background:#2674a6;" +
                "            white-space: nowrap;" +
                "        }" +
                "        table.details tr td{" +
                "            background:#eeeee0;" +
                "            white-space: nowrap;" +
                "        }" +
                "        h1 {" +
                "            margin: 0px 0px 5px; font: 165% verdana,arial,helvetica" +
                "        }" +
                "        h2 {" +
                "            margin-top: 1em; margin-bottom: 0.5em; font: bold 125% verdana,arial,helvetica" +
                "        }" +
                "        h3 {" +
                "            margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica" +
                "        }" +
                "        .Failure {" +
                "            font-weight:bold; color:red;" +
                "        }" +
                "" +
                "" +
                "        img" +
                "        {" +
                "            border-width: 0px;" +
                "        }" +
                "" +
                "" +
                "        #div_left {" +
                "            float: left;" +
                "            width: 25%;" +
                "            height: 100%;" +
                "        }" +
                "        #div_center {" +
                "            float: left;" +
                "            width: 2%;" +
                "            height: 100%;" +
                "        }" +
                "        #div_right {" +
                "            float: left;" +
                "            width: 73%;" +
                "            height: 100%;" +
                "        }" +
                "\n" +
                "#right-panel {\n" +
                "margin-left: -40;" +
                "    right: 0;\n" +
                "    top: 0;\n" +
                "    bottom: 0;\n" +
                "    left: 11px;\n" +
                "    overflow: auto;\n" +
                "    background: white\n" +
                "}\n" +
                "\n" +
                "#right-panel .group {\n" +
                "    font-size: 15px;\n" +
                "    font-weight: bold;\n" +
                "    line-height: 16px;\n" +
                "    padding: 0 0 0 18px;\n" +
                "    counter-reset: assertion;\n" +
                "    background-repeat: repeat-x;\n" +
                "    background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAQCAYAAADXnxW3AAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9sDEBUkDq8pxjkAAAAdaVRYdENvbW1lbnQAAAAAAENyZWF0ZWQgd2l0aCBHSU1QZC5lBwAAADdJREFUCNdVxrERwDAMAzGK0v47eS6Z927SpMFBAAbkvSvnRk5+7K5cVfLMyN39bWakJAjA5xw9R94jN3tVhVEAAAAASUVORK5CYII=)\n" +
                "}\n" +
                "\n" +
                "#right-panel .zebra {\n" +
                "    background-repeat: repeat;\n" +
                "    padding: 0 0 0 18px;\n" +
                "    background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAmCAYAAAAFvPEHAAAAAXNSR0IArs4c6QAAAAZiS0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9sDEBYWFlNztEcAAAAdaVRYdENvbW1lbnQAAAAAAENyZWF0ZWQgd2l0aCBHSU1QZC5lBwAAABdJREFUCNdjYKAtePv5338mBgYGBpoQAGy1BAJlb/y6AAAAAElFTkSuQmCC)\n" +
                "}\n" +
                "\n" +
                "#right-panel .data {\n" +
                "    line-height: 19px;\n" +
                "    white-space: nowrap\n" +
                "}\n" +
                "\n" +
                "#right-panel pre.data {\n" +
                "    white-space: pre\n" +
                "}\n" +
                "\n" +
                "#right-panel tbody.failure {\n" +
                "    color: red\n" +
                "}\n" +
                "\n" +
                "#right-panel td.key {\n" +
                "    min-width: 108px\n" +
                "}\n" +
                "\n" +
                "#right-panel td.delimiter {\n" +
                "    min-width: 18px\n" +
                "}" +
                "    </style>" +
                "    <style type=\"text/css\">" +
                "        #suites {" +
                "            line-height: 1.7em;" +
                "            border-spacing: 0.1em;" +
                "            width: 100%;" +
                "        }" +
                "        .suite {" +
                "            background-color: #999999;" +
                "            font-weight: bold;" +
                "        }" +
                "        .header {" +
                "            font-size: 1.0em;" +
                "            font-weight: bold;" +
                "            text-align: left;" +
                "        }" +
                "        .header.suite {" +
                "            cursor: pointer;" +
                "            clear: right;" +
                "            height: 1.214em;" +
                "            margin-top: 1px;" +
                "        }" +
                "        .toggle {" +
                "            font-family: monospace;" +
                "            font-weight: bold;" +
                "            padding-left: 2px;" +
                "            padding-right: 5px;" +
                "            color: #777777;" +
                "        }" +
                "        .test {" +
                "            background-color: #eeeeee;" +
                "            padding-left: 2em;" +
                "        }" +
                "        .successIndicator {" +
                "            float: right;" +
                "            font-family: monospace;" +
                "            font-weight: bold;" +
                "            padding-right: 2px;" +
                "            color: #44aa44;" +
                "        }" +
                "" +
                "        .skipIndicator {" +
                "            float: right;" +
                "            font-family: monospace;" +
                "            font-weight: bold;" +
                "            padding-right: 2px;" +
                "            color: #ffaa00;" +
                "        }" +
                "        .failureIndicator {" +
                "            float: right;" +
                "            font-family: monospace;" +
                "            font-weight: bold;" +
                "            padding-right: 2px;" +
                "            color: #ff4444;" +
                "        }" +
                " .resultsTitleTable {\n" +
                "        border: 0;\n" +
                "        width: 100%;\n" +
                "        margin-top: 1.8em;\n" +
                "        line-height: 1.7em;\n" +
                "        border-spacing: 0.1em;\n" +
                "    }" +
                "        .resultsTable {" +
                "            border: 0;" +
                "            width: 100%;" +
                "            line-height: 1.7em;" +
                "            border-spacing: 0.1em;" +
                "        }" +
                "" +
                "        .resultsTable .method {" +
                "            width: 1em;" +
                "        }" +
                "        .resultsTable.passed {\n" +
                "        background: #008000;" +
                "width: 1em;\n" +
                "    }\n" +
                "    .resultsTable.failure {\n" +
                "        background: red;" +
                "width: 1em;\n" +
                "    }" +
                "        .resultsTable .duration {" +
                "            width: 6em;" +
                "        }" +
                "" +
                "        .resultsTable td {" +
                "            vertical-align: top;" +
                "        }" +
                "" +
                "        .passed {" +
                "            background-color: #44aa44;" +
                "width: 1em;" +
                "        }" +
                "" +
                "        .skipped {" +
                "            background-color: #ffaa00;" +
                "width: 1em;" +
                "        }" +
                "" +
                "        .failed {" +
                "            background-color: #ff4444;" +
                "width: 1em;" +
                "        }" +
                "        .arguments {" +
                "            font-family: Lucida Console, Monaco, Courier New, monospace;" +
                "            font-weight: bold;" +
                "        }" +
                "    </style>\n";
        return style;
    }

    private static String getJavaScript() {
        String javaScript = "\n<script language=\"JavaScript\">\n" +
                "        function toggleElement(elementId, displayStyle)" +
                "        {" +
                "            var current = getStyle(elementId, 'display');" +
                "            document.getElementById(elementId).style.display = (current == 'none' ? displayStyle : 'none');" +
                "        }" +
                "        function getStyle(elementId, property)" +
                "        {" +
                "            var element = document.getElementById(elementId);" +
                "            return element.currentStyle ? element.currentStyle[property]" +
                "                    : document.defaultView.getComputedStyle(element, null).getPropertyValue(property);" +
                "        }" +
                "\n" +
                "        function toggle(toggleId)" +
                "        {" +
                "            var toggle;" +
                "            if (document.getElementById)" +
                "            {" +
                "                toggle = document.getElementById(toggleId);" +
                "            }" +
                "            else if (document.all)" +
                "            {" +
                "                toggle = document.all[toggleId];" +
                "            }" +
                "            toggle.textContent = toggle.innerHTML == '\\u25b6' ? '\\u25bc' : '\\u25b6';" +
                "        }\n" +
                "/*\n" +
                "* 删除左面菜单所有子元素\n" +
                "* */\n" +
                "function deleteAllTestBody(testTbody) {\n" +
                "    var trArray = testTbody.childNodes;\n" +
                "    var length = trArray.length;\n" +
                "    for (var i = 0; i < length; i++) {\n" +
                "        try {\n" +
                "            var nodeName = trArray[i].nodeName;\n" +
                "            if (nodeName == \"TR\") {\n" +
                "                testTbody.removeChild(trArray[i]);\n" +
                "            }\n" +
                "        } catch (e) {\n" +
                "\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "function showCaseType(obj) {\n" +
                "    var status = \"successIndicator\";\n" +
                "    status=obj.value;\n" +
                "    var testTbody = document.getElementById(\"tests-0\");\n" +
                "    deleteAllTestBody(testTbody);\n" +
                "    var allSpan = document.getElementById(\"allSpan\");\n" +
                "    var spanDivList = allSpan.getElementsByTagName(\"div\");\n" +
                "    var htmlTr = \"\";\n" +
                "    var index =0;\n" +
                "    for (var i = 0; i < spanDivList.length; i++) {\n" +
                "        var div = spanDivList[i];\n" +
                "        try {\n" +
                "            var span = div.getElementsByTagName(\"span\")[0];\n" +
                "            var a = div.getElementsByTagName(\"a\")[0];\n" +
                "            a.setAttribute(\"id\", \"Case\" + index);\n" +
                "            var spanOuterHTML = span.outerHTML;\n" +
                "            var aOuterHTML = a.outerHTML;\n" +
                "            if (status == \"all\") {\n" +
                "                htmlTr += \"<tr><Td class='test'>\" + spanOuterHTML + aOuterHTML + \"</Td></tr>\"\n" +
                "            } else {\n" +
                "                var spanClassName = span.className;\n" +
                "                if (status == spanClassName) {\n" +
                "                    htmlTr += \"<tr><Td class='test'>\" + spanOuterHTML + aOuterHTML + \"</Td></tr>\"\n" +
                "                }\n" +
                "            }\n" +
                "            index++;\n" +
                "        } catch (E) {\n" +
                "\n" +
                "        }\n" +
                "    }\n" +
                "    testTbody.innerHTML=htmlTr;\n" +
                "}\n" +
                "function searchCase() {\n" +
                "    var searchText = document.getElementById(\"searchText\").value;\n" +
                "    var table = document.getElementById(\"suites\");\n" +
                "    var aList = table.getElementsByTagName(\"a\");\n" +
                "    if (aList.length > 0) {\n" +
                "        var index=0;\n" +
                "        for (var i = 0; i < aList.length; i++) {\n" +
                "            var obj = aList[i];\n" +
                "            var text = obj.text;\n" +
                "            if (searchText == text) {\n" +
                "                showDetail(obj);\n" +
                "                break;\n" +
                "            }\n" +
                "            console.info(text);\n" +
                "          index++;\n" +
                "        }\n" +
                "        if(index==aList.length){\n" +
                "            alert(\"当前状态下没有该用例\")\n" +
                "        }\n" +
                "    }\n" +
                "}" +
                "function showDetail(obj) {\n" +
                " var caseId = obj.id;\n " +
                " document.getElementById(\"currentCaseId\").value = caseId; \n" +
                " var caseName =obj.text;\n" +
                " document.getElementById(\"caseName\").innerHTML=\"当前用例:\"+caseName;\n" +
                "  var parentCaseId=\"parent\"+caseId;\n " +
                " var allCaseNum = document.getElementById(\"allCaseNum\").value;\n" +
                "        for (var i = 0; i < allCaseNum; i++) {\n" +
                "            var div = \"parentCase\" + i;\n" +
                "            if (div == parentCaseId) {\n" +
                "               document.getElementById(div).style.display=\"inline\"\n" +
                "            } else {\n" +
                "               document.getElementById(div).style.display=\"none\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    </script>";

        return javaScript;
    }

    private static String getHeadStart() {
//    	JMeterTask j = new JMeterTask();
//    	String filename = j.getfilename();
        String title = "<!DOCTYPE html private \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
                "<html>" +
                "<head>" +
                "    <META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
//                "    <title>"+filename+"自动化测试报告</title>";
                "    <title>自动化测试报告</title>";

        return title;
    }

    private static String getHeadEnd() {
        return "</head>";
    }
    
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

   private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
               sb.append('\t');
        }
      }  
   
	  public static String read(String filepath) throws ParseException {
		  System.out.println("开始读取日志文件:" + filepath);
		  StringBuffer str=new StringBuffer("");
		  try { 
		      File filename = new File(filepath); 
//		      InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"GB2312");
		      InputStreamReader reader = new InputStreamReader(new FileInputStream(filename),"utf-8");
		      BufferedReader br = new BufferedReader(reader);
		      String line = "";
//		      String line=br.readLine();
		      while ((line=br.readLine())!=null) {  
		      	if (line.indexOf("responseCode,responseMessage")==-1) {
		      	str.append(line + "\r\n");
		      	}
		      } 
		  } catch (Exception e) {  
		      e.printStackTrace();  
		  }  
		  return str.toString();
		  }
		  
			public static void WritetoFile(String content, String filename) {
		        try {          
//		        	String fileName = "E:\\xmind.xml";
//		        	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName),"GB2312"); 
//		            content = "<?xml version=\"1.0\" encoding=\"GB2312\"?>" + content + "</testsuite>";
		        	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename),"utf-8"); 
//		            content = content + "</testsuite>";
		            out.write(content);
		            out.flush(); 
		            out.close(); 
//		            ok = 1;
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
			
			public static void WritetoFile1(String content, String filename) {
		        try {  
		        	String a = "";
		        	try {
						a = read(filename);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//		        	String fileName = "E:\\xmind.xml";
//		        	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName),"GB2312"); 
		        	content = "loglist=[\""+content+"\","; 
//		        	System.out.println(content+"content"+a);
		        	a = a.replace("loglist=[",content);	
		        	System.out.println(a);
		        	OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename),"utf-8"); 
//		            content = content + "</testsuite>";
		            out.write(a);
		            out.flush(); 
		            out.close(); 
//		            ok = 1;
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
}
