Reverse Proxy
=============

Testing clustered application deployment requires testing of each instance of an application. Typically this is achieved by hitting each deployment's IP and port directly. Unfortunately some applications and infrastructure components are seneitive to address part of http request.

To deal with this reverse proxy associates a DNS name with specific IP:port combination. It is implemented as an http proxy to allow browsers and scripts to continue working as if with a normal setup. DNS - IP associations can be changed on the fly using simple RESTful API and a simple web console. Proxy supports http2http, https2http and https2https associations.

To run:
java -classpath reverseproxy-<version>.jar com.ryaltech.tools.proxy.Launcher [-proxyPort <proxyPort>] [-managementPort <managementPort>] [-propertyFile <propertyFile>]
  <proxyPort> - port proxy listens on. It defaults to 8080
	<managementPort> - port to start management server on.  If not specified management server will not start.
	<propertyFile> - location to the file that contains initial proxy configuration.  If not specified none will be used. /src/test/resources/test.properties contains an example of a property file


To manage afterwards through the browser:
 http://localhost:<managementPort>

REST API:
 * To add/change address mapping use HTTP PUT http://localhost:<managementPort>/addressmap/from/to
 * To remove address mapping use HTTP DELETE http://localhost:<managementPort>/addressmap/from
 * To get current configuration use HTTP GET http://localhost:<managementPort>/addressmap
 

