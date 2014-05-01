import commands
import datetime
import sys
import time

from wsgiref.simple_server import make_server, WSGIRequestHandler, WSGIServer
import CGIHTTPServer

"""
nsinfo (namespace info) web server

Web server which returns the ip:port on which it is running.
"""

class NsInfoServer:
    def __init__(self, num):
        self.port_num = num
        addr = commands.getoutput("ip address").split()
        self.ip_addr = addr[addr.index('inet') + 1].split('/')[0]
        self.to_return = "%s:%s" % (self.ip_addr, self.port_num)

    def app(self, environ, start_response):
        start_response('200 OK', [])
        yield self.to_return

class MyHandler(WSGIRequestHandler):
    # Disable logging DNS lookups - DNS doesn't work in MMM
    def address_string(self):
        return str(self.client_address[0])

    def log_request(*args, **kw): pass

from SocketServer import ThreadingMixIn
class DevServer(ThreadingMixIn, WSGIServer):
    pass

if __name__ == '__main__':
    if len(sys.argv) < 2:
        sys.exit("Takes one argument, the port number to serve on" +
                 "- for example 'python nsinfo_web_server.py 8080' to" +
                 " serve on port 8080")
    else:
        miniserver = NsInfoServer(int(sys.argv[1]))
        server = make_server("0.0.0.0",
                             miniserver.port_num,
                             miniserver.app,
                             server_class = DevServer,
                             handler_class = MyHandler)
        server.serve_forever()