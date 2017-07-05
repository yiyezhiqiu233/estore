# estore
网上商城

to prevent throw of MaxUploadSizeExceededException:
	in tomcat/conf/server/xml:
		<Connector port="..." protocol="..."
               connectionTimeout="..."
               maxSwallowSize="-1"			<-- set "-1"
               redirectPort="..." />
			   
<reference link="https://tomcat.apache.org/tomcat-8.0-doc/config/http.html"/>
