using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Serialization;

namespace Security.Space
{
    [Serializable]
    public class IdentityMessage
    {
        public string Fqn { get; set; }
        [XmlElement("Group")]
        public List<string> Groups { get; set; }
    }
}
