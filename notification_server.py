import tornado.ioloop
import tornado.web
import asyncio


class ServerData:
    def __init__(self, data):
        self.data = data


state = ServerData('any data')

class MainRequestHandler(tornado.web.RequestHandler):
    def get(self):
        response = {
            'info': 'server ready on port 8888'
        }
        self.write(response)

class DateRequestHandler(tornado.web.RequestHandler):
    def get(self):
        response = {
            'data': state.data
        }
        self.write(response)


class DateChangeHandler(tornado.web.RequestHandler):
    def post(self):
        json = tornado.escape.json_decode(self.request.body)
        state.data = str(json["data"])
        response = {
            'info': 'data has been changed',
            'data': state.data
        }
        self.write(response)


if __name__ == "__main__":
    app = tornado.web.Application([
        (r"/", MainRequestHandler),
        (r"/data", DateRequestHandler),
        (r"/post", DateChangeHandler)
    ])

    app.listen(8888)
    print("listening port 8888")
    tornado.ioloop.IOLoop.instance().start()
