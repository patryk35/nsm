package pdm.networkservicesmonitor.agent.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pdm.networkservicesmonitor.agent.AppConstants;
import pdm.networkservicesmonitor.agent.connection.ConnectionController;
import pdm.networkservicesmonitor.agent.connection.MonitorWebClient;
import pdm.networkservicesmonitor.agent.payloads.DataPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Component
@Slf4j
public class PacketManager implements Runnable{

    @Autowired
    private MonitorWebClient monitorWebClient;

    private List<String> logs;

    private boolean isLocked;

    private Queue<DataPacket> packetQueue;


    public PacketManager(){
        isLocked = false;
        logs = new ArrayList<>();
        packetQueue = new ArrayBlockingQueue<>(AppConstants.MAX_PACKETS_IN_SENDING_QUEUE);
    }

    public void addLog(String logEntry){
        log.error("Log added");

        while(isLocked){
            log.trace("Waiting... Log will be added when adding will be unlocked");
            try {
                Thread.sleep(20); //TODO: const
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logs.add(logEntry);
    }

    @Override
    public void run() {

        while (true){
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isLocked = true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DataPacket dataPacket = new DataPacket(logs);
            logs = new ArrayList<>();
            isLocked = false;
            try {
                while(!packetQueue.isEmpty()) {
                    monitorWebClient.sendPacket(packetQueue.peek());
                }
                monitorWebClient.sendPacket(dataPacket);

            } catch (Exception e){
                log.error("Packet cannot be send due to connection problems");
                log.error(e.getMessage());
                if(packetQueue.size() == 20){
                    //TODO: save to file all 21 packets
                    // below temporary option
                    Queue<DataPacket> dataPackets = new ArrayBlockingQueue<>(2*packetQueue.size());
                    dataPackets.addAll(packetQueue);
                    dataPackets.add(dataPacket);
                    log.error("Queue length duplicated");
                } else {
                    packetQueue.add(dataPacket);
                }
            }
            log.trace("Log sended");

        }

    }
}
