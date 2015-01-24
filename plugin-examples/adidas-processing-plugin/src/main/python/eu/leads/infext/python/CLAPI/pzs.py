#
# Freelance server - Model 1
# Trivial echo service
#
# Author: Daniel Lundin <dln(at)eintr(dot)org>
#

import sys
import zmq
from eu.leads.infext.python.CLAPI import helloworld_clinterface
from eu.leads.infext.python.CLAPI.conversionmethods import translateInputParameters

def factory(str):
    if str == 'eu.leads.infext.python.CLAPI.helloworld_clinterface':
        return helloworld_clinterface.AccessPoint()
    elif str == '':
        return
    # and so on

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print "I: Syntax: %s <endpoint>" % sys.argv[0]
        sys.exit(0)
    
    endpoint = sys.argv[1]
    context = zmq.Context()
    server = context.socket(zmq.REP)
    print endpoint
    server.bind(endpoint)
    
    print "I: Echo service is ready at %s" % endpoint
    while True:
        msg = server.recv_multipart()
        if not msg:
            break  # Interrupted
        
        # read message
        # pt.1 module name
        print "module name:", msg[0]
        ap = factory(msg[0])
        # pt.2 all of the params
        print "params: ", [text for text in msg[1:]]
        params = translateInputParameters(msg[1:])
        # call a class with all of the params as list 
        retval = ap.execute(params)
        
        server.send_multipart(retval)
    
    server.setsockopt(zmq.LINGER, 0) # Terminate immediately




