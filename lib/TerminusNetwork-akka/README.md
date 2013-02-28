mvn compile

# for server:
mvn exec:java -Dexec.mainClass="edu.buffalo.cse.terminus.server.TerminusServer"

# for client:
mvn exec:java -Dexec.mainClass="edu.buffalo.cse.terminus.client.TerminusClient"
