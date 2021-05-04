# tradeLedger

About the Application:
1.	This TL-Api application exposes an endpoint at http://localhost:8080/CheckApplicantEligibility which accepts post requests with the following JSON structure:
{
"name" : "TestName",
"address" : "TestAddress",
"email" : "TestEmail"
}

2.	This application internally invokes the third-party endpoint hosted on http://localhost:3317/eligibility/check to fetch the card product eligibility data. The third-party invocation is guarded by resilience4j circuit breaker. The response from the third party is saved asynchronously in the following Cassandra database table EligibilityResponse with TTL for records set as 7 years: 
CREATE KEYSPACE responsecapture WITH replication= { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1}
CREATE TABLE responsecapture.eligibilityresponse (
    eligibilityidentifier text primary key, response text
) WITH default_time_to_live = 221356800;

3.	In case the same request is received in TL-Api , the response from the third party is appended to the previously stored response along with the date in JSON format. 

Pre-requisites to start the TL-Api Application: 
1.	Ensure the Third-party service is up and running at http://localhost:3317/eligibility/check. If there is a change in URL , modify the property “eligibility.url” in application.properties to match it to the correct URL.
2.	Ensure an instance of Cassandra cluster is running and provide the details of IP, keyspace and port in application.properties. Table will be created in the keyspace if already not existing.

Starting the TL-Api Application:
1.	TL-Api application can be started as normal springboot application from the class  CardsApplication located in com.tradeledger.cards folder.

Accommodating the design considerations:
1.	To cater to the peak volume of requests during the initial startup, Spring cloud Load balancer (SpringCloud Ribbon ) and service discovery (SpringCloud Eureka ) can be employed to scale the TL-Api service horizontally. Asynchronous CompletableFuture is used so that the response time for the service is not affected.
2.	The time-out of the TL-Api service is set as 15 sec to cater to the timeout of 10 Secs of the third-party service. 
3.	Processed requests are persisted in Cassandra table having TTL for 7 year which can be used to Audi purpose. In case of any issues in saving the response, Splunk dashboards can be created to raise an alert and the alert would need a manual intervention. 


