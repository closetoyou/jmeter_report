# jmeter_report
重新封装 ant-jmeter 驱动包

build.xml 样例

<?xml version="1.0" encoding="UTF-8"?>

<project name="ant-jmeter-test" default="run" basedir=".">
	
	<tstamp>
		<format property="time" pattern="yyyyMMddhhmm" />
	</tstamp>
		
	<property name="jmeter.home" value="/usr/local/apache-jmeter-5.3" />
	<property name="jmeter.init.dir" value="/data/init_workspace/jxdj_init" />
	<property name="jmeter.result.jtl.dir" value="/data/result_workspace/jxdj_jtl" />
	<property name="jmeter.result.html.dir" value="/data/result_workspace/jxdj_html" />
	<property name="jmeter.script.dir" value="" />
	<property name="jmeter.run.user" value="" />	
	<property name="reportName" value="" />
	<property name="reportUrl" value="" />
	<property name="reportFileName" value="" />
	<property name="jmeter.result.jtlName" value="${jmeter.result.jtl.dir}/${reportName}-${time}.jtl" />
	<property name="jmeter.result.htmlName" value="${jmeter.result.html.dir}/report-${time}.html" />
		
	<target name="run">	
		<antcall target="clean" />
		<antcall target="test"/>
		<antcall target="reportTask" />
		<antcall target="report" />
	</target>
		
	<target name="test">		
		<taskdef name="jmeter" classname="org.test.ant.taskdefs.jmeter.JMeterTask" />
		<jmeter jmeterhome="${jmeter.home}" resultlog="${jmeter.result.jtlName}">				
			<testplans dir="${jmeter.script.dir}" includes="*.jmx" />
			<property name="jmeter.save.saveservice.output_format" value="xml"/>					
		</jmeter>
	</target>
			
	<target name="reportTask">
		<taskdef name="myReportTask" classname="org.test.ant.taskdefs.jmeter.ReportTask" />
		<myReportTask 
		in="${jmeter.result.jtlName}"		
		out="${jmeter.result.htmlName}"
		mailhost=""
		ssl="true"
		user=""
		password=""
		mailTitle=""
		from=""
		toAddress=""
		runUser="${jmeter.run.user}"
		reportUrl="${reportUrl}"
		reportName="${reportFileName}"
		>
		
		</myReportTask>
	</target>

    <target name="report">
        <copy file="${jmeter.result.htmlName}" tofile="${jmeter.result.html.dir}/jxdj-report.html" />
    </target>

	<target name="clean">
		<echo>初始化目录</echo>
		<delete includeemptydirs="true">
			<fileset dir="${jmeter.init.dir}" includes="**/*"/>
		</delete>
		<delete file="${jmeter.result.html.dir}/jxdj-report.html"></delete>
	</target>

</project>

myReportTask 为重新封装的方法 
主要提供 jenkins 参数化驱动及日志报告输出
jenkins ant 参数样例
ant -Djmeter.script.dir="脚本路径" -Djmeter.run.user="执行人" -DreportName="报告名称（后端）" -DreportUrl="报告weburl" -DreportFileName="报告名称（前端）"


