# This file contains code for fetching information about an SSL/TLS certificate from a host
#
# Author: Josh McIntyre
#
import cryptography
import cryptography.x509
import socket
import ssl

# Define a class for fetching and parsing certificate information
class SSLInfo:

    PORT = 443
    NO_CERT_MSG = "No certificate information"

    # Initialize the certificate parser with the hostname and fetch information
    def __init__(self, hostname):
    
        # Set the hostname
        self.hostname = hostname
        
        self.cert = None
        self.subject_name = None
        self.issuer_name = None
        self.algorithm = None
        self.serial_number = None
        self.issued_on = None
        self.expires_on = None
    
        # Fetch and parse cert info, then format
        self.fetch_ssl_info()
        self.construct_cert_info()
        
    # Fetch and parse the certificate for a hostname
    def fetch_ssl_info(self):
    
        # Create a connection to fetch the certificate

        try:
            context = ssl.create_default_context()
            context.check_hostname = False
            context.verify_mode = ssl.CERT_NONE
            conn = socket.create_connection((self.hostname, self.PORT))
            sock = context.wrap_socket(conn, server_hostname=self.hostname, )
            sock.settimeout(1)
        
            pem_cert = ssl.DER_cert_to_PEM_cert(sock.getpeercert(True))
        except (socket.gaierror, ConnectionRefusedError):
            self.cert = None
            return
        
        # Get certificate info
        self.cert = cryptography.x509.load_pem_x509_certificate(pem_cert.encode())
        
        self.subject_name = self.cert.subject.rfc4514_string()
        self.issuer_name = self.cert.issuer.rfc4514_string()
        self.algorithm = self.cert.signature_algorithm_oid._name
        self.serial_number = self.cert.serial_number
        self.issued_on = self.cert.not_valid_before
        self.expires_on = self.cert.not_valid_after
    
    # Format the certificate info
    def construct_cert_info(self):
    
        if self.cert:
            self.cert_info = f"Subject name: {self.subject_name}\n"
            self.cert_info += f"Issuer name: {self.issuer_name}\n"
            self.cert_info += f"Algorithm: {self.algorithm}\n"
            self.cert_info += f"Serial number: {self.serial_number}\n"
            self.cert_info += f"Issued on: {self.issued_on}\n"
            self.cert_info += f"Expires on: {self.expires_on}\n"
        else:
            self.cert_info = self.NO_CERT_MSG
