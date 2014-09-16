using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Security.Principal;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Serialization;
using Waffle.Windows.AuthProvider;

namespace Security.Space
{
    public class WISClient
    {
        private WindowsSecurityContext clientContext;
        private WindowsSecurityContext initContext;
        private TcpClient client;
        private XmlSerializer serializer = new XmlSerializer(typeof(Message));
        private XmlSerializerNamespaces _defaultNamespace;
        private IPEndPoint remoteEP;
        private string package;

        public WISClient(string host, int port, string package)
        {
            this.package = package;
            this._defaultNamespace = new XmlSerializerNamespaces();
            this._defaultNamespace.Add("", "");

            this.client = new TcpClient();

            IPAddress address = Dns.GetHostAddresses(host)[0];

            this.remoteEP = new IPEndPoint(address, port);
        }


        public string Authenticate()
        {
            client.Connect(remoteEP);

            sendMessage(new HelloMessage());

            using (StreamReader reader = new StreamReader(client.GetStream()))
            {
                string line;

                while ((line = reader.ReadLine()) != null)
                {
                    Message msg = Deserialize(line);
                    Object body = msg.Body;
                    Console.WriteLine("message receiving: {0}", body);

                    if (body is AuthorizationRequiredMessage)
                    {
                        WindowsCredentialsHandle credentials = WindowsCredentialsHandle.GetCurrentCredentialsHandle(package);
                        initContext = WindowsSecurityContext.GetCurrent(package, WindowsIdentity.GetCurrent().Name,
                            Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);

                        clientContext = initContext;

                        sendMessage(new TokenMessage(clientContext.Token));
                    }
                    else if (body is AuthorizedMessage)
                    {
                        return ((AuthorizedMessage)body).Id;
                    }
                    else if (body is TokenMessage)
                    {
                        TokenMessage tokenMessage = (TokenMessage)body;

                        try
                        {
                            byte[] tkn = BitUtils.StringToByteArray(tokenMessage.Token);

                            clientContext = new WindowsSecurityContext(initContext, tkn,
                                    Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);

                            sendMessage(new TokenMessage(clientContext.Token));
                        }
                        catch (Win32Exception ex)
                        {
                            Console.WriteLine("exception receiving token: {0}", ex);
                            throw ex;
                        }
                    }
                    else
                    {
                        //TODO: handle unknown message
                        break;
                    }
                }
            }

            return null;
        }

        private void sendMessage(Object msg)
        {
            Message message = new Message(msg);

            byte[] buff = SerializeMessage(message);

            if (client.GetStream().CanWrite)
            {
                client.GetStream().Write(buff, 0, buff.Length);

                client.GetStream().Flush();
            }
            else
            {
                throw new IOException("can not send message");
            }
        }

        private byte[] SerializeMessage(Message message)
        {
            string messageString;

            using (var stringWriter = new MemoryStream())
            {
                using (var writer = XmlWriter.Create(stringWriter))
                {
                    serializer.Serialize(writer, message, _defaultNamespace);
                }

                messageString = Convert.ToBase64String(stringWriter.ToArray());
            }

            return Encoding.UTF8.GetBytes(messageString + "\n");
        }

        private Message Deserialize(string m)
        {
            byte[] buff = Convert.FromBase64String(m);

            Message message = null;

            using (var stringReader = new MemoryStream(buff))
            {
                using (var reader = XmlReader.Create(stringReader))
                {
                    message = (Message)serializer.Deserialize(reader);
                }
            }

            return message;
        }
    }
}
