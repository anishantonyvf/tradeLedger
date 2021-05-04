# tradeLedger
This Application TL-Api expose an endpoint at http://localhost:8080/CheckApplicantEligibility which accepts post requests with the following JSON structure:
{
"name" : "TestName",
"address" : "TestAddress",
"email" : "TestEmail"
}

This application internally invokes the third-party endpoint hosted on http://localhost:3317/eligibility/check to fetch the card product eligibility data. The third-party invocation is guarded by resilience4j circuit breaker. The response from the third party is saved asynchronously in the following Cassandra database table EligibilityResponse with TTL for records set as 7 years: 

CREATE KEYSPACE responsecapture WITH replication= { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 1}

CREATE TABLE responsecapture.eligibilityresponse (
    eligibilityidentifier text primary key, response text
) WITH default_time_to_live = 221356800;

In case the same request is received in TL-Api , the response from the third party is appended to the previously stored response along with the date in JSON format. 

Accomodating the design considerations :

1.	To cater to the peak volume of requests during the initial startup, Spring cloud Load balancer (SpringCloud Ribbob ) and service discovery (SpringCloud Eureka ) can be employed to scale the TL-Api service horizontally. Asynchronous CompletableFuture is used so that the response time for the service is not affected.
2.	The time-out of the TL-Api service is set as 15 sec to cater to the timeout of 10 Secs of the third-party service. 
3.	Processed request are persisted in Cassandra table having TTL for 7 year which can be used to Audi purpose.


