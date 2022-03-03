# Real-Time-Data-Analytics
Interactive analytics for Reddit | Real-Time data analysis.
The purpose of this project was to get metrics about Reddit posts in Real time using various technologies such as Angular, Apache Kafka, Spring KStream, Apache Spark, Spring Kafka Spring Webflux with Reactor.  
It contains five main components:
* `client` which is the front end app using Angular.
* a web service (`Reddit-producer`) that calls the Reddit API and gets posts. it then sends it to a Kafka topic. Here is an example of a Rest call to the reddit API: 
![image](https://user-images.githubusercontent.com/47919190/156658903-166892c5-f94c-4a51-b39f-1f60f5dee00d.png)

* two Consumers (`Kafka-Stream-Consumer` and `Spark-Consumer`) that are basically stream processors. These get the data from Kafka as a Stream, and process it in Real-time, producing  metrics and statistics that are put back in a Kafka Topic `metrics` to be consumed later.
* `Spring-Kafka-Reactive-Backend` is a service that is connected to the reddit metrics topic and waits sends it to the frontend using a websocket.
## Architecture
![image](https://user-images.githubusercontent.com/47919190/156658469-23252980-1f6e-456e-88b0-c3e4a87e8535.png)
Note: Hive wasn't used in this project.
## Screenshots
![image](https://user-images.githubusercontent.com/47919190/156659756-37ff43b9-8e77-4cf4-a2c3-15643551366e.png)


