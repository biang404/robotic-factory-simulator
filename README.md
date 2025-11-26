# Welcome to Boyang XU’s SLR Course TP Demonstration!

You can download the code using **`git pull`** and open it in **Eclipse** via *Import Existing Projects into Workspace*.

---

## TP1 – Livelock Resolution

To run the TP1 content (livelock solution):

1. Run the class **`SimulatorApplication.java`** located in  
   `fr.tp.inf112.projects.robotsim.app` package.
2. When the GUI opens, choose **Local mode** and then choose **Classic Mode**.
3. You can then start and stop the simulation using the buttons  
   **Start Simulation** and **Stop Simulation**.

---

## TP2 – Remote Persistence Manager

To run the TP2 content (remote persistence manager):

1. First, run **`RemoteServer.java`** in the  
   `fr.tp.inf112.projects.robotsim.remote` package to start the server.
2. Then, run **`SimulatorApplication.java`** again (in the same package as TP1).
3. In the GUI, choose **Local mode** and then choose **Classic Mode**.
4. Now you can use **Open Canvas** and **Save Canvas** operations.

> ⚠️ Note:  
> - I have already prepared two models so you don't have to save one. 
> - If you want to save a model, do **not** to add a file extension when saving.

---

## TP3 – Microservice Simulation

To run the TP3 content (microservice):

1. Build upon the previous setup.  
2. Open the package  
   `fr.tp.inf112.projects.robotsim.service.simulation`.
3. Locate **`EntryApplication.java`**, right-click it, and select  
   **Run As → Spring Boot App**.
4. Then run **`SimulatorApplication.java`** again.
5. This time, choose **Remote mode** in the GUI, and then choose **Classic mode** for the notification.

Now you can:
- Use **Open Canvas** to load a previously saved model.
- Start the simulation with **Start Simulation**.

> ⚠️ Notes:  
> - In this mode, *Robot1* may have a noticeable initial delay, but it will eventually start moving.  
> - You may find that after clicking "starting simulation" and "stop simulation", when you try to restart it, the simualtion will go back to the initial state where the factory has just been loaded. There's no wrong because in tp3 when we try to start the simulation we're demanded to pass the model to the server again and run it from the very start. 
> - The default factory model (created at startup) has no `modelId`, so it cannot be used directly for microservice simulation — do **not** click *Start Simulation* immediately after launching the GUI, otherwise it will fall back to local mode and you will have to rerun the SimulationApplication.java.

---

## 🧠 Alternative Test (HTTP API)

If you encounter issues running TP3 via the GUI, you can also verify the project using REST API calls:

1. Run **`EntryApplication.java`** as a Spring Boot App.
2. Open your browser and send the following requests:

   - Start simulation:  
     `http://localhost:8080/api/sim/start?id=<id from Open Canvas>`
   - Check simulation state:  
     `http://localhost:8080/api/sim/state?id=<id from Open Canvas>`
   - Stop simulation:  
     `http://localhost:8080/api/sim/stop?id=<id from Open Canvas>`

After starting the simulation, you can refresh the `/state` URL repeatedly to observe real-time updates.  
Under normal conditions, you should see the coordinates of **Robot1** and **Robot2** changing continuously — indicating that they are moving.

---

## TP4 – Kafka Simulator

For this TP, I have completed the separation between the **notifier** and the **factory**, as well as the part responsible for sending Kafka messages — meaning that when the project runs, it can successfully send JSON messages through Kafka.  
I also implemented the Kafka **consumer** according to the documentation, but unfortunately, I encountered significant difficulties when trying to integrate the consumer into the main system logic, which prevented me from fully applying it in my project. I apologize for that.

To **test the Kafka message sending**, please follow these steps:

1. Start **Zookeeper** and **Kafka**.  
2. Open a terminal, enter the Kafka container, and run the following command (adjust the topic name according to your project setup):

   ```bash
   kafka-console-consumer \
     --bootstrap-server localhost:9092 \
     --topic simulation-1.rfactory \
     --from-beginning
3. After you have started both the **RemoteServer** and the **Microservice**, launch **`SimulationApplication.java`**.

4. In the GUI, select **Remote** and then **Notifier (Mode Kafka Local)**.

5. Open a factory model using **Open Canvas**, and then click **Start Simulation**.

You should now see continuously refreshed JSON messages appearing in your Kafka consumer terminal.

Regarding the consumer part, please refer to
**`fr.tp.inf112.projects.robotsim.app.FactorySimulationEventConsumer.java`**
to review my implementation.

I am sincerely sorry that I could not successfully integrate this consumer into the factory’s execution logic — the entire project turned out to be very large and complex, and I could not fully understand all of its parts.
