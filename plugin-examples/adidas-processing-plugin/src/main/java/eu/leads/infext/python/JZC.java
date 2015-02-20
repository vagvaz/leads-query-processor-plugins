package eu.leads.infext.python;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class JZC {
    private static final int REQUEST_TIMEOUT = 1000;
    private static final int MAX_RETRIES = 3;       //  Before we abandon
    
    private String [] endpoints;
    
    public JZC(List<String> list) {
    	Collections.shuffle(list);
		this.endpoints = list.toArray(new String[list.size()]);
		System.out.println(list);
	}

    private JSONArray tryRequest (ZContext ctx, String endpoint, ZMsg request)
    {
        //System.out.printf("I: trying echo service at %s…\n",new Object[]{endpoint});
        Socket client = ctx.createSocket(ZMQ.REQ);
        client.connect(endpoint);

        //  Send request, wait safely for reply
        ZMsg msg = request.duplicate();
        msg.send(client);
        PollItem[] items = { new PollItem(client, ZMQ.Poller.POLLIN) };
        ZMQ.poll(items, REQUEST_TIMEOUT);
        ZMsg reply = null;
        if (items[0].isReadable())
            reply = ZMsg.recvMsg(client);
        JSONArray jsonReply = null;
        String strReply = reply.popString();
        if(strReply != null)
        	jsonReply = new JSONArray(strReply);
        //  Close socket in any case, we're done with it now
        ctx.destroySocket(client);
        reply.destroy();
        return jsonReply;
    }
    //  The client uses a Lazy Pirate strategy if it only has one server to talk
    //  to. If it has two or more servers to talk to, it will try each server just
    //  once:

    public List<Object> send(List<String> argsArray)
    {
    	List<Object> returnList = new ArrayList<Object>();
    	
        ZContext ctx = new ZContext();
        ZMsg request = new ZMsg();
        for(String arg : argsArray)
        	request.add(arg);
        JSONArray reply = null;

        int endpointsNo = this.endpoints.length;
        if (endpointsNo == 0)
            System.out.println("I: syntax: jzc <endpoint> …\n");
        else
        if (endpointsNo == 1) {
            //  For one endpoint, we retry N times
            int retries;
            for (retries = 0; retries < MAX_RETRIES; retries++) {
                String endpoint = this.endpoints[0];
                reply = tryRequest(ctx, endpoint, request);
                if (reply != null)
                    break;          //  Successful
                System.out.printf("W: no response from %s, retrying…\n", new Object[]{endpoint});
            }
        }
        else {
            //  For multiple endpoints, try each at most once
        	// TODO CHANGE THAT TO RANDOM!!!!
            int endpointNbr;
            for (endpointNbr = 0; endpointNbr < endpointsNo; endpointNbr++) {
                String endpoint = this.endpoints[endpointNbr];
                reply = tryRequest (ctx, endpoint, request);
                if (reply != null)
                    break;          //  Successful
                System.out.printf ("W: no response from %s\n", new Object[]{endpoint});
            }
        }
        if (reply != null) {
            System.out.println ("Service is running OK\n");
//            Iterator<ZFrame> itr = reply.iterator();
//            while(itr.hasNext()) {
//               Object element = itr.next();
//               returnList.add(element.toString());
//               System.out.print(element + " ");
//            }
            System.out.println("Received: <<"+reply+">>");
            JSONArray json = reply;
            for (int i=0; i<json.length(); i++)
                returnList.add( json.get(i) );
        }
        else
        	returnList = null;
        request.destroy();
        ctx.destroy();
        
        return returnList;
    }

}
