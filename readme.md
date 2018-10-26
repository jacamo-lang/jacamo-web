To run:
* `gradle marcos` runs agent marcos and the REST platform
* `gradle bob` runs agents bob and alice. Bob sends a message to marcos using its rest API.
* see http://yourIP:3280 for a web interface (see the console for the right IP)

Notes:
* Each agent has a REST API to receive message and be inspected
* ZooKeeper is used for name service. Bob can send a message to `marcos` using its name. ZooKeeper maps the name to a URL.
* DF service is provided also by ZooKeeper
* Java JAX-RS is used for the API

See ClientTest.java for an example of Java client. It can be run with `gradle test1`.

# REST API

## for humans

* GET HTML `/agents`:
    returns the list of running agents

* GET HTML `/agents/{agentname}/all`
    returns the mind state of the agent

* GET HTML `/services`
    returns the DF state

# for machine (or intelligent humans)

* POST XML `/agents/{agentname}/mb`
    Adds a message in the agent's mailbox. See class Message.java for details of the fields.

* GET XML `/agents/{agentname}/all`
    returns the mind state of the agent

* GET TXT /agents/{agentname}/plans
    return the agent's plans

See RestImpl.java for more
