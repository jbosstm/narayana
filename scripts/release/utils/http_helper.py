import httplib


def request(method, host, path, body=None, headers={}):
    """
    Generic HTTP request method
    """
    connection = httplib.HTTPConnection(host)
    connection.request(method, path, body, headers)
    return connection.getresponse()


def get(host, path, body=None, headers={}):
    """
    HTTP GET request
    """
    return request('GET', host, path, body, headers)


def post(host, path, body=None, headers={}):
    """
    HTTP POST request
    """
    return request('POST', host, path, body, headers)


def put(host, path, body=None, headers={}):
    """
    HTTP PUT request
    """
    return request('PUT', host, path, body, headers)
