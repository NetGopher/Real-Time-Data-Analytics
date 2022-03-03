# Real-Time-Data-Analytics
Interactive analytics for Reddit | Real-Time data analysis.
The purpose of this project was to get metrics about Reddit posts in Real time using various technologies such as Angular, Apache Kafka, Spring KStream, Apache Spark, Spring Kafka Spring Webflux with Reactor.  
It contains five main components:
* `client` which is the front end app using Angular.
* a web service (`Reddit-producer`) that calls the Reddit API and gets posts. it then sends it to a Kafka topic. Here is an example of a Rest call to the reddit API: 
![Rapport_stage_d'application67](https://user-images.githubusercontent.com/47919190/156660688-4630e007-23a8-4fb8-802c-4d61523645eb.jpg)

* two Consumers (`Kafka-Stream-Consumer` and `Spark-Consumer`) that are basically stream processors. These get the data from Kafka as a Stream, and process it in Real-time, producing  metrics and statistics that are put back in a Kafka Topic `metrics` to be consumed later.
* `Spring-Kafka-Reactive-Backend` is a service that is connected to the reddit metrics topic and waits sends it to the frontend using a websocket.
## Architecture
![Rapport_stage_d'application66](https://user-images.githubusercontent.com/47919190/156660684-306a7f97-c18d-47b3-ae12-f7efafda5611.jpg)
Note: Hive wasn't used in this project.
## Screenshots



![Rapport_stage_d'application](https://user-images.githubusercontent.com/47919190/156659950-3fa5571e-5f85-4125-866a-3d428a482496.jpg)
![Rapport_stage_d'application 4jpg](https://user-images.githubusercontent.com/47919190/156660118-1599a61b-e457-475c-9b78-b544f4645cfa.jpg)
![Rapport_stage_d'application3](https://user-images.githubusercontent.com/47919190/156660127-48dbe5ad-892d-490f-bb6e-01e416e463c3.jpg)
![Rapport_stage_d'application2](https://user-images.githubusercontent.com/47919190/156660131-f3ee9afd-62fe-4ca9-899c-87266e6525dd.jpg)
![Rapport_stage_d'application9](https://user-images.githubusercontent.com/47919190/156660134-7ba502fb-34c0-4aba-b076-ccf6b8027649.jpg)
![Rapport_stage_d'application8](https://user-images.githubusercontent.com/47919190/156660140-9ae1b2ca-aa85-433c-aa3f-136ecff92845.jpg)
![Rapport_stage_d'application7](https://user-images.githubusercontent.com/47919190/156660144-fd06bb2c-cb65-4d4f-8723-6e279e1b0f04.jpg)
![Rapport_stage_d'application6](https://user-images.githubusercontent.com/47919190/156660154-96b8884f-dc4e-40d9-92a2-c37e5300444f.jpg)
![Rapport_stage_d'application5](https://user-images.githubusercontent.com/47919190/156660155-8e00ca7a-c4e4-4e64-a90d-757608e195f3.jpg)


