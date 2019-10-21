package wetterServicePackage;

import org.apache.thrift.TException;

public class ServerHandler implements Calc.Iface {

    @Override
    public boolean sendData (String data){
        System.out.println("Received: " + data);
        return true;
    }
}
