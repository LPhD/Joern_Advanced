from octopus.server.server_command import ServerCommand

class ShellManager(object):

    def __init__(self, server_host, server_port):
        self.command = ServerCommand(server_host, server_port)

    def create(self, project_name, shellname = 'noname'):
        print("1b")
        #Here
        #response = self.command.execute_get_command("/manageshells/create/{}/{}".format(project_name, shellname))
        response = 2
        print("2b")
        try:
            port = int(response)
            print("3b")
        except:
            print(response)
            raise Exception("Error creating shell. Does the project exist?")
        print("4b")
        return port
    
    # Disconnect from server
    def disconnect(self):
        self.command._disconnect()

    def list(self, project_name=None, shell_port=None, filter_occupied=False):
        response = self.command.execute_get_command("/manageshells/list")
        if not response:
            return
        for shell in response.split('\n'):
            port, project, name, occupied = shell.split('\t')
            port = int(port)
            occupied = True if occupied == 'true' else False
            if (not project_name or project == project_name) \
                    and (not shell_port or port == shell_port) \
                    and (not filter_occupied or not occupied):
                yield port, project, name, ('occupied' if occupied else 'free')
