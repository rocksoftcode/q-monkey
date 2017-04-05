q-monkey
=======

q-monkey is a Java-based queue abstraction that allows multithreaded processing of events and requires no additional dependencies.

grinder offers a simple way to operate against a queue and is well-suited for standalone applications. It was initially put together to process crawling large web applications/sites as an alternative to recursion. 

There are a couple of minor concessions to use grinder:
<ul>
<li>A class that implements <code>QMonkeyConsumer</code> must be supplied, to operate on queue items.</li>
<li>grinder uses an <code>ArrayBlockingQueue</code>, which has a constrained capacity.  That said, grinder in its unmodified form may not be perfectly suited for *huge*-scale applications at this time.</li>
<li>grinder asks for a timeout value, so that a program's execution can terminate (grinder watches its delegate queue).</li>
</ul>

*Usage* (Groovy)
```
QMonkey<String> q = new QMonkey<>(5)
q.start(myConsumer)
```
