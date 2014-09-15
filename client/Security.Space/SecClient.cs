//using System;
//using System.Collections.Generic;
//using System.ComponentModel;
//using System.IO;
//using System.Linq;
//using System.Net.Sockets;
//using System.Runtime.Serialization.Formatters.Binary;
//using System.Security.Principal;
//using System.Text;
//using System.Threading;
//using System.Threading.Tasks;
//using System.Xml.Serialization;
//using Waffle.Windows.AuthProvider;
//using sc = Waffle.Windows.AuthProvider.Secur32;
//namespace Security.Space
//{
//    class NegotiationClient : IDisposable
//    {
//        private Socket socket;
//        private string package = "NTLM";

//        private XmlSerializer serializer = new XmlSerializer(typeof(tokenInfo));
//        private XmlSerializerNamespaces ns;

//        private WindowsSecurityContext continueContext;
//        private string connectionId;

//        public NegotiationClient()
//        {
//            this.ns = new XmlSerializerNamespaces();
//            this.ns.Add("", "");

//            this.socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            
//            //socket.Connect("10.211.55.4", 10001);
//        }

//        public void Auth()
//        {
//            WindowsSecurityContext initContext = WindowsSecurityContext.GetCurrent(package,
//               null, Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);

            
//            WindowsSecurityContext continueContext = initContext;

//            while (true)
//            {
//                tokenInfo t = new tokenInfo()
//                {
//                    connectionId = connectionId,
//                    protocol = package,
//                    token = BitUtils.ByteArrayToString(continueContext.Token)
//                };

//                Console.WriteLine("Send Token => {0}", t.token);

//                byte[] bytes = Serialize(t);

//                int count = 0;

//                while (count < bytes.Length)
//                {
//                    count += socket.Send(bytes, count, bytes.Length - count, SocketFlags.None);
//                }

//                socket.Send(UTF8Encoding.UTF8.GetBytes("\n"));

//                byte[] recBuff = new byte[1024];

//                int recived = socket.Receive(recBuff);


//                tokenInfo token = Deserialize(recBuff, recived);

//                if (!token.con)
//                {
//                    initContext.Dispose();
//                    break;
//                }

//                Console.WriteLine("Received token <= {0}", token.token);

//                byte[] responseToken = BitUtils.StringToByteArray(token.token); //Convert.FromBase64String(token.token);

                
//                continueContext = new WindowsSecurityContext(initContext, responseToken,
//                    Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);
//            }
//        }

//        public void AuthV1()
//        {
            
//            string package = "Negotiate";

//            WindowsAuthProviderImpl provider = new WindowsAuthProviderImpl();
//            // This code must be running at the client side
//            WindowsCredentialsHandle credentials = WindowsCredentialsHandle.GetCurrentCredentialsHandle(package);

            
//            WindowsSecurityContext initContext = WindowsSecurityContext.GetCurrent(package,
//                WindowsIdentity.GetCurrent().Name, Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);
//            IWindowsSecurityContext continueContext = initContext;
//            IWindowsSecurityContext responseContext = null;
//            string connectionId = Guid.NewGuid().ToString();
//            do
//            {
//                responseContext = provider.AcceptSecurityToken(connectionId, continueContext.Token, package,
//                    Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);
//                if (responseContext.Token != null)
//                {
//                    Console.WriteLine("  Token: {0}", Convert.ToBase64String(responseContext.Token));
//                    Console.WriteLine("  Continue: {0}", responseContext.Continue);
//                }

//                if (!responseContext.Continue)
//                {
//                    break;
//                }

//                continueContext = new WindowsSecurityContext(initContext, responseContext.Token,
//                    Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);
//            } while (responseContext.Continue);
//        }


//        private tokenInfo Deserialize(byte[] buffer, int size)
//        {
//            string base64Token = UTF8Encoding.UTF8.GetString(buffer, 0, size);

//            byte[] buffer1 = Convert.FromBase64String(base64Token);

//            MemoryStream streamReader = new MemoryStream(buffer1);

//            tokenInfo token = (tokenInfo)serializer.Deserialize(streamReader);

//            return token;
//        }

//        private byte[] Serialize(tokenInfo t)
//        {
//            MemoryStream streamWriter = new MemoryStream();
//            serializer.Serialize(streamWriter, t, this.ns);
//            byte[] xml = streamWriter.ToArray();
//            String str = Convert.ToBase64String(xml);
//            byte[] bytes = UTF8Encoding.UTF8.GetBytes(str);
//            return bytes;
//        }

//        private void Receive(byte[] buffer, int offset, int size, int timeout)
//        {
//            int startTickCount = Environment.TickCount;
//            int received = 0;  // how many bytes is already received
//            do
//            {
//                if (Environment.TickCount > startTickCount + timeout)
//                    throw new Exception("Timeout.");
//                try
//                {
//                    received += socket.Receive(buffer, offset + received, size - received, SocketFlags.None);
//                    Console.WriteLine("received: " + received);
//                }
//                catch (SocketException ex)
//                {
//                    if (ex.SocketErrorCode == SocketError.WouldBlock ||
//                        ex.SocketErrorCode == SocketError.IOPending ||
//                        ex.SocketErrorCode == SocketError.NoBufferSpaceAvailable)
//                    {
//                        // socket buffer is probably empty, wait and try again
//                        Thread.Sleep(30);
//                    }
//                    else
//                        throw ex;  // any serious error occurr
//                }
//            } while (received < size);
//        }

//        //public void Authenticate()
//        //{
//        //    // client ----------- acquire outbound credential handle

//        //    sc.SecHandle phClientCredential = sc.SecHandle.Zero;
//        //    //CredHandle phClientCredential = new CredHandle();
//        //    sc.SECURITY_INTEGER ptsClientExpiry = sc.SECURITY_INTEGER.Zero;

//        //    int rc = Secur32.AcquireCredentialsHandle(
//        //                             null, "Negotiate",
//        //                             sc.SECPKG_CRED_OUTBOUND,
//        //                             IntPtr.Zero,
//        //                             IntPtr.Zero,
//        //                             0,
//        //                             IntPtr.Zero,
//        //                             out phClientCredential,
//        //                             out ptsClientExpiry);
//        //    if (rc != sc.SEC_E_OK)
//        //    {
//        //        throw new Win32Exception("");
//        //    }
//        //    //assertEquals(Secur32.SEC_E_OK, );

//        //    // client ----------- security context
//        //    sc.SecHandle phClientContext = sc.SecHandle.Zero;
//        //    //CtxtHandle phClientContext = new CtxtHandle();
//        //    uint pfClientContextAttr;
//        //    //NativeLongByReference pfClientContextAttr = new NativeLongByReference();
//        //    sc.SecBufferDesc pbServerToken = new sc.SecBufferDesc(sc.MAX_TOKEN_SIZE);

//        //    while (true)
//        //    {
//        //        sc.SecBufferDesc pbClientToken = new sc.SecBufferDesc(sc.MAX_TOKEN_SIZE);
//        //        // server token is empty the first time

//        //        //int clientRc = sc.InitializeSecurityContext(
//        //        //        ref phClientCredential,
//        //        //        ref phClientContext,
//        //        //        WindowsIdentity.GetCurrent().Name,
//        //        //        sc.ISC_REQ_CONNECTION,
//        //        //        0,
//        //        //        sc.SECURITY_NATIVE_DREP,
//        //        //        ref pbServerToken,
//        //        //        0,
//        //        //        ref phClientContext,
//        //        //        ref pbClientToken,
//        //        //        out pfClientContextAttr,
//        //        //        out sc.SECURITY_INTEGER.Zero);

//        //        int clientRc = sc.InitializeSecurityContext(
//        //                ref phClientCredential,
//        //                IntPtr.Zero,
//        //                WindowsIdentity.GetCurrent().Name,
//        //                sc.ISC_REQ_CONNECTION,
//        //                0,
//        //                sc.SECURITY_NATIVE_DREP,
//        //                IntPtr.Zero,
//        //                0,
//        //                ref phClientContext,
//        //                ref pbClientToken,
//        //                out pfClientContextAttr,
//        //                out sc.SECURITY_INTEGER.Zero);

//        //        if (clientRc == sc.SEC_E_OK)
//        //            break;
//        //        if (clientRc != sc.SEC_I_CONTINUE_NEEDED)
//        //        {
//        //            throw new Win32Exception("InitializeSecurityContext");
//        //        }
//        //    }

//        //    var tokenSize = Secur32.MAX_TOKEN_SIZE;
//        //    var hasContextAndContinue = phClientContext != Secur32.SecHandle.Zero && pbServerToken != Secur32.SecBufferDesc.Zero;
//        //    string targetName = WindowsIdentity.GetCurrent().Name;

//        //    do
//        //    {
//        //        sc.SecBufferDesc pbClientToken = new sc.SecBufferDesc(sc.MAX_TOKEN_SIZE);
//        //        //_token.Dispose();
//        //        //_token = new Secur32.SecBufferDesc(tokenSize);

//        //        if (hasContextAndContinue)
//        //        {
//        //            rc = Secur32.InitializeSecurityContext(
//        //                ref phClientCredential,
//        //                ref phClientContext,
//        //                targetName,
//        //                sc.ISC_REQ_CONNECTION,
//        //                0,
//        //                 sc.SECURITY_NATIVE_DREP,
//        //                ref pbServerToken,
//        //                0,
//        //                ref phClientContext,
//        //                ref pbClientToken,
//        //                out pfClientContextAttr,
//        //                out sc.SECURITY_INTEGER.Zero);
//        //        }
//        //        else
//        //        {
//        //            rc = Secur32.InitializeSecurityContext(
//        //                ref phClientCredential,
//        //                IntPtr.Zero,
//        //                targetName,
//        //                sc.ISC_REQ_CONNECTION,
//        //                0,
//        //                 sc.SECURITY_NATIVE_DREP,
//        //                IntPtr.Zero,
//        //                0,
//        //                ref phClientContext,
//        //                ref pbClientToken,
//        //                out pfClientContextAttr,
//        //                out sc.SECURITY_INTEGER.Zero);
//        //        }

//        //        switch (rc)
//        //        {
//        //            case Secur32.SEC_E_INSUFFICIENT_MEMORY:
//        //                tokenSize += Secur32.MAX_TOKEN_SIZE;
//        //                break;
//        //            case Secur32.SEC_E_OK:
//        //                break;
//        //            case Secur32.SEC_I_CONTINUE_NEEDED:
//        //                _continue = true;
//        //                break;
//        //            default:
//        //                throw new Win32Exception(rc);
//        //        }
//        //    } while (rc == Secur32.SEC_E_INSUFFICIENT_MEMORY);
//        //}



//        public void Dispose()
//        {

//        }
//    }
//}
