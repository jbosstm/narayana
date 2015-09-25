
def get_boolean(message):
    s = raw_input(message + ': ')
    return string_to_boolean(s)


def string_to_boolean(s):
    return s.lower() in ('yes', 'y', 'true')
