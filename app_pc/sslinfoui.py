# This file contains a simple UI for fetching TLS certificate information
#
# Author: Josh McIntyre
#
import tkinter

import sslinfo

# Define an SSLInfo UI class
class SSLInfoUI:

    # Constants
    TITLE = "SSLInfo"
    WIDTH = 600
    HEIGHT = 300
    GEOMETRY = f"{WIDTH}x{HEIGHT}"
    LISTBOX_WIDTH = 80
    LISTBOX_HEIGHT = 10
    HOST_HEIGHT = 1
    NO_CERT_MSG = "No certificate information"
    

    # Constructor, sets basic variables up
    def __init__(self):
    
        # Set up basic class storage
        self.hostname = None
        self.cert_info = self.NO_CERT_MSG
        self.cert_info_updated = True
        
        # Configure UI elements
        self.configure_ui()
        
        # Start the main loop
        self.main_loop()
        
    # Configure UI elements
    def configure_ui(self):
    
        self.window = tkinter.Tk()
        self.window.title(self.TITLE)
        self.window.geometry(self.GEOMETRY)
        
        self.host_text = tkinter.Text(width=self.LISTBOX_WIDTH, height=self.HOST_HEIGHT, wrap=tkinter.WORD)
        self.host_label = tkinter.Label(text="Hostname")
        
        self.fetch_button = tkinter.Button(text="Get certificate info", command=self.fetch_cert_info)
        self.fetch_text = tkinter.Text(width=self.LISTBOX_WIDTH, height=self.LISTBOX_HEIGHT, wrap=tkinter.WORD)

    # Main loop for the UI
    def main_loop(self):
    
        while True:
        
            # Update any elements that need updating
            if self.cert_info_updated:
                self.fetch_text.delete(1.0, tkinter.END)
                self.fetch_text.insert(1.0, self.cert_info)
                self.cert_info_updated = False
            

            # Pack the UI elements
            self.host_text.pack()
            self.host_label.pack()
            self.fetch_button.pack()
            self.fetch_text.pack()
        
            # Update the tkinter window
            self.window.update() 
            
    # Fetch certificate information
    def fetch_cert_info(self):
    
        # Get the hostname from the text field
        self.hostname = self.host_text.get(1.0, tkinter.END).strip()

        # Fetch certificate info
        self.cert_info = sslinfo.SSLInfo(self.hostname).cert_info
        self.cert_info_updated = True

# The main entry point for the program
def main():

    sslinfoui = SSLInfoUI()

if __name__ == "__main__":

    main()