# estore
网上商城
  
 1. To prevent throw of MaxUploadSizeExceededException:<br/>
	in tomcat/conf/server/xml:<br/>
	
		<Connector port="..." protocol="..."
               connectionTimeout="..."
               maxSwallowSize="-1"			<-- set "-1"
               redirectPort="..." />

	[reference](https://tomcat.apache.org/tomcat-8.0-doc/config/http.html)
