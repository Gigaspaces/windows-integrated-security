using GigaSpaces.Core;
using NDesk.Options;
using Security.Space;
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

                String id= new WISClient(host, port, sp).Authenticate();

                Console.WriteLine("password:" + id);

                var factory = new SpaceProxyFactory("sp");
                factory.Credentials = new SecurityContext(WindowsIdentity.GetCurrent().Name, id);

                ISpaceProxy proxy=factory.Create();

                proxy.Write(new
                {
                    Name = "Shadi Massalha"
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
            }

            Console.WriteLine("Enter any key to continue...");
            Console.ReadKey();
        }
    }
}
