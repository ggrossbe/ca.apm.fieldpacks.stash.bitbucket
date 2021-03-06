# Stash-BitBucket Field Pack 1.0

##Description
The Stash Field Pack gathers metrics from the Stash-BitBucket REST API & reports them via the EPAgent v9.7.1 + API.

Being my first published Java FP I must give a ShoutOut to my colleague Fayaz Ghiasy for helping me get started!!!

## APM version
Minimum Restful EPAgent 9.7.1

## Supported third party versions
- Developed on Mac OS, Atlassian Bitbucket v4.2.0
- Tested on RedHat, Atlassian Bitbucket v3.11.0

## Limitations
- I have yet to determine if the MERGED & DECLINED Pull Requests increment values based on my current approach for gathering the data.  The OPEN Pull Requests has been tested succesfully.  This has only been tested in a couple small environments thus far.  I am in progress of testing on full productions sized environment.  
- Only 1 STASH REST API URL can be monitored at this time.

# Installation Instructions
```
Required Files: StashFP.jar & stash.propeties

* Install, configure and run an EPAgent on the same or a remote server. See [CA APM Environment Performance Agent Implementation Guide](https://wiki.ca.com/display/APMDEVOPS97/CA+APM+Environment+Performance+Agent+Implementation+Guide).

1. Move provided StashFP.jar to <EPAgentHome>/epaplugins folder
2. Move provided stash.properties to <EPAgentHome> folder
3. Edit <EPAgentHome>/IntroscopeEPAgent.properties file
4. Add StashFP.jar as STATEFUL plugin.  
  * Example Provided Below
5. Set ClassPath for EPAgent: Edit <EPAgentHome>/bin/EPACtrl.sh
	Append the following ":epaplugins/StashFP.jar" to the "EpaCmd="java -Xms${MIN_HEAP_VAL_IN_MB}m -Xmx${MAX_HEAP_VAL_IN_MB}m -cp" line
	# the command to start the EPAgent

	* Example Directly Below
# the command to start the EPAgent
EpaCmd="java -Xms${MIN_HEAP_VAL_IN_MB}m -Xmx${MAX_HEAP_VAL_IN_MB}m -cp lib/EPAgent.jar:lib/IntroscopeServices.jar:lib/Agent.jar:epaplugins/epaMQMonitor/epaMQMonitor.jar:epaplugins/epaMQMonitor:epaplugins/epaMQMonitor/lib/com.ibm.mq.pcf.jar:epaplugins/epaMQMonitor/lib/com.ibm.mq.jar:epaplugins/epaMQMonitor/lib/connector.jar:epaplugins/epaMQMonitor/lib/com.ibm.mqjms.jar:epaplugins/StashFP.jar com.wily.introscope.api.IntroscopeEPAgent"

```
```
#################################
# Stateful Plugins
#-----------------
introscope.epagent.plugins.stateful.names=StashFP
introscope.epagent.stateful.StashFP.class=com.apm.stash.StashFPwrapper
```
## Prerequisites
Ensure BitBucket has REST API Enabled
BitBucket User has access to the REST API

## Metric description
- Stash Server
  - Is Last Page: Similar to the Limit metric this notifies you if there is additional data the FieldPack is not seeing due to the default contraint on the Stash API
  - Limit: By default Stash allows only 25 results retruned when callin the API.  This metric will notify you if you have reached the default max & need to increase the default value
  - Numer of Repos:Total Number of Repositories currently stored on BitBucket instance
- Listed Per Repository
  - Total Number of Pull Requests
  - Total Number of OPEN Pull Requests
  - Total Number of MERGED Pull Requests
  - Total Number of DECLINED Pull Requests

## Custom Management Modules
None

## Custom type viewers
See provided screenshot for details

# Debugging and Troubleshooting
xxxxx

## Support
This document and associated tools are made available from CA Technologies as examples and provided at no charge as a courtesy to the CA APM Community at large. This resource may require modification for use in your environment. However, please note that this resource is not supported by CA Technologies, and inclusion in this site should not be construed to be an endorsement or recommendation by CA Technologies. These utilities are not covered by the CA Technologies software license agreement and there is no explicit or implied warranty from CA Technologies. They can be used and distributed freely amongst the CA APM Community, but not sold. As such, they are unsupported software, provided as is without warranty of any kind, express or implied, including but not limited to warranties of merchantability and fitness for a particular purpose. CA Technologies does not warrant that this resource will meet your requirements or that the operation of the resource will be uninterrupted or error free or that any defects will be corrected. The use of this resource implies that you understand and agree to the terms listed herein.

Although these utilities are unsupported, please let us know if you have any problems or questions by adding a comment to the CA APM Community Site area where the resource is located, so that the Author(s) may attempt to address the issue or question.

Unless explicitly stated otherwise this field pack is only supported on the same platforms as the APM core agent. See [APM Compatibility Guide](http://www.ca.com/us/support/ca-support-online/product-content/status/compatibility-matrix/application-performance-management-compatibility-guide.aspx).


# Change log
Version | Authors | Comment
--------|--------|--------
1.0 | [Alex York] | First version of the field pack.

