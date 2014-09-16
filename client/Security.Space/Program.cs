using GigaSpaces.Core;
using NDesk.Options;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data.SqlClient;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Security.Principal;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Serialization;
using Waffle.Windows.AuthProvider;

namespace Gigaspaces.WIS.Client.ExampleSpace
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                
                string host = null;
                int port = 0;
                string sp = "Negotiate";
                string spn = null;

                var p = new OptionSet()
                {
                    {"h|host=","server host name or ip",v=> host = v },
                    {"p|port=","server port",v=> port = int.Parse(v) },
                    {"sp|secpack=","security package [Negotiate | NTLM]",v=> sp = v },
                    {"spn=","security principal name",v=> spn = v }

                };

                p.Parse(args);

                String id= new Client(host, port, sp).Authenticate();

                Console.WriteLine("password:" + id);

                var factory = new SpaceProxyFactory("sp");
                factory.Credentials = new SecurityContext(WindowsIdentity.GetCurrent().Name, id);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }

            Console.WriteLine("Enter any key to continue...");
            Console.ReadKey();
        }

        //[DllImport("advapi32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        //public static extern int DuplicateTokenEx(
        //    IntPtr existingTokenHandle,
        //    uint desiredAccess,
        //    ref SecurityAttributes threadAttributes,
        //    int impersonationLevel,
        //    int tokenType,
        //    out IntPtr duplicateTokenHandle
        //    );

        //[StructLayout(LayoutKind.Sequential)]
        //public struct SecurityAttributes
        //{
        //    public int Length;
        //    public IntPtr lpSecurityDescriptor;
        //    public bool bInheritHandle;
        //}

        //private static void CheckExistingToken()
        //{
        //    using (WindowsSecurityContext context = WindowsSecurityContext.GetCurrent(PACKAGE,
        //       WindowsIdentity.GetCurrent().Name, Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP))
        //    {

        //        Console.WriteLine("Token: {0}", Convert.ToBase64String(context.Token));
        //        Console.WriteLine("Continue: {0}", context.Continue);
        //    }

        //}

        //private static IWindowsSecurityContext Negotiate(IWindowsAuthProvider provider)
        //{
        //    string user = WindowsIdentity.GetCurrent().Name;

        //    Console.WriteLine("User: {0}", user);

        //    WindowsSecurityContext initContext = WindowsSecurityContext.GetCurrent(PACKAGE,
        //       user, Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);


        //    IWindowsSecurityContext continueContext = initContext;
        //    IWindowsSecurityContext responseContext = null;

        //    string connectionId = Guid.NewGuid().ToString();

        //    while (true)
        //    {
        //        responseContext = provider.AcceptSecurityToken(connectionId, continueContext.Token, PACKAGE,
        //            Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);

        //        if (responseContext.Token != null)
        //        {
        //            Console.WriteLine("  Token: {0}", Convert.ToBase64String(responseContext.Token));
        //            Console.WriteLine("  Continue: {0}", responseContext.Continue);
        //        }

        //        if (!responseContext.Continue)
        //        {
        //            return responseContext;
        //        }


        //        continueContext = new WindowsSecurityContext(initContext, responseContext.Token,
        //            Secur32.ISC_REQ_CONNECTION, Secur32.SECURITY_NATIVE_DREP);
        //    }
        //}
    }
}
