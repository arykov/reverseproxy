Reverse Proxy
============

Testing clustered application deployment requires testing of each instance of an application. Typically this is achieved by hitting each deployment's IP and port directly. Unfortunately some applications and infrastructure components are seneitive to address part of http request.

To deal with this reverse proxy associates a DNS name with specific IP:port combination. It is implemented as an http proxy to allow browsers and scripts to continue working as if with a normal setup. DNS - IP associations can be changed on the fly using simple RESTful API and a simple web console. Proxy supports http2http, https2http and https2https associations.

**To run, use the following command :**   _java -classpath reverseproxy-<version>.jar com.ryaltech.tools.proxy.Launcher [-proxyPort proxyPort] [-managementPort managementPort] [-propertyFile propertyFile]_
  * _proxyPort_ - port proxy listens on. It defaults to 8080
  * _managementPort_ - port to start management server on.  If not specified management server will not start.
  * _propertyFile_ - location to the file that contains initial proxy configuration.  If not specified none will be used. [Here's an example of a property file](https://github.com/arykov/reverseproxy/blob/master/src/test/resources/test.properties).


**To manage using a browser:**

 _http://localhost:[managementPort]_

REST API:
 * To add/change address mapping use HTTP PUT http://localhost:[managementPort]/addressmap/from/to. For example: _http://localhost:[managementPort]/addressmap/http://cnn.com/http://bbc.co.uk_
 * To remove address mapping use HTTP DELETE http://localhost:[managementPort]/addressmap/from. For example: _http://localhost:[managementPort]/addressmap/http://cnn.com_
 * To get current configuration use HTTP GET http://localhost:[managementPort]/addressmap
 

