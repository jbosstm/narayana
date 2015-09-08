from base64 import b64encode


def encode_to_auth_header(username, password):
    auth = encode_base_auth(username, password)
    return 'Basic {0}'.format(auth)


def encode_base_auth(username, password):
    return b64encode(b'{0}:{1}'.format(username, password)).decode('ascii')
